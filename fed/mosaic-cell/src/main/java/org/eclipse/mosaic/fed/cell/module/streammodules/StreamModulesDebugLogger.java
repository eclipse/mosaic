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

import org.eclipse.mosaic.fed.cell.config.model.TransmissionMode;
import org.eclipse.mosaic.fed.cell.utility.CapacityUtility;
import org.eclipse.mosaic.fed.cell.utility.NodeCapacityUtility;

import org.slf4j.Logger;

/**
 * Helper class providing various methods to log extensive information about processed messages.
 * All messages are only available for log level lower or equals DEBUG
 */
class StreamModulesDebugLogger {

    /**
     * Logs if packet got lost.
     *
     * @param log      Logs packet loss.
     * @param input    Input data includes transmission related information.
     * @param attempts Attempts to send the packet.
     */
    static void logPacketLoss(Logger log, StreamProcessor.Input input, int attempts) {
        if (!log.isDebugEnabled()) {
            return;
        }

        final String senderId = input.getV2xMessage().getRouting().getSource().getSourceName();
        log.debug(" msg-{} IS NOT deliverable via {} in region \"{}\" "
                        + "due to packet loss during {} attempts. Notifying the sending node {}",
                input.getV2xMessage().getId(), input.getMode(), input.getRegion().id,
                attempts, senderId);
    }

    /**
     * Logs if the transmission data exceeds the channel capacity.
     *
     * @param log            Logs exceeded channel capacity.
     * @param input          Input data includes transmission related information.
     * @param messageEndTime End time of the message.
     * @param neededBw       Needed Bandwidth for the transmission.
     */
    static void logChannelCapacityExceeded(Logger log, StreamProcessor.Input input, long messageEndTime, long neededBw) {
        if (!log.isDebugEnabled()) {
            return;
        }

        long delayInNs = (messageEndTime - input.getMessageStartTime());
        long msgLenInBit = CapacityUtility.getMessageLengthWithHeaders(input.getV2xMessage());
        int msgId = input.getV2xMessage().getId();
        final String senderId = input.getV2xMessage().getRouting().getSource().getSourceName();
        if (input.getMode().isUplink()) {
            log.debug(" msg-{} IS NOT deliverable via {} in region \"{}\" "
                            + "due to exceeded capacity "
                            + "(with msgSize={} bit, delay={} ns, neededBandwidth={} bps, "
                            + "regionCapacityLeft={} bps, nodeId={}, nodeCapacityLeft={})."
                            + "(For TCP, notifying the sending node {})",
                    msgId, input.getMode(), input.getRegion().id, msgLenInBit, delayInNs, neededBw,
                    input.getRegion().uplink.capacity, input.getNodeId(),
                    NodeCapacityUtility.getAvailableUlCapacity(input.getNodeConfiguration()), senderId);
        } else if (input.getMode().equals(TransmissionMode.DownlinkUnicast)) {
            log.debug(" msg-{} IS NOT deliverable via {} in region \"{}\" "
                            + "due to exceeded capacity "
                            + "(with msgSize={} bit, delay={} ns, neededBandwidth={} bps, "
                            + "capacityLeft={} bps, nodeId={}, nodeCapacityLeft={})."
                            + "(For TCP, notifying the sending node {})",
                    msgId, input.getMode(), input.getRegion().id,
                    msgLenInBit, delayInNs, neededBw, input.getRegion().downlink.capacity, input.getNodeId(),
                    NodeCapacityUtility.getAvailableDownlinkCapacity(input.getNodeConfiguration()),
                    senderId);
        } else if (input.getMode().equals(TransmissionMode.DownlinkMulticast)) {
            log.debug(" msg-{} IS NOT deliverable via {} in region \"{}\" "
                            + "due to exceeded capacity "
                            + "(with msgSize={} bit, delay={} ns, neededBandwidth={} bps, capacityLeft={} bps)."
                            + "(For TCP, notifying the sending node {})",
                    msgId, input.getMode(), input.getRegion().id,
                    msgLenInBit, delayInNs, neededBw, input.getRegion().downlink.capacity,
                    senderId);
        }
    }

