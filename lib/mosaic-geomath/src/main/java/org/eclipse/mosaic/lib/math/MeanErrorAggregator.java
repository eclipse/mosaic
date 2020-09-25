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

package org.eclipse.mosaic.lib.math;

public class MeanErrorAggregator {

    private final Aggregator aggregator = new Aggregator();

    public MeanErrorAggregator add(double observed, double predicted) {
        aggregator.add(Math.pow(predicted - observed, 2));
        return this;
    }

    public double max() {
        return Math.sqrt(aggregator.max());
    }

    public double min() {
        return Math.sqrt(aggregator.min());
    }


    public double meanSquaredError() {
        return aggregator.average();
    }

    public double meanError() {
        return Math.sqrt(meanSquaredError());
    }

    public int samples() {
        return aggregator.total();
    }

    public MeanErrorAggregator reset() {
        aggregator.reset();
        return this;
    }

}