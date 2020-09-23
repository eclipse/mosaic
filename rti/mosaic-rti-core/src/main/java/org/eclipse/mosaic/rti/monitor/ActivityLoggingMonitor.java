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
 */

package org.eclipse.mosaic.rti.monitor;

import org.eclipse.mosaic.rti.api.FederateAmbassador;
import org.eclipse.mosaic.rti.api.FederationManagement;
import org.eclipse.mosaic.rti.api.Interaction;
import org.eclipse.mosaic.rti.api.Monitor;
import org.eclipse.mosaic.rti.api.TimeManagement;
import org.eclipse.mosaic.rti.api.time.FederateEvent;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ActivityLoggingMonitor implements Monitor {

    /**
     * Logger for writing out the activity logs.
     */
    protected final Logger statLog;

    /**
     * Map for counting logged messages by type id.
     */
    private final Map<String, Integer> messageCounter = new HashMap<>();

    private final LinkedList<FederateEventDetails> eventList = new LinkedList<>();

    public ActivityLoggingMonitor(Logger activityLogger) {
        statLog = activityLogger;
    }

    @Override
    public void onBeginSimulation(FederationManagement fm, TimeManagement tm, int numberOfThreads) {
        statLog.info(
                "SIM;{};{};{};{}",
                0,
                fm.getFederationId(),
                tm.getClass().getSimpleName(),
                numberOfThreads);

        for (FederateAmbassador federate : fm.getAmbassadors()) {
            boolean timeConstrained = federate.isTimeConstrained();
            boolean timeRegulating = federate.isTimeRegulating();
            statLog.info(
                    "FED;{};{};{};",
                    federate.getId(),
                    timeConstrained,
                    timeRegulating);
        }
    }

    @Override
    public void onEndSimulation(FederationManagement fm, TimeManagement tm, long duration, int statusCode) {
        statLog.info(
                "SIM;{};{};D:{}",
                tm.getEndTime(),
                fm.getFederationId(),
                duration);

        printStatisticsInfo();
    }

    @Override
    public void onBeginActivity(FederateEvent event) {
        statLog.info(
                "EVT;{};{};id={}",
                event.getRequestedTime(),
                event.getFederateId(),
                event.getId());
    }

    @Override
    public void onEndActivity(FederateEvent event, long duration) {
        String fedId = event.getFederateId();

        eventList.push(new FederateEventDetails(event.getRequestedTime(), fedId, duration));
        statLog.info(
                "EVT;{};{};D:{};id={}",
                event.getRequestedTime(),
                event.getFederateId(),
                duration,
                event.getId()
        );
    }

    @Override
    public void onInteraction(Interaction interaction) {
        /* Special handling for dummy messages since they have their type coded
         * in a class variable (messageId) */
        String key = interaction.getTypeId();
        Integer count = messageCounter.get(key);
        messageCounter.put(key, count != null ? count + 1 : 1);

        this.statLog.info(
                "MSG;{};{};FROM {};id={};hash={}",
                interaction.getTime(),
                interaction.getTypeId(),
                interaction.getSenderId(),
                interaction.getId(),
                String.format("%08X", interaction.hashCode()));
    }

    @Override
    public void onReceiveInteraction(String receiver, Interaction interaction) {
        statLog.info(
                "MSG;{};{};TO {};id={};hash={}",
                interaction.getTime(),
                interaction.getTypeId(), receiver,
                interaction.getId(),
                String.format("%08X", interaction.hashCode()));
    }

    @Override
    public void onProcessInteraction(String federate, Interaction interaction) {
        statLog.info(
                "MSG;{};{};AT {};id={};hash={}",
                interaction.getTime(),
                interaction.getTypeId(), federate,
                interaction.getId(),
                String.format("%08X", interaction.hashCode()));
    }

    @Override
    public void onScheduling(int id, FederateEvent event) {
        statLog.info(
                "PRL;{};{};{};{};{}",
                id,
                event.getId(),
                event.getFederateId(),
                event.getRequestedTime(),
                event.getLookahead());
    }

    private void printStatisticsInfo() {
        statLog.info("Simulation ended. Statistics:");
        // Calculate average event values
        long minDistance = Integer.MAX_VALUE;
        long maxDistance = Integer.MIN_VALUE;
        double avgDistance = 0;
        // long avgDuration = 0;
        double total = eventList.size();
        Map<String, List<Long>> federateEventDurations = new HashMap<>();
        FederateEventDetails lastEvent = eventList.pollLast();
        FederateEventDetails currentEvent = lastEvent;
        while (currentEvent != null) {
            long diff = currentEvent.time - lastEvent.time;
            minDistance = Math.min(diff, minDistance);
            maxDistance = Math.max(diff, maxDistance);
            avgDistance += diff / total;

            long currentDuration = currentEvent.duration;

            // Save each event duration per federate
            String currentFederate = currentEvent.federate;
            List<Long> durationList = federateEventDurations.computeIfAbsent(currentFederate, k -> new ArrayList<>());

            durationList.add(currentDuration);

            lastEvent = currentEvent;
            currentEvent = eventList.pollLast();
        }

        // ////////////////////////////////////////////////// Event information
        statLog.info("Event Details: ");
        statLog.info("Federate;avgEventDuration;FederateEvents");
        for (Entry<String, List<Long>> entry : federateEventDurations.entrySet()) {
            List<Long> currentDurations = entry.getValue();
            int durationSize = currentDurations.size();
            double average = 0;
            for (long duration : currentDurations) {
                average += ((double) duration / durationSize);
            }
            statLog.info("{};{};{}", entry.getKey(), average, durationSize);
        }

        statLog.info("Minimum event distance: {}", minDistance);
        statLog.info("Maximum event distance: {}", maxDistance);
        statLog.info("Average event distance: {}", avgDistance);

        // //////////////////////////////////////// Message passing information
        statLog.info("Message Counts (sent):");
        statLog.info("MessageType;MessageCount");
        for (Entry<String, Integer> msg : messageCounter.entrySet()) {
            statLog.info("{};{}",
                    msg.getKey(),
                    msg.getValue()
            );
        }
    }

    private static class FederateEventDetails {

        private final long time;
        private final String federate;
        private final long duration;

        public FederateEventDetails(long time, String federate, long duration) {
            this.time = time;
            this.federate = federate;
            this.duration = duration;
        }
    }
}