    /**
     * Logs if the message is successful delivered.
     *
     * @param log    Logs data about successful delivery.
     * @param input  Input data includes transmission related information.
     * @param result Result of the sent message.
     */
    static void logSuccessfulDelivery(Logger log, StreamProcessor.Input input, StreamProcessor.Result result) {
        if (!log.isDebugEnabled()) {
            return;
        }

        long delayInNs = (result.getMessageEndTime() - input.getMessageStartTime());
        long msgLenInBit = CapacityUtility.getMessageLengthWithHeaders(input.getV2xMessage());
        int msgId = input.getV2xMessage().getId();
        if (input.getMode().isUplink()) {
            log.debug(" msg-{} IS deliverable via {} in region \"{}\" "
                            + "(with msgSize={} bit, delay={} ns, neededBandwidth={} bps, capacityLeft={} bps, nodeCapacityLeft={})",
                    msgId, input.getMode(), input.getRegion().id,
                    msgLenInBit, delayInNs, result.getRequiredBandwidthInBps(), input.getRegion().uplink.capacity,
                    NodeCapacityUtility.getAvailableUlCapacity(input.getNodeConfiguration()));
        } else if (input.getMode().equals(TransmissionMode.DownlinkUnicast)) {
            log.debug(" msg-{} IS deliverable via {} in region \"{}\" "
                            + "(with msgSize={} bit, delay={} ns, neededBandwidth={} bps, "
                            + "capacityLeft={} bps, nodeId={}, nodeCapacityLeft={})",
                    msgId, input.getMode(), input.getRegion().id,
                    msgLenInBit, delayInNs, result.getRequiredBandwidthInBps(), input.getRegion().downlink.capacity,
                    input.getNodeId(), NodeCapacityUtility.getAvailableDownlinkCapacity(input.getNodeConfiguration()));
        } else if (input.getMode().equals(TransmissionMode.DownlinkMulticast)) {
            log.debug(" msg-{} IS deliverable via {} in region \"{}\" "
                            + "(with msgSize={} bit, delay={} ns, neededBandwidth={} bps, capacityLeft={} bps)",
                    msgId, input.getMode(), input.getRegion().id,
                    msgLenInBit, delayInNs, result.getRequiredBandwidthInBps(), input.getRegion().downlink.capacity);
        }
    }

    /**
     * Logs in case that the message delivery is unsuccessful.
     *
     * @param log    Logs data about unsuccessful sending.
     * @param input  Input data includes transmission related information.
     * @param result Result of the message transmission.
     */
    static void logUnsuccessfulSending(Logger log, StreamProcessor.Input input, StreamProcessor.Result result) {
        if (!log.isDebugEnabled()) {
            return;
        }

        if (input.getNodeConfiguration() == null) {
            logUnsuccessfulSendingWithoutNodeConfig(log, input, result);
        } else {
            logUnsuccessfulSendingWithNodeConfig(log, input, result);
        }
    }

    /**
     * Logs unsuccessful message sending because of missing node configuration.
     *
     * @param log    Logs data about unsuccessful sending.
     * @param input  Input data includes transmission related information.
     * @param result Result of the message transmission.
     */
    private static void logUnsuccessfulSendingWithoutNodeConfig(Logger log, StreamProcessor.Input input, StreamProcessor.Result result) {
        long delayInNs = (result.getMessageEndTime() - input.getMessageStartTime());
        long msgLenInBit = CapacityUtility.getMessageLengthWithHeaders(input.getV2xMessage());
        int msgId = input.getV2xMessage().getId();
        if (input.getMode().isUplink()) {
            log.debug(" msg-{} is not sendable via {} in region \"{}\" due to {}"
                            + "(with msgSize={} bit, delay={} ns, neededBandwidth={} bps, "
                            + "capacityLeft={} bps, the node cell configuration is null)",
                    msgId, input.getMode(), input.getRegion().id, result.getNackReasons(),
                    msgLenInBit, delayInNs, result.getRequiredBandwidthInBps(), input.getRegion().uplink.capacity);
        } else if (input.getMode().equals(TransmissionMode.DownlinkUnicast)) {
            log.debug(" msg-{} is not sendable via {} in region \"{}\" due to {}"
                            + "(with msgSize={} bit, delay={} ns, neededBandwidth={} bps, "
                            + "capacityLeft={} bps, the node cell configuration is null)",
                    msgId, input.getMode(), input.getRegion().id, result.getNackReasons(),
                    msgLenInBit, delayInNs, result.getRequiredBandwidthInBps(), input.getRegion().downlink.capacity);
        } else if (input.getMode().equals(TransmissionMode.DownlinkMulticast)) {
            log.debug(" msg-{} is not sendable via {} in region \"{}\" due to {}"
                            + "(with msgSize={} bit, delay={} ns, neededBandwidth={} bps, capacityLeft={} bps)",
                    msgId, input.getMode(), input.getRegion().id, result.getNackReasons(),
                    msgLenInBit, delayInNs, result.getRequiredBandwidthInBps(), input.getRegion().downlink.capacity);
        }
    }

