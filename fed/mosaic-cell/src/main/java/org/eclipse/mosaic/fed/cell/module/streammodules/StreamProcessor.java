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

import org.eclipse.mosaic.fed.cell.config.model.CNetworkProperties;
import org.eclipse.mosaic.fed.cell.config.model.TransmissionMode;
import org.eclipse.mosaic.fed.cell.utility.CapacityUtility;
import org.eclipse.mosaic.fed.cell.utility.DelayUtility;
import org.eclipse.mosaic.fed.cell.utility.NodeCapacityUtility;
import org.eclipse.mosaic.fed.cell.utility.NodeUtility;
import org.eclipse.mosaic.fed.cell.utility.RegionCapacityUtility;
import org.eclipse.mosaic.fed.cell.utility.RetransmissionLossUtility;
import org.eclipse.mosaic.lib.enums.NegativeAckReason;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.model.transmission.TransmissionResult;
import org.eclipse.mosaic.lib.objects.communication.CellConfiguration;
import org.eclipse.mosaic.lib.objects.v2x.MessageStreamRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.rti.DATA;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the processing of a message transmission within both up- and downstream modules.
 */
public class StreamProcessor {

    private final Logger log;
    private final RandomNumberGenerator randomNumberGenerator;

    private int processedMessages;

    /**
     * Creates a new {@link StreamProcessor} object.
     *
     * @param log                   Logger object
     * @param randomNumberGenerator A random number.
     */
    StreamProcessor(Logger log, RandomNumberGenerator randomNumberGenerator) {
        this.log = log;
        this.randomNumberGenerator = randomNumberGenerator;
    }

    /**
     * Do the actual stream processing (which is equal for Up and Down).
     * The according steps are
     * <ol>
     *      <li/> Calc Core Delay
     *      <li/> Handle PacketLoss / PacketRetransmission
     *      <li/> Check Capacity (including possible increased neededBandwidth due to lossProbability)
     *      <li/> If Up -> GEO, If Down -> RTI.sendMessage (always through the ChainManager)
     * </ol>
     *
     * @return "unspecified" StreamResultMessage which should be finalized by the specific Up/Down
     */
    Result process(final Input input) {
        log.debug("Do streamProcessing for {} in region \"{}\"", input.mode, input.nodeId);
        Result result = new Result();
        result.messageEndTime = input.messageStartTime;
        result.messageProcessed = true;
        result.acknowledged = true;

        if (!isNodeConfigurationEnabled(input)) {
            result.disableProcessing(NegativeAckReason.NODE_DEACTIVATED);
        }
        if (result.isMessageProcessed()) {
            try {
                calculateTransmissionModels(input, result);
            } catch (InternalFederateException e) {
                result.disableProcessing();
            }
        }

        updateStatistics(input, result);
        return result;
    }

    private void calculateTransmissionModels(Input input, Result result) throws InternalFederateException {
        // 1) CoreDelay-model
        // * get core delay according to parameters.getMode() - constant, simple random, gammas
        final long coreDelayInNs = DelayUtility.calculateDelay(input.region, input.mode, input.nodeId, randomNumberGenerator);

        // 2) Pr/Pl-model
        // * check if packet can be transmitted and how many attempts are needed
        int prPlAttempts = calculateRetransmissionLossModel(input, result, coreDelayInNs);

        // 3) Capacity-model
        // * consume bandwidth according to delays and
        //   possibly multiple attempts (even in unsuccessful case)
        // * check if transmission does not exceed available capacity
        long neededBandwidthInBps = calculateNeededBandwidth(input, result, coreDelayInNs, prPlAttempts);
        consumeCapacity(input, result, neededBandwidthInBps);
    }

    private boolean isNodeConfigurationEnabled(Input input) {
        String nodeId = input.nodeId;
        // DownlinkMulticasts addresses multiple nodes with multiple configs
        if (input.mode.equals(TransmissionMode.DownlinkMulticast)) {
            return true;
        }
        try {
            input.nodeConfiguration = NodeUtility.getCellConfigurationOfNodeByName(nodeId);
        } catch (InternalFederateException e) {
            return false;
        }
        return input.nodeConfiguration.isEnabled();
    }

