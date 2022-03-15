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

import org.eclipse.mosaic.lib.enums.VehicleClass;

/**
 * Class containing parameters for route calculation.
 * Gathers parameters in one object.
 */
public class RoutingParameters {

    private int numAlternativeRoutes = 0;

    private RoutingCostFunction routingCostFunction = RoutingCostFunction.Fastest;

    private boolean considerTurnCosts = false;

    private double restrictionCosts = Double.POSITIVE_INFINITY;

    private VehicleClass vehicleClass = VehicleClass.Car;

    public int getNumAlternativeRoutes() {
        return numAlternativeRoutes;
    }

    /**
     * Sets the maximum number of alternatives routes to search for between the given start and end point.
     *
     * @param numAlternativeRoutes Number of routes which should be found
     */
    public RoutingParameters alternativeRoutes(int numAlternativeRoutes) {
        this.numAlternativeRoutes = numAlternativeRoutes;
        return this;
    }

    public RoutingCostFunction getRoutingCostFunction() {
        return routingCostFunction;
    }

    /**
     * Sets the cost function (e.g. Shortest or Fastest) to use during route building.
     *
     * @param routingCostFunction The cost function for route calculation
     */
    public RoutingParameters costFunction(RoutingCostFunction routingCostFunction) {
        this.routingCostFunction = routingCostFunction;
        return this;
    }

    /**
     * Returns <code>true</code> if turn costs should be considered during routing.
     */
    public boolean isConsiderTurnCosts() {
        return considerTurnCosts;
    }

    /**
     * Defines, if turn costs should be considered or not.
     *
     * @param considerTurnCosts <code>true</code> if turn costs should be considered, <code>false</code> if turn should be ignored
     */
    public RoutingParameters considerTurnCosts(boolean considerTurnCosts) {
        this.considerTurnCosts = considerTurnCosts;
        return this;
    }

    /**
     * Defines the costs to apply for turn restrictions. Use {@code Double#POSITIVE_INFINITY} to forbid all turn restrictions (recommended).
     *
     * @param restrictionCosts the value to apply as costs when a turn is restricted
     */
    public RoutingParameters costsForTurnRestriction(double restrictionCosts) {
        this.restrictionCosts = Math.max(0d, restrictionCosts);
        return this;
    }

    public double getRestrictionCosts() {
        return restrictionCosts;
    }

    /**
     * Defines the {@link VehicleClass} to search a route for (e.g. routing for bicycles differs from car).
     *
     * @param vehicleClass Vehicle class for this routing request
     */
    public RoutingParameters vehicleClass(VehicleClass vehicleClass) {
        this.vehicleClass = vehicleClass;
        return this;
    }

    public VehicleClass getVehicleClass() {
        return vehicleClass;
    }

    @Override
    public String toString() {
        return "RoutingParameters [numAlternativeRoutes=" + numAlternativeRoutes + ", routingCostFunction=" + routingCostFunction
                + ", considerTurnCosts=" + considerTurnCosts + "]";
    }
}
