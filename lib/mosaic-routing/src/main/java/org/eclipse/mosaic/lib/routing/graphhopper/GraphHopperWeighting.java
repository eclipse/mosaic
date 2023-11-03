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

package org.eclipse.mosaic.lib.routing.graphhopper;

import org.eclipse.mosaic.lib.routing.RoutingCostFunction;
import org.eclipse.mosaic.lib.routing.graphhopper.util.GraphhopperToDatabaseMapper;
import org.eclipse.mosaic.lib.routing.graphhopper.util.VehicleEncoding;
import org.eclipse.mosaic.lib.routing.graphhopper.util.WayTypeEncoder;

import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.routing.weighting.TurnCostProvider;
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

    public GraphHopperWeighting(VehicleEncoding vehicleEncoding, WayTypeEncoder wayTypeEncoder, TurnCostProvider turnCostProvider, GraphhopperToDatabaseMapper graphMapper) {
        super(vehicleEncoding.access(), vehicleEncoding.speed(), turnCostProvider);
        this.edgePropertiesState = new GraphHopperEdgeProperties(vehicleEncoding, wayTypeEncoder, graphMapper);
        this.maxSpeed = speedEnc.getMaxOrMaxStorableDecimal() / 3.6;
    }

    public GraphHopperWeighting setRoutingCostFunction(RoutingCostFunction routingCostFunction) {
        this.routingCostFunction = routingCostFunction;
        return this;
    }

    @Override
    public double getMinWeight(double distance) {
        return distance / maxSpeed;
    }

    @Override
    public double calcTurnWeight(int inEdge, int viaNode, int outEdge) {
        return super.calcTurnWeight(inEdge, viaNode, outEdge);
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edge, boolean reverse) {
        if (reverse ? !edge.getReverse(accessEnc) : !edge.get(accessEnc)) {
            return Double.POSITIVE_INFINITY;
        }
        synchronized (edgePropertiesState) {
            edgePropertiesState.setCurrentEdgeIterator(edge, reverse);
            if (routingCostFunction == null) {
                return (edge.getDistance() / edgePropertiesState.getSpeed()) * 3.6;
            } else {
                return routingCostFunction.calculateCosts(edgePropertiesState) * 3.6;
            }
        }
    }

    public String getName() {
        if (routingCostFunction == null) {
            return "fastest";
        } else {
            return routingCostFunction.getCostFunctionName().toLowerCase();
        }
    }

}
