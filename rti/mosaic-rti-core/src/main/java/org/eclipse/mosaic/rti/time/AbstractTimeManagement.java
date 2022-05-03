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

package org.eclipse.mosaic.rti.time;

import org.eclipse.mosaic.lib.util.EfficientPriorityQueue;
import org.eclipse.mosaic.lib.util.PerformanceMonitor;
import org.eclipse.mosaic.rti.ExternalWatchDog;
import org.eclipse.mosaic.rti.MosaicComponentParameters;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.WatchDogThread;
import org.eclipse.mosaic.rti.api.ComponentProvider;
import org.eclipse.mosaic.rti.api.FederateAmbassador;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.TimeManagement;
import org.eclipse.mosaic.rti.api.WatchDog;
import org.eclipse.mosaic.rti.api.time.FederateEvent;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;
import java.util.Queue;
import javax.annotation.Nonnull;

/**
 * Abstract class providing base functions for initialization, progress printing and finishing of
 * {@link TimeManagement} implementations.
 */
public abstract class AbstractTimeManagement extends Observable implements TimeManagement {

    /**
     * Prevent the bottleneck issues by frequent logging. The minimum time difference.
     * Unit: [ms]. Set to <code>0</code> to disable this feature.
     */
    private final static long SIMULATION_INFO_LOG_INTERVAL = 500 * TIME.MILLI_SECOND;

    protected final static int STATUS_CODE_SUCCESS = 101;

