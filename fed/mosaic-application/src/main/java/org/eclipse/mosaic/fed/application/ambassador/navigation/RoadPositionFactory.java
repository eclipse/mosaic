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

package org.eclipse.mosaic.fed.application.ambassador.navigation;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.objects.road.SimpleRoadPosition;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;

import org.apache.commons.lang3.Validate;

import java.util.Iterator;
import java.util.List;

/**
 * Factory facade, which generates {@link IRoadPosition} in various ways. The {@link IRoadPosition} object
 * is mainly used for stop instructions for vehicles.
 */
public class RoadPositionFactory {

    /**
     * Creates a {@link IRoadPosition} based on the edgeId in SUMO format
     * "&lt;way&gt;_&lt;connectionFromNode&gt;_&lt;connectionToNode&gt;_&lt;previousNode&gt;".
     *
     * @param edgeId     The id of the edge in SUMO format.
     * @param laneIndex  The index of the lane where the road position is generated at.
     * @param edgeOffset the offset in m along the edge where the position is generated at
     * @return A new {@link IRoadPosition} with all available data.
     */
    public static IRoadPosition createFromSumoEdge(final String edgeId, final int laneIndex, final double edgeOffset) {
        final String[] roadIdParts = edgeId.split("_");
        if (roadIdParts.length < 4) {
            throw new IllegalArgumentException(String.format("Could not read edge id %s", edgeId));
        }
        final IRoadPosition roadPosition =
                new SimpleRoadPosition(roadIdParts[0], roadIdParts[1], roadIdParts[2], roadIdParts[3], laneIndex, edgeOffset);
        return refine(roadPosition);
    }

    /**
     * Create a {@link IRoadPosition} based on the current position of
     * the vehicle and the route its driving on. The position will be generated
     * starting at the position of the vehicle at a given distance along the route.
     *
     * @param currentPosition The current position of the vehicle.
     * @param currentRoute    The current route the vehicle driving on.
     * @param laneIndex       The index of lane where the road position is generated at.
     * @param distance        Distance in meters where the road position is generated at, starting from the current position of the vehicle.
     * @return A new {@link IRoadPosition} along the given route.
     */
    public static IRoadPosition createAlongRoute(
            final IRoadPosition currentPosition,
            final VehicleRoute currentRoute,
            final int laneIndex,
            final double distance
    ) {
        final CentralNavigationComponent cnc = SimulationKernel.SimulationKernel.getCentralNavigationComponent();

        Validate.notNull(cnc, "The CentralNavigationComponent must not be null");
        Validate.notNull(currentRoute, "The route must not be null");
        Validate.notNull(currentPosition, "The currentPosition must not be null");
        Validate.notNull(currentPosition.getPreviousNode(), "The currentPosition must provide the previously passed node");
        Validate.isTrue(distance > 0d, "The distance must be greater than 0");

        String nodeId;
        String from = null;
        String to = null;
        double untilStop = 0 - currentPosition.getOffset() - distance;
        for (Iterator<String> nodeIdIterator = currentRoute.getNodeIdList().iterator(); nodeIdIterator.hasNext(); ) {
            nodeId = nodeIdIterator.next();
            if (from != null) {
                to = nodeId;
                untilStop += cnc.getPositionOfNode(from).distanceTo(cnc.getPositionOfNode(to));
                if (untilStop > 0) {
                    break;
                }
                if (nodeIdIterator.hasNext()) {
                    from = to;
                }
            }
            if (nodeId.equals(currentPosition.getPreviousNode().getId())) {
                from = nodeId;
            }
        }

        double lengthOfRoadSegment = cnc.getPositionOfNode(from).distanceTo(cnc.getPositionOfNode(to));
        final IRoadPosition roadPosition;
        if (untilStop > 0) {
            roadPosition = new SimpleRoadPosition(from, to, laneIndex, lengthOfRoadSegment - untilStop);
        } else {
            roadPosition = new SimpleRoadPosition(from, to, laneIndex, lengthOfRoadSegment);
        }
        return refine(roadPosition);
    }

    /**
     * Creates an {@link IRoadPosition} for the last edge of a
     * list of route nodeIds.
     *
     * @param currentRoute The {@link VehicleRoute}, that the {@link IRoadPosition} should be created for.
     * @param laneIndex    The lane index.
     * @return An {@link IRoadPosition} of the last edge in route.
     */
    public static IRoadPosition createAtEndOfRoute(VehicleRoute currentRoute, int laneIndex) {
        return createAtEndOfRoute(currentRoute.getNodeIdList(), laneIndex);
    }

    /**
     * Creates an {@link IRoadPosition} for the last edge of a
     * list of route nodeIds.
     *
     * @param currentRouteNodeIds The list of nodeIds, that the {@link IRoadPosition} should be created for.
     * @param laneIndex           The lane index.
     * @return An {@link IRoadPosition} of the last edge in route.
     */
    public static IRoadPosition createAtEndOfRoute(List<String> currentRouteNodeIds, int laneIndex) {
        final String startNodeOfLastEdge = currentRouteNodeIds.get(currentRouteNodeIds.size() - 2);
        final String endNodeOfLastEdge = currentRouteNodeIds.get(currentRouteNodeIds.size() - 1);
        return refine(new SimpleRoadPosition(startNodeOfLastEdge, endNodeOfLastEdge, laneIndex, -1d));
    }

    /**
     * Creates an {@link IRoadPosition} between two nodeIds.
     *
     * @param nodeA     The first nodeId.
     * @param nodeB     The second nodeId.
     * @param laneIndex The lane index.
     * @param offset    The laneOffset.
     * @return An {@link IRoadPosition} between {@code nodeA} and {@code nodeB}.
     */
    @SuppressWarnings("unused")
    public static IRoadPosition createBetweenNodes(String nodeA, String nodeB, int laneIndex, double offset) {
        return refine(new SimpleRoadPosition(nodeA, nodeB, laneIndex, offset));
    }

    /**
     * This method refines the road position while obtaining the missing information from the database.
     *
     * @param roadPosition {@link IRoadPosition} containing position information such as upcoming and previous node Ids.
     * @return The refined road position as{@link IRoadPosition}.
     */
    private static IRoadPosition refine(IRoadPosition roadPosition) {
        return SimulationKernel.SimulationKernel.getCentralNavigationComponent().refineRoadPosition(roadPosition);
    }
}