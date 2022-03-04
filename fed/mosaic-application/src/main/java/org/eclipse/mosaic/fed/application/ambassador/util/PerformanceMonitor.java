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

package org.eclipse.mosaic.fed.application.ambassador.util;

import org.eclipse.mosaic.rti.TIME;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PerformanceMonitor {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Map<String, List<Measurement>> storedMeasurements = new HashMap<>();

    public Measurement start(String name) {
        return new Measurement(name);
    }

    private void store(Measurement measurement) {
        storedMeasurements.computeIfAbsent(measurement.name, l -> new LinkedList<>()).add(measurement);
    }

    public void printSummary() {
        storedMeasurements.forEach(this::printSummary);
    }

    public void exportDetailedMeasurements(Writer out) throws IOException {
        for (List<Measurement> measurements : storedMeasurements.values()) {
            for (Measurement measurement : measurements) {
                measurement.exportMeasurement(out);
            }
        }
        out.flush();
    }

    private void printSummary(String name, List<Measurement> measurements) {
        long total = 0;
        long max = 0;
        long min = Long.MAX_VALUE;
        for (Measurement m : measurements) {
            total += m.getDuration();
            max = Math.max(max, m.getDuration());
            min = Math.min(min, m.getDuration());
        }
        long average = total / measurements.size();

        log.info(name + " (calls: " + measurements.size() + " "
                + "total: " + total / TIME.MILLI_SECOND + " ms "
                + "average: " + (average > 1000 ? (average / TIME.MILLI_SECOND + " ms ") : (average + " ns "))
                + "max: " + (max > 1000 ? (max / TIME.MILLI_SECOND + " ms ") : (max + " ns "))
                + "min: " + (min > 1000 ? (min / TIME.MILLI_SECOND + " ms ") : (max + " ns ")) + ")");

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

        public void exportMeasurement(Writer out) throws IOException {
            out.write(String.format("%s;%d;%s%n", name, getDuration(), StringUtils.join(getProperties(), ";")));
        }
    }


}
