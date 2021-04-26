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

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.Validate;

import java.util.List;

/**
 * Factory facade, which generates {@link IRoadPosition} in various ways. The {@link IRoadPosition} object
 * is mainly used for stop instructions for vehicles.
 */
public class RoadPositionFactory {

    /**
     * Creates a {@link IRoadPosition} based on an arbitrary edge ID (= connection ID) in SUMO.
     *
     * @param edgeId       The id of the edge.
     * @param laneIndex    The index of the lane where the road position is generated at.
     * @param edgeOffset   the offset in m along the edge where the position is generated at
     * @return A new {@link IRoadPosition} with all available data.
     */
    public static IRoadPosition createFromSumoEdge(final String edgeId, final int laneIndex, final double edgeOffset) {
        return refine(new SimpleRoadPosition(edgeId, laneIndex, edgeOffset, 0d));
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
        Validate.notNull(currentPosition.getConnectionId(), "The currentPosition must provide the id of the connection");
        Validate.isTrue(distance > 0d, "The distance must be greater than 0");

        double untilStop = 0 - currentPosition.getOffset() - distance;

        String connectionToStopOn = null;
        for (String connectionId : currentRoute.getConnectionIds()) {
            if (connectionId.equals(currentPosition.getConnectionId())) {
                connectionToStopOn = connectionId;
            }

            if (connectionToStopOn != null) {
                connectionToStopOn = connectionId;
                untilStop += cnc.getLengthOfConnection(connectionId);
                if (untilStop > 0) {
                    break;
                }
            }
        }

        double lengthOfConnection = cnc.getLengthOfConnection(connectionToStopOn);
        IRoadPosition roadPosition;
        if (untilStop > 0) {
            roadPosition = new SimpleRoadPosition(connectionToStopOn, laneIndex, lengthOfConnection - untilStop, 0d);
        } else {
            roadPosition = new SimpleRoadPosition(connectionToStopOn, laneIndex, lengthOfConnection, 0d);
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
        return createAtEndOfRoute(currentRoute.getConnectionIds(), laneIndex);
    }

    /**
     * Creates an {@link IRoadPosition} for the last edge of a
     * list of route nodeIds.
     *
     * @param currentRouteConnectionIds The list of connectionIds, that the {@link IRoadPosition} should be created for.
     * @param laneIndex                 The lane index.
     * @return An {@link IRoadPosition} of the last edge in route.
     */
    public static IRoadPosition createAtEndOfRoute(List<String> currentRouteConnectionIds, int laneIndex) {
        // laneOffset of -1 results in a position 1m before the end of the route
        // lateral offset does not matter, therefore we use 0
        return refine(new SimpleRoadPosition(Iterables.getLast(currentRouteConnectionIds), laneIndex, -1d, 0d));
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