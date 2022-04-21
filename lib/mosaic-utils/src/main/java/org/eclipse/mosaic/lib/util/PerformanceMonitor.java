/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.util;

import org.eclipse.mosaic.rti.TIME;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerformanceMonitor {

    private final static PerformanceMonitor INSTANCE = new PerformanceMonitor(LoggerFactory.getLogger("performance"));

    public static PerformanceMonitor getInstance() {
        return INSTANCE;
    }

    private final Map<String, MeasurementAggregation> storedMeasurements = new HashMap<>();
    private final Logger detailsLog;

    public PerformanceMonitor(Logger detailsLog) {
        this.detailsLog = detailsLog;
    }

    public Measurement start(String name) {
        return new Measurement(name);
    }

    private void store(Measurement measurement) {
        MeasurementAggregation aggregation = storedMeasurements.computeIfAbsent(measurement.name, k -> new MeasurementAggregation());
        aggregation.calls++;
        aggregation.total += measurement.getDuration();
        aggregation.min = Math.min(aggregation.min, measurement.getDuration());
        aggregation.max = Math.max(aggregation.max, measurement.getDuration());
        aggregation.total += measurement.getDuration();

        if (detailsLog != null && detailsLog.isInfoEnabled()) {
            measurement.exportMeasurement(detailsLog);
        }
    }

    public void logSummary(Logger out) {
        storedMeasurements.forEach((name, m) -> m.logSummary(name, out));
    }

    private static class MeasurementAggregation {
        private long calls = 0;
        private long total = 0;
        private long min = Long.MAX_VALUE;
        private long max = 0;


        private void logSummary(String name, Logger out) {
            long average = total / calls;
            out.info(name + " (calls: " + calls + " "
                    + "total: " + total / TIME.MILLI_SECOND + " ms "
                    + "average: " + (average > 1000 ? (average / TIME.MILLI_SECOND + " ms ") : (average + " ns "))
                    + "max: " + (max > 1000 ? (max / TIME.MILLI_SECOND + " ms ") : (max + " ns "))
                    + "min: " + (min > 1000 ? (min / TIME.MILLI_SECOND + " ms ") : (max + " ns ")) + ")");

        }
    }

    public class Measurement implements AutoCloseable {

        private final String name;
        private long start;
        private long stop;
        private List<String> properties;

        private Measurement(String name) {
            this.name = name;
            this.start = System.nanoTime();
        }

        public Measurement restart() {
            this.start = System.nanoTime();
            return this;
        }

        public void stop() {
            stop = System.nanoTime();
            PerformanceMonitor.this.store(this);
        }

        public long getDuration() {
            return stop - start;
        }

        public Measurement setProperties(Object... properties) {
            if (this.properties == null) {
                this.properties = new ArrayList<>(properties.length);
            }
            for (Object property : properties) {
                this.properties.add(property.toString());
            }
            return this;
        }

        public List<String> getProperties() {
            return properties;
        }

        @Override
        public void close() {
            stop();
        }

        public void exportMeasurement(Logger out) {
            out.info("{};{};{}", name, getDuration(), properties != null ? StringUtils.join(getProperties(), ";") : "");
        }
    }


}
