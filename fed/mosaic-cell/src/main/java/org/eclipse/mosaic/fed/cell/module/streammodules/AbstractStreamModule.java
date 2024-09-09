/*
 * Copyright (c) 2020 Fraunhofer FOKUS and others. All rights reserved.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contact: mosaic@fokus.fraunhofer.de
 */

package org.eclipse.mosaic.fed.cell.module.streammodules;

import org.eclipse.mosaic.fed.cell.chain.ChainManager;
import org.eclipse.mosaic.fed.cell.config.model.CNetworkProperties;
import org.eclipse.mosaic.fed.cell.config.model.TransmissionMode;
import org.eclipse.mosaic.fed.cell.message.CellModuleMessage;
import org.eclipse.mosaic.fed.cell.message.StreamResult;
import org.eclipse.mosaic.fed.cell.module.CellModule;
import org.eclipse.mosaic.fed.cell.utility.NodeCapacityUtility;
import org.eclipse.mosaic.fed.cell.utility.NodeUtility;
import org.eclipse.mosaic.fed.cell.utility.RegionCapacityUtility;
import org.eclipse.mosaic.fed.cell.utility.RegionUtility;
import org.eclipse.mosaic.interactions.communication.V2xMessageAcknowledgement;
import org.eclipse.mosaic.lib.enums.ProtocolType;
import org.eclipse.mosaic.lib.objects.communication.CellConfiguration;
import org.eclipse.mosaic.lib.objects.v2x.MessageStreamRouting;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

/**
 * This generic {@link AbstractStreamModule} is the basis, and covers the tasks that are common,
 * for the {@link UpstreamModule} and {@link DownstreamModule}.
 * It includes the actual transmission and also the capacity clean up after trans.
 */
public abstract class AbstractStreamModule extends CellModule {

    private final Logger log;
    private final StreamProcessor streamProcessor;

    AbstractStreamModule(String moduleName, ChainManager chainManager, Logger log) {
        super(moduleName, chainManager);
        this.log = log;
        this.streamProcessor = new StreamProcessor(log, chainManager.getRandomNumberGenerator());
    }

    @Override
    public long getProcessedMessages() {
        return streamProcessor.getProcessedMessages();
    }

    StreamProcessor.Result doStreamProcessing(final StreamProcessor.Input inputParameters) {
        return streamProcessor.process(inputParameters);
    }

    CellModuleMessage processResult(final StreamProcessor.Input input, final StreamProcessor.Result result) {
        // Assemble first part of streamResultMessage (success, unable)
        StreamResult streamResult = new StreamResult(
                input.getRegion().id, result.getRequiredBandwidthInBps(), input.getMode(), input.getNodeId(), input.getV2xMessage()
        );

        // Set all success messages (including V2xMessages) to transmitForward.
        // In the streaming mode the stream module only waits for the arrival of the first packet (the core delay)
        // before notifying the following node. The capacity is still only freed after the streaming duration in addition to the core delay.
        if (result.isMessageProcessed()) {
            // The message was sent, therefore the capacity was consumed,
            // hence it has to be freed after the transmission.
            CellModuleMessage.Builder notifyMessageBuilder = new CellModuleMessage
                    .Builder(input.getEmittingModule(), input.getEmittingModule())
                    .startTime(input.getMessageStartTime())
                    .resource(streamResult);

            if (input.getV2xMessage().getRouting() instanceof MessageStreamRouting messageStreamRouting) {
                long streamDuration = messageStreamRouting.getStreamingDuration();
                notifyMessageBuilder.endTime(result.getMessageEndTime() + streamDuration);
            } else {
                notifyMessageBuilder.endTime(result.getMessageEndTime());
            }

            chainManager.finishEvent(notifyMessageBuilder.build());
        }

        if (result.isAcknowledged()) {
            StreamModulesDebugLogger.logSuccessfulDelivery(log, input, result);
        } else {
            if (result.isMessageProcessed()) {
                StreamModulesDebugLogger.logUnsuccessfulDelivery(log, input, result);
            } else {
                StreamModulesDebugLogger.logUnsuccessfulSending(log, input, result);
            }

            if (isTcp(input)) {
                // In case of TCP, inform sender with Nack when message is not transmittedForward
                // (when message can be transmittedForward, just transmit it without information to the sender)
                chainManager.sendInteractionToRti(
                        new V2xMessageAcknowledgement(result.getMessageEndTime(), input.getV2xMessage(), result.getNackReasons())
                );
            }
        }
        return new CellModuleMessage.Builder(input.getEmittingModule(), input.getNextModule())
                .startTime(input.getMessageStartTime())
                .endTime(result.getMessageEndTime())
                .resource(streamResult)
                .build();
    }

