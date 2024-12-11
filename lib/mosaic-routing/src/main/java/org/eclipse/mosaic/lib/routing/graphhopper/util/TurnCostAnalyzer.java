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

package org.eclipse.mosaic.lib.routing.graphhopper.util;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.GeoUtils;
import org.eclipse.mosaic.rti.UNITS;

import com.graphhopper.routing.util.AccessFilter;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.TurnCostStorage;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;

/**
 * Calculates costs for all turns of a traffic network.
 */
public class TurnCostAnalyzer {

    /* fetch pillar nodes only */
    private final static int GEOMETRY_FETCH_MODE_PILLAR_NODES_ONLY = 0;

    /**
     * max_a = (coefficient of friction = 0.9 (dry asphalt) times gravity on Earth = 9.81 m/s^2)
     */
    private final static double MAX_A = 9.81 * 0.9;

    /**
     * default acceleration of a car.
     */
    private final double acceleration;

    /**
     * default deceleration of a car.
     */
    private final double deceleration;

    /*
     * TODO make depend on road type
     */
    private double minCurveRadius;
    private final WayTypeEncoder waytypeEncoder;
    private final BaseGraph graph;

    /**
     * Crates a new {@link TurnCostAnalyzer} with default calculation properties.
     */
    public TurnCostAnalyzer(BaseGraph graph, WayTypeEncoder waytypeEncoder) {
        this.graph = graph;
        this.waytypeEncoder = waytypeEncoder;
        /*
         * Mean acceleration and deceleration for cars
         * (according to [Maurya et al: "Acceleration-Deceleration Behaviour of Various Vehicle Types", 2016]).
         */
        acceleration = 0.9;
        deceleration = 2.5;
        minCurveRadius = 20;
    }



    public void createTurnCostsForVehicle(VehicleEncoding encoding) {

        EdgeExplorer outEdgeExplorer = graph.createEdgeExplorer(AccessFilter.allEdges(encoding.access()));
        EdgeExplorer inEdgeExplorer = graph.createEdgeExplorer(AccessFilter.allEdges(encoding.access()));

        TurnCostStorage tcStorage = graph.getTurnCostStorage();

        for (int n = 0; n < graph.getNodes(); n++) {
            EdgeIterator inEdgeIt = inEdgeExplorer.setBaseNode(n);
            while (inEdgeIt.next()) {
                EdgeIterator outEdgeIt = outEdgeExplorer.setBaseNode(n);
                while (outEdgeIt.next()) {
                    if (inEdgeIt.getEdge() != outEdgeIt.getEdge()) {
                        boolean alreadyRestricted = tcStorage.get(encoding.turnRestriction(), inEdgeIt.getEdge(), n, outEdgeIt.getEdge());
                        double existingCosts = tcStorage.get(encoding.turnCost(), inEdgeIt.getEdge(), n, outEdgeIt.getEdge());
                        //if turn is already restricted, than we do not need to consider turn costs
                        if (!alreadyRestricted) {
                            double c = calculateTurnCosts(encoding, inEdgeIt, n, outEdgeIt) + existingCosts;
                            if (c > 0.00001) {
                                tcStorage.set(encoding.turnCost(), inEdgeIt.getEdge(), n, outEdgeIt.getEdge(), Math.ceil(c));
                            }
                        }
                    }
                }

            }
        }

    }

