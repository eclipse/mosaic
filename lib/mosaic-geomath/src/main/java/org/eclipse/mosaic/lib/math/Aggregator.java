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

package org.eclipse.mosaic.lib.math;

public class Aggregator {

    private double min;
    private double max;
    private double sum;
    private int total;

    public Aggregator() {
        reset();
    }

    public Aggregator add(double value) {
        min = Math.min(min, value);
        max = Math.max(max, value);
        sum += value;
        total++;
        return this;
    }

    public double sum() {
        return sum;
    }

    public double average() {
        return total > 0 ? sum / total : 0;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public int total() {
        return total;
    }

    public Aggregator reset() {
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        sum = 0d;
        total = 0;
        return this;
    }


}