    /**
     * Logs unsuccessful message sending if node configuration existing.
     *
     * @param log    Logs data about unsuccessful sending.
     * @param input  Input data includes transmission related information.
     * @param result Result of the message transmission.
     */
    private static void logUnsuccessfulSendingWithNodeConfig(Logger log, StreamProcessor.Input input, StreamProcessor.Result result) {
        long delayInNs = (result.getMessageEndTime() - input.getMessageStartTime());
        long msgLenInBit = CapacityUtility.getMessageLengthWithHeaders(input.getV2xMessage());
        int msgId = input.getV2xMessage().getId();
        if (input.getMode().isUplink()) {
            log.debug(" msg-{} is not sendable via {} in region \"{}\" due to {}"
                            + "(with msgSize={} bit, delay={} ns, neededBandwidth={} bps, "
                            + "capacityLeft={} bps, nodeId={}, nodeCapacityLeft={})",
                    msgId, input.getMode(), input.getRegion().id, result.getNackReasons(),
                    msgLenInBit, delayInNs, result.getRequiredBandwidthInBps(), input.getRegion().uplink.capacity, input.getNodeId(),
                    NodeCapacityUtility.getAvailableUlCapacity(input.getNodeConfiguration()));
        } else if (input.getMode().equals(TransmissionMode.DownlinkUnicast)) {
            log.debug(" msg-{} is not sendable via {} in region \"{}\" due to {}"
                            + "(with msgSize={} bit, delay={} ns, neededBandwidth={} bps, "
                            + "capacityLeft={} bps, nodeId={}, nodeCapacityLeft={})",
                    msgId, input.getMode(), input.getRegion().id, result.getNackReasons(),
                    msgLenInBit, delayInNs, result.getRequiredBandwidthInBps(), input.getRegion().downlink.capacity, input.getNodeId(),
                    NodeCapacityUtility.getAvailableDownlinkCapacity(input.getNodeConfiguration()));
        } else if (input.getMode().equals(TransmissionMode.DownlinkMulticast)) {
            log.debug(" msg-{} is not sendable via {} in region \"{}\" due to {}"
                            + "(with msgSize={} bit, delay={} ns, neededBandwidth={} bps, capacityLeft={} bps)",
                    msgId, input.getMode(), input.getRegion().id, result.getNackReasons(),
                    msgLenInBit, delayInNs, result.getRequiredBandwidthInBps(), input.getRegion().downlink.capacity);
        }
    }

    /**
     * Logs unsuccessful message delivery to a specific region.
     *
     * @param log    Logs unsuccessful message delivery.
     * @param input  Input data includes transmission related information.
     * @param result Result of the message transmission.
     */
    static void logUnsuccessfulDelivery(Logger log, StreamProcessor.Input input, StreamProcessor.Result result) {
        if (!log.isDebugEnabled()) {
            return;
        }

        long delayInNs = (result.getMessageEndTime() - input.getMessageStartTime());
        long msgLenInBit = CapacityUtility.getMessageLengthWithHeaders(input.getV2xMessage());
        int msgId = input.getV2xMessage().getId();
        if (input.getMode().isUplink()) {
            log.debug(" msg-{} IS NOT deliverable via {} in region \"{}\" "
                            + "(with msgSize={} bit, delay={} ns, neededBandwidth={} bps, "
                            + "capacityLeft={} bps, nodeId={}, nodeCapacityLeft={})",
                    msgId, input.getMode(), input.getRegion().id,
                    msgLenInBit, delayInNs, result.getRequiredBandwidthInBps(), input.getRegion().uplink.capacity, input.getNodeId(),
                    NodeCapacityUtility.getAvailableUlCapacity(input.getNodeConfiguration()));
        } else if (input.getMode().equals(TransmissionMode.DownlinkUnicast)) {
            log.debug(" msg-{} IS NOT deliverable via {} in region \"{}\" "
                            + "(with msgSize={} bit, delay={} ns, neededBandwidth={} bps, "
                            + "capacityLeft={} bps, nodeId={}, nodeCapacityLeft={})",
                    msgId, input.getMode(), input.getRegion().id,
                    msgLenInBit, delayInNs, result.getRequiredBandwidthInBps(), input.getRegion().downlink.capacity, input.getNodeId(),
                    NodeCapacityUtility.getAvailableDownlinkCapacity(input.getNodeConfiguration()));
        } else if (input.getMode().equals(TransmissionMode.DownlinkMulticast)) {
            log.debug(" msg-{} IS NOT deliverable via {} in region \"{}\" "
                            + "(with msgSize={} bit, delay={} ns, neededBandwidth={} bps, capacityLeft={} bps)",
                    msgId, input.getMode(), input.getRegion().id,
                    msgLenInBit, delayInNs, result.getRequiredBandwidthInBps(), input.getRegion().downlink.capacity);
        }
    }
}