    /**
     * Calculates the turn costs depending on the edge type( incoming- or outgoing edge). The edge will be converted
     * into geometric points. Further parameter for the calculating are the angle between incoming and outgoing edge,
     * the length and the maximum speed of the edge.
     *
     * @param incomingEdge Incoming edge
     * @param node         ID of the node
     * @param outgoingEdge Outgoing edge
     * @return Calculated turn cost
     */
    private double calculateTurnCosts(VehicleEncoding encoding, EdgeIterator incomingEdge, int node, EdgeIterator outgoingEdge) {
        //determining way types

        int incomingWayType = incomingEdge.get(waytypeEncoder);
        int outgoingWayType = outgoingEdge.get(waytypeEncoder);

        //we ignore turn costs, as soon as we are on highways
        if (WayTypeEncoder.isHighway(incomingWayType) && WayTypeEncoder.isHighway(outgoingWayType)) {
            return 0;
        }

        //use way geometry to calculate
        final GHPoint pointFrom = getSecondLastPointOfEdge(graph, incomingEdge);
        final GHPoint pointVia = new GHPoint(graph.getNodeAccess().getLat(node), graph.getNodeAccess().getLon(node));
        final GHPoint pointTo = getSecondPointOfEdge(graph, outgoingEdge);

        //calculate angle between incoming/outgoing edge using bearing of from-via and via-to
        GeoPoint from = GeoPoint.latLon(pointFrom.lat, pointFrom.lon);
        GeoPoint via = GeoPoint.latLon(pointVia.lat, pointVia.lon);
        GeoPoint to = GeoPoint.latLon(pointTo.lat, pointTo.lon);
        double bearingFromVia = GeoUtils.azimuth(via, from);
        double bearingViaTo = GeoUtils.azimuth(via, to);
        double alpha = (bearingViaTo - (bearingFromVia) + 360) % 360;

        //get length and max speed of edges
        double v1 = incomingEdge.get(encoding.speed()) * UNITS.KMH;
        double v2 = outgoingEdge.get(encoding.speed()) * UNITS.KMH;
        double l1 = incomingEdge.getDistance();
        double l2 = outgoingEdge.getDistance();

        //decide if left or right turn
        boolean isLeftTurn = alpha < 140;
        boolean isRightTurn = alpha > 220;

        double turnVelocity;

        //if left turn or right turn is going to take a more important street type, the car needs to stop (turn speed = 0 m/s)
        if (isLeftTurn || (isRightTurn && (WayTypeEncoder.isLowerType(outgoingWayType,
                incomingWayType)))) {
            turnVelocity = 0;
            //else, the maximum turn speed depends on the angle between incoming and outgoing edge
        } else {
            if (isLeftTurn || isRightTurn) {
                //when turning, the max speed on the outgoing edge also depends on its length (i.e., if the
                //outgoing edge is very short, we don't need to accelerate till max speed)
                v2 = Math.min(v2, MAX_A * Math.sqrt((2 * l2) / MAX_A));
            }

            //adjusting angle: tangens only gives positive values in [0,180]
            if (alpha > 180) {
                alpha = 360 - alpha;
            }

            //Yeah Science! (maximum turn velocity)
            turnVelocity = Math.min(v1, Math.sqrt(MAX_A * Math.tan(Math.toRadians(alpha / 2)) * Math.min(
                    minCurveRadius, Math.min(l1, l2)) / 2));
        }

        if (WayTypeEncoder.isResidential(incomingWayType) && WayTypeEncoder.isResidential(
                outgoingWayType)) {
            //decrease max turn speed to 10 km/h, if it is on a junction within a residential area
            turnVelocity = Math.min(turnVelocity, 10.0 * UNITS.KMH);
        }

        // drivers usually do not max out their turn velocity
        turnVelocity *= 0.8;

        //Again some Science (time for deceleration to v_turn + time for acceleration to v2)
        double turnCosts = (Math.pow(v1 - turnVelocity, 2) / (2 * deceleration * v1))
                + (Math.pow(v2 - turnVelocity, 2) / (2 * acceleration * v2));

        //adding additional costs if left turn (i.e. waiting for opposing traffic)
        if (isLeftTurn) {
            turnCosts += 10;
        }
        //a turn never needs more time than 60 seconds!
        return Math.min(turnCosts, 60);
    }

    /**
     * Returns the second last coordinate of an edge. If no way geometry is available for this edge,
     * the end node is the last second coordinate.
     *
     * @return the second last coordinate of an edge.
     */
    private GHPoint getSecondLastPointOfEdge(Graph graph, EdgeIterator edge) {
        PointList geom = edge.fetchWayGeometry(FetchMode.PILLAR_ONLY);
        if (geom.isEmpty()) {
            return new GHPoint(graph.getNodeAccess().getLat(edge.getAdjNode()),
                    graph.getNodeAccess().getLon(edge.getAdjNode()));
        }
        int index = geom.size() - 1;
        return new GHPoint(geom.getLat(index), geom.getLon(index));
    }

    /**
     * Returns the second coordinate of an edge. If no way geometry is available for this edge, the
     * end node is the second coordinate.
     *
     * @return the second coordinate of an edge.
     */
    private GHPoint getSecondPointOfEdge(Graph graph, EdgeIterator edge) {
        PointList geom = edge.fetchWayGeometry(FetchMode.PILLAR_ONLY);
        if (geom.isEmpty()) {
            return new GHPoint(graph.getNodeAccess().getLat(edge.getAdjNode()),
                    graph.getNodeAccess().getLon(edge.getAdjNode()));
        }
        return new GHPoint(geom.getLat(0), geom.getLon(0));
    }

}