    /**
     * Attempts of packet retransmission in case of packet loss.
     *
     * @param input         Input data includes transmission related information.
     * @param result        Result of the message transmission.
     * @param coreDelayInNs Delay of the core network [ns].
     * @return Number of attempts.
     */
    private int calculateRetransmissionLossModel(Input input, Result result, long coreDelayInNs) {
        final TransmissionResult transmissionResult =
                RetransmissionLossUtility.determineTransmissionAttempts(randomNumberGenerator, input.region, input.mode);

        if (!transmissionResult.success) {
            if (input.v2xMessage.getRouting() instanceof MessageStreamRouting) {
                // MessageStreamRouting has no total loss
                transmissionResult.attempts++;
                transmissionResult.success = true;
            } else {
                StreamModulesDebugLogger.logPacketLoss(log, input, transmissionResult.attempts);
                result.unacknowledge(NegativeAckReason.PACKET_LOSS);
            }
        }
        result.messageEndTime = (input.messageStartTime + transmissionResult.attempts * coreDelayInNs);
        return transmissionResult.attempts;
    }

    /**
     * The transmission channel is limited. For this reason, the needed bandwidth
     * will be calculated depending on the protocol (Stream, Packet)
     *
     * @param input         Input data includes transmission related information.
     * @param result        Result of the message transmission.
     * @param coreDelayInNs Delay of the core network [ns].
     * @param prPlAttempts  Number of attempts for retransmission.
     * @return Bandwidth.
     */
    private long calculateNeededBandwidth(Input input, Result result, long coreDelayInNs, int prPlAttempts) {
        if (input.v2xMessage.getRouting() instanceof MessageStreamRouting) {
            return calculateNeededBandwidthStream(input, result);
        } else {
            return calculateNeededBandwidthPacket(input, result, coreDelayInNs, prPlAttempts);
        }
    }

    /**
     * Calculates the needed bandwidth for the stream transmission.
     *
     * @param input  Input data includes transmission related information.
     * @param result Result of the message transmission.
     * @return Bandwidth for the stream.
     */
    private long calculateNeededBandwidthStream(Input input, Result result) {
        long neededBandwidth = CapacityUtility.getStreamingBandwidth(input.v2xMessage);

        if (!(RegionCapacityUtility.isCapacitySufficient(input.mode, input.region, neededBandwidth))) {
            result.disableProcessing(NegativeAckReason.CHANNEL_CAPACITY_EXCEEDED);
        }
        if (!(NodeCapacityUtility.isCapacitySufficient(input.mode, input.nodeConfiguration, neededBandwidth))) {
            result.disableProcessing(NegativeAckReason.NODE_CAPACITY_EXCEEDED);
        }
        return neededBandwidth;
    }

    /**
     * Calculates the needed bandwidth for the packet-oriented protocols.
     *
     * @param input         Input data includes transmission related information.
     * @param result        Result of the message transmission.
     * @param coreDelayInNs Delay of the core network [ns].
     * @param prPlAttempts  Number of attempts for retransmission.
     * @return Bandwidth for the data packets.
     */
    private long calculateNeededBandwidthPacket(Input input, Result result, long coreDelayInNs, int prPlAttempts) {
        long messageSize = CapacityUtility.getMessageLength(input.v2xMessage);
        long neededBandwidth = prPlAttempts * CapacityUtility.calculateNeededCapacity(messageSize, coreDelayInNs) * DATA.BIT;

        // When the bandwidth is sufficient go on and send the packet
        if (!CapacityUtility.isCapacitySufficient(input.mode, input.region, input.nodeConfiguration, neededBandwidth)) {
            // FIXME: maybe add log for slower transmission
            // When the bandwidth is not sufficient, check whether enough bandwidth is available to start a new transmission
            if (CapacityUtility.isAvailable(input.mode, input.region, input.nodeConfiguration)) {
                // Adapt the delay/the bandwidth and send packet
                long availableBandwidthInBps = CapacityUtility.availableCapacity(input.mode, input.region, input.nodeConfiguration);
                long actualDelayInNs = CapacityUtility.calculateNeededDelay(messageSize * prPlAttempts, availableBandwidthInBps);
                neededBandwidth = availableBandwidthInBps;
                result.messageEndTime = input.messageStartTime + actualDelayInNs;
            } else {
                StreamModulesDebugLogger.logChannelCapacityExceeded(log, input, result.messageEndTime, neededBandwidth);
                // Drop packet since not enough bandwidth is available
                result.disableProcessing();
                if (!RegionCapacityUtility.isAvailable(input.mode, input.region)) {
                    result.nackReasons.add(NegativeAckReason.CHANNEL_CAPACITY_EXCEEDED);
                }
                if (!NodeCapacityUtility.isAvailable(input.mode, input.nodeConfiguration)) {
                    result.nackReasons.add(NegativeAckReason.NODE_CAPACITY_EXCEEDED);
                }
            }
        }
        return neededBandwidth;
    }