    private boolean isTcp(StreamProcessor.Input input) {
        return input.getV2xMessage().getRouting().getDestination().getProtocolType().equals(ProtocolType.TCP);
    }

    /**
     * Release the occupied channel capacity after message transmission (result.endTime).
     *
     * @param result Internal cell module message.
     */
    void freeBandwidth(CellModuleMessage result) {
        Validate.notNull(result, "Could not free capacity because the stream result message is null");
        if (log.isDebugEnabled()) {
            log.debug("t={}: Entering freeBandwidth() of module {}",
                    TIME.format(result.getEndTime()),
                    result.getEmittingModule());
        }
        if (!(result.getResource() instanceof StreamResult streamResult)) {
            throw new RuntimeException("Tried to free bandwidth but the resource of the result message was not a StreamResultMessage");
        }
        TransmissionMode mode = streamResult.getMode();
        freeBandwidthForNode(streamResult.getInvolvedNode(), streamResult.getConsumedBandwidth(), mode);
        CNetworkProperties region = RegionUtility.getRegionByName(streamResult.getRegionId());
        freeBandwidthForRegion(region, streamResult.getConsumedBandwidth(), mode);
    }

    /**
     * Frees the bandwidth for the node.
     *
     * @param nodeId            Id of the node.
     * @param consumedBandwidth occupied bandwidth by the node for the transmission.
     * @param mode              Transmission mode.
     */
    private void freeBandwidthForNode(String nodeId, long consumedBandwidth, TransmissionMode mode) {
        if (nodeId == null) {
            return;
        }
        CellConfiguration receiverCellConfiguration = null;
        try {
            receiverCellConfiguration = NodeUtility.getCellConfigurationOfNodeByName(nodeId);
        } catch (InternalFederateException e) {
            log.debug("Could not free the capacity for the node {}, because the node is not registered", nodeId);
        }
        if (receiverCellConfiguration != null) {
            if (mode.isUplink()) {
                NodeCapacityUtility.freeCapacityUp(receiverCellConfiguration, consumedBandwidth);
            } else if (mode.isDownlink()) {
                NodeCapacityUtility.freeCapacityDown(receiverCellConfiguration, consumedBandwidth);
            }
        }
    }

    /**
     * Frees the bandwidth for the base region.
     *
     * @param region            Base region.
     * @param consumedBandwidth Occupied bandwidth by the base region.
     * @param mode              Transmission mode.
     */
    private void freeBandwidthForRegion(CNetworkProperties region, long consumedBandwidth, TransmissionMode mode) {
        if (mode.isUplink()) {
            RegionCapacityUtility.freeCapacityUp(region, consumedBandwidth);
            log.debug(" available uplink capacity in region \"{}\" is now {} bps",
                    region.id, region.uplink.capacity);
        } else if (mode.isDownlink()) {
            RegionCapacityUtility.freeCapacityDown(region, consumedBandwidth);
            log.debug(" available downlink capacity in region \"{}\" is now {} bps",
                    region.id, region.downlink.capacity);
        } else {
            throw new RuntimeException("No matching mode while freeing bandwidth");
        }
    }
}
