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

package org.eclipse.mosaic.lib.routing;

/**
 * Interface to calculate a cost function for the edges in order to find the best route. It provides two options:
 * 1. Shortest: Shortest route to the destination.
 * 2. Fastest: Fastest route to the destination.
 */
public interface RoutingCostFunction {

    /**
     * Calculates the cost for a given edge.
     *
     * @param edgeProperties - collection of various attributes the edge provides
     * @return the costs for this connection
     */
    double calculateCosts(final EdgeProperties edgeProperties);

    String getCostFunctionName();

    RoutingCostFunction Shortest = new RoutingCostFunction() {

        @Override
        public double calculateCosts(final EdgeProperties edgeProperties) {
            return edgeProperties.getLength();
        }

        @Override
        public String getCostFunctionName() {
            return "Shortest";
        }

    };

    RoutingCostFunction Fastest = new RoutingCostFunction() {

        @Override
        public double calculateCosts(final EdgeProperties edgeProperties) {
            if (edgeProperties.getSpeed() <= 0d) {
                return Double.POSITIVE_INFINITY;
            }
            return edgeProperties.getLength() / edgeProperties.getSpeed();
        }

        @Override
        public String getCostFunctionName() {
            return "Fastest";
        }

    };

    RoutingCostFunction Default = RoutingCostFunction.Fastest;

}