    /**
     * Adds the consumed capacity for the transmission to {@link Result}.
     *
     * @param input           Input data includes transmission related information.
     * @param result          Result of the message transmission.
     * @param neededBandwidth Needed bandwidth for the transmission.
     */
    private void consumeCapacity(Input input, Result result, long neededBandwidth) {
        if (result.messageProcessed) {
            CapacityUtility.consumeCapacity(input.mode, input.region, input.nodeConfiguration, neededBandwidth);
            result.requiredBandwidth = neededBandwidth;
        }
    }

    /**
     * Updates statistics for the transmission.
     *
     * @param input  Input data includes transmission related information.
     * @param result Result of the message transmission.
     */
    private void updateStatistics(Input input, Result result) {
        if (log.isTraceEnabled()) {
            log.trace("Calculated total {}-delay for message in region \"{}\": {} ns",
                    input.mode, input.region.id, result.messageEndTime - input.messageStartTime);
        }
        ++processedMessages;
    }

    /**
     * Get number of processed messages.
     *
     * @return Number of processed messages.
     */
    int getProcessedMessages() {
        return processedMessages;
    }

    /**
     * Helper class to summarize transmission related information in an object
     * to be used as input for {@link #process}.
     */
    static class Input {
        /**
         * Start time of the message.
         */
        private long messageStartTime;
        /**
         * Message to be transmitted.
         */
        private V2xMessage v2xMessage;
        /**
         * Mode of transmission.
         */
        private TransmissionMode mode;

        /**
         * Node id to be used for transmission, this can be the sending or receiving
         * node depending on the used module.
         */
        private String nodeId;
        /**
         * Region to be used for the transmission, depends on used module.
         */
        private CNetworkProperties region;
        /**
         * Configuration of the node.
         */
        private CellConfiguration nodeConfiguration;

        /**
         * Current module.
         */
        private String emittingModule;
        /**
         * Next module.
         */
        private String nextModule;

        Input message(long startTime, V2xMessage message, TransmissionMode mode) {
            this.messageStartTime = startTime;
            this.v2xMessage = message;
            this.mode = mode;
            return this;
        }

        Input node(String nodeId, CNetworkProperties region) {
            this.nodeId = nodeId;
            this.region = region;
            return this;
        }

        Input module(String emittingModule, String nextModule) {
            this.emittingModule = emittingModule;
            this.nextModule = nextModule;
            return this;
        }

        long getMessageStartTime() {
            return messageStartTime;
        }

        V2xMessage getV2xMessage() {
            return v2xMessage;
        }

        TransmissionMode getMode() {
            return mode;
        }

        String getNodeId() {
            return nodeId;
        }

        CellConfiguration getNodeConfiguration() {
            return nodeConfiguration;
        }

        CNetworkProperties getRegion() {
            return region;
        }

        String getEmittingModule() {
            return emittingModule;
        }

        String getNextModule() {
            return nextModule;
        }
    }

    /**
     * Helper class to summarize the message process in an object,
     * output of {@link #process}.
     */
    static class Result {
        /**
         * Flag indicating if message could be processed. See {@link #process} for details.
         */
        private boolean messageProcessed;
        /**
         * Flag indicating whether message was acknowledged. See {@link #process} for details.
         */
        private boolean acknowledged;
        /**
         * List of reasons for why message wasn't acknowledged.
         */
        private final List<NegativeAckReason> nackReasons = new ArrayList<>();
        /**
         * End time of the message.
         */
        private long messageEndTime;
        /**
         * The required bandwidth for the message.
         */
        private long requiredBandwidth;

        boolean isMessageProcessed() {
            return messageProcessed;
        }

        boolean isAcknowledged() {
            return acknowledged;
        }

        List<NegativeAckReason> getNackReasons() {
            return nackReasons;
        }

        long getMessageEndTime() {
            return messageEndTime;
        }

        long getRequiredBandwidthInBps() {
            return requiredBandwidth;
        }

        public void disableProcessing(NegativeAckReason negativeAckReason) {
            disableProcessing();
            nackReasons.add(negativeAckReason);
        }

        public void disableProcessing() {
            messageProcessed = false;
            acknowledged = false;
        }

        void unacknowledge(NegativeAckReason negativeAckReason) {
            acknowledged = false;
            nackReasons.add(negativeAckReason);
        }
    }
}