    protected static final DecimalFormat FORMAT_ONE_DIGIT = new DecimalFormat("#0.0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    protected static final DecimalFormat FORMAT_TWO_DIGIT = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    protected final Logger logger;
    private final Logger progressLogger;

    private long lastLogTime = 0;

    private long simStartRealtimeNs;

    /**
     * Ordered queue holding all requested times of federates.
     * TODO: the ordering of this queue has to be revisited:
     * <ul>
     *     <li/> {@link org.eclipse.mosaic.rti.api.parameters.FederatePriority} is used as reference values
     *     <li/> {@link FederateEvent#compareTo} orders events reversed as described FederatePriority
     *     <li/> maybe it's enough to just reverse the compareTo logic
     * </ul>
     */
    protected final Queue<FederateEvent> events;

    protected final ComponentProvider federation;

    /**
     * The end time of the simulation.
     */
    private final long endTime;

    /**
     * The current simulation time of the federation in nanoseconds.
     */
    protected long time = -1;

    protected WatchDog watchDog;
    protected ExternalWatchDog externalWatchDog;

    protected AbstractTimeManagement(ComponentProvider federation, MosaicComponentParameters componentParameters) {
        this.progressLogger = LoggerFactory.getLogger("SimulationProgress");
        this.logger = LoggerFactory.getLogger(getClass());
        this.events = new EfficientPriorityQueue<>();
        this.federation = federation;
        this.endTime = componentParameters.getEndTime();
    }

    @Override
    public long getSimulationTime() {
        return this.time;
    }

    @Override
    public long getEndTime() {
        return this.endTime;
    }

    @Override
    public void requestAdvanceTime(String federateId, long time, long lookahead, byte priority) throws IllegalValueException {
        if (time < this.time) {
            throw new IllegalValueException(String.format(
                    "The federate '%s' requested a time (%d) which is already in the past (current time is: %d)",
                    federateId, time, this.time
            ));
        }
        synchronized (this.events) {
            FederateEvent e = new FederateEvent(federateId, time, lookahead, priority);
            if (!this.events.contains(e)) {
                this.events.add(e);
            }
        }
    }

    /**
     * The method is called once before the simulation is started. It calls the
     * initialize method of all joined federates.
     *
     * @throws IllegalValueException     a parameter has an invalid value
     * @throws InternalFederateException an exception inside of a joined federate occurs
     */
    protected void prepareSimulationRun() throws IllegalValueException, InternalFederateException {
        // check time parameters
        if (this.getEndTime() < 0) {
            throw new IllegalValueException("Invalid end time.");
        }

        // advance to start time
        this.time = 0;

        // schedule start event for each federate
        Collection<FederateAmbassador> ambassadors = federation.getFederationManagement().getAmbassadors();
        for (FederateAmbassador fed : ambassadors) {
            fed.initialize(0, getEndTime());
        }

        simStartRealtimeNs = System.nanoTime();
    }

    @Override
    public void finishSimulationRun(int statusCode) throws InternalFederateException {
        long durationMs = simStartRealtimeNs > 0
                ? (System.nanoTime() - simStartRealtimeNs) / TIME.MILLI_SECOND
                : 0;

        try {
            this.stopWatchDog();
            for (FederateAmbassador fed : federation.getFederationManagement().getAmbassadors()) {
                fed.finishSimulation();
            }
        } finally {
            PerformanceMonitor.getInstance().logSummary(logger);
            // always print simulation finished even if federate throws exception on finishing
            printSimulationFinished(durationMs, statusCode);
            federation.getMonitor().onEndSimulation(federation.getFederationManagement(), this, durationMs, statusCode);
        }
    }

    private void printSimulationFinished(long durationMs, int statusCode) {
        //Please leave the blanks there. They override the line completely.
        progressLogger.info("Simulating: {}ns ({}s) - {}%                                         \r",
                time,
                FORMAT_ONE_DIGIT.format(time / (double) TIME.SECOND),
                FORMAT_ONE_DIGIT.format((time * 100d) / getEndTime())
        );

        progressLogger.info(System.lineSeparator());

        final long currentTime = System.currentTimeMillis();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        logger.info("Simulation ended after {}s of {}s ({}%)", time / TIME.SECOND, getEndTime() / TIME.SECOND, (time * 100) / getEndTime());
        logger.info("Started: " + dateFormat.format(new Date(currentTime - durationMs)));
        logger.info("Ended: " + dateFormat.format(new Date(currentTime)));
        logger.info("Duration: {} (RTF: {})",
                DurationFormatUtils.formatDuration(durationMs, "HH'h' mm'm' ss.SSS's'"),
                durationMs > 0 ? FORMAT_TWO_DIGIT.format((time / TIME.MILLI_SECOND) / durationMs) : 0
        );
        logger.info("");
        if (statusCode == STATUS_CODE_SUCCESS) {
            logger.info("Simulation finished: {}", statusCode);
        } else {
            logger.info("Simulation interrupted: {}", statusCode);
        }
    }



    @Override
    public long getNextEventTimestamp() throws IllegalValueException {
        if (events.peek() != null) {
            return events.peek().getRequestedTime();
        } else {
            throw new IllegalValueException("No next event in queue.");
        }
    }

    @Nonnull
    @Override
    public WatchDog startWatchDog(String simId, int maxIdleTime) {
        watchDog = new WatchDogThread(federation, maxIdleTime);
        watchDog.start();
        return watchDog;
    }

    private void stopWatchDog() {
        if (watchDog != null) {
            watchDog.stopWatching();
        }
    }

    @Override
    public void updateWatchDog() {
        if (watchDog != null) {
            watchDog.updateCurrentTime();
        }
    }

    @Override
    public void startExternalWatchDog(String simId, int port) {
        if (port > 0) {
            progressLogger.info("" + port);
            externalWatchDog = new ExternalWatchDog(federation, port);
            externalWatchDog.start();
        }
    }

    protected void printProgress(long currentRealTimeNs, PerformanceInformation performanceInformation) {
        if ((currentRealTimeNs - lastLogTime) > SIMULATION_INFO_LOG_INTERVAL) {

            double seconds = time / (double) TIME.SECOND;
            double percent = (time * 100d) / endTime;

            lastLogTime = currentRealTimeNs;
            //Please leave the blank there. They override the line completely.
            progressLogger.info("Simulating: {}ns ({}s / {}s) - {}% (RTF:{}, ETC:{})                        \r",
                    time,
                    FORMAT_ONE_DIGIT.format(seconds),
                    FORMAT_ONE_DIGIT.format(endTime / (double) TIME.SECOND),
                    FORMAT_ONE_DIGIT.format(percent),
                    FORMAT_TWO_DIGIT.format(performanceInformation.realTimeFactor),
                    prettyTime(performanceInformation.estimatedTimeToCompletion, FORMAT_ONE_DIGIT)
            );
        }
    }

    /**
     * Format the time in long to produce a string. Depending on the input time value, the following units are possible:
     * s: second
     * m: minute
     * h: hour
     * d: day
     * mo: month
     * y: year
     *
     * @param seconds the time to format
     * @param f       formats decimal number
     * @return the decimal formatted String
     */
    private String prettyTime(double seconds, DecimalFormat f) {
        // recalculate etc based on duration
        if (seconds < 0) {
            return "unknown";
        } else if (seconds >= 0 && seconds < 120) {
            return f.format(seconds) + "s";
        } else if (seconds >= 120 && seconds < 7200) {
            return f.format(seconds / 60f) + "m";
        } else if (seconds >= 7200 && seconds < 48 * 3600) {
            return f.format(seconds / 3600f) + "h";
        } else if (seconds >= 3600 * 48 && seconds < 3600 * 24 * 60) {
            return f.format(seconds / (3600 * 24f)) + "d";
        } else if (seconds >= 3600 * 24 * 60 && seconds < 3600 * 24 * 720) {
            return f.format(seconds / (3600 * 24f * 30)) + "mo";
        } else if (seconds >= 3600 * 24 * 720) {
            return f.format(seconds / (3600 * 24f * 365)) + "y";
        }
        return "unknown";
    }

    protected static class PerformanceCalculator {

        private double passedSimulationTimeSinceLogNs = 0;
        private long passedRealtimeSinceLogNs = 0;
        private long lastSimTime = 0;
        private long lastRealTimeNs = 0;

        private PerformanceInformation performance = new PerformanceInformation();

        protected PerformanceInformation update(long simulationTime, long simulationEndTime, long realtimeNanoseconds) {
            // calculate average RTF and ETC
            passedRealtimeSinceLogNs += (realtimeNanoseconds - lastRealTimeNs);
            lastRealTimeNs = realtimeNanoseconds;

            passedSimulationTimeSinceLogNs += (simulationTime - lastSimTime);
            lastSimTime = simulationTime;

            // check performance for the last 5 seconds
            if (passedRealtimeSinceLogNs > 5 * TIME.SECOND) {
                performance.realTimeFactor = passedSimulationTimeSinceLogNs / (double) passedRealtimeSinceLogNs;

                long remainingTime = simulationEndTime - simulationTime;
                performance.estimatedTimeToCompletion = (remainingTime / (double) TIME.SECOND) / performance.realTimeFactor;
                // unknown, if > 100y
                if (performance.estimatedTimeToCompletion > 3600 * 24f * 365 * 100) {
                    performance.estimatedTimeToCompletion = -1d;
                }
                passedSimulationTimeSinceLogNs = 0;
                passedRealtimeSinceLogNs = 0;
            }
            return performance;
        }
    }
}
