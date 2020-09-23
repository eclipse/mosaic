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

package org.eclipse.mosaic.lib.routing.graphhopper;

import org.eclipse.mosaic.lib.routing.RoutingCostFunction;
import org.eclipse.mosaic.lib.routing.graphhopper.util.GraphhopperToDatabaseMapper;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.util.EdgeIteratorState;

/**
 * A dynamic weight calculation. If an alternative travel time
 * on an edge is known, then this travel time will be used to weight
 * during routing. Otherwise, the minimum travel time will be used
 * to weight an edge.
 */
public class GraphHopperWeighting extends AbstractWeighting {

    private final GraphHopperEdgeProperties edgePropertiesState;
    private final double maxSpeed;

    private RoutingCostFunction routingCostFunction;

    public GraphHopperWeighting(FlagEncoder encoder, GraphhopperToDatabaseMapper graphMapper) {
        super(encoder);
        this.edgePropertiesState = new GraphHopperEdgeProperties(encoder, graphMapper);
        this.maxSpeed = encoder.getMaxSpeed() / 3.6; // getMaxSpeed returns the speed in km/h
    }

    @Override
    public double getMinWeight(double distance) {
        return distance / maxSpeed;
    }

    @Override
    public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
        synchronized (edgePropertiesState) {
            edgePropertiesState.setCurrentEdgeIterator(edge, reverse);
            if (routingCostFunction == null) {
                return edge.getDistance() / edgePropertiesState.getSpeed();
            } else {
                return routingCostFunction.calculateCosts(edgePropertiesState);
            }
        }
    }

    public String getName() {
        if (routingCostFunction == null) {
            return "null|" + flagEncoder;
        } else {
            return routingCostFunction.getCostFunctionName().toLowerCase() + "|" + flagEncoder;
        }
    }

    public void setRoutingCostFunction(RoutingCostFunction routingCostFunction) {
        this.routingCostFunction = routingCostFunction;
    }

}
