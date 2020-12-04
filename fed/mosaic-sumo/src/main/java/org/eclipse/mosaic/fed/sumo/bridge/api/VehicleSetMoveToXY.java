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

package org.eclipse.mosaic.fed.sumo.bridge.api;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.rti.api.InternalFederateException;

/**
 * This class represents the traci command which allows to set the vehicle move to explicit position.
 */
public interface VehicleSetMoveToXY {

    /**
     * Enum class that represents the different mode.
     */
    enum Mode {
        SWITCH_ROUTE(0),
        KEEP_ROUTE(1),
        EXACT_POSITION(2);

        public final int mode;

        Mode(int mode) {
            this.mode = mode;
        }
    }

    /**
     * This method executes the command with the given arguments in order to set the vehicle move to a position.
     *
     * @param traciCon      Connection to Traci.
     * @param vehicleId     The Id of the vehicle.
     * @param edgeId        The Id of the edge.
     * @param laneIndex     The index of the lane.
     * @param position      The cartesian position to move it.
     * @param angle         Angle of the move.
     * @param keepRouteMode Move mode.
     * @throws CommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    void execute(Bridge traciCon, String vehicleId, String edgeId, int laneIndex, CartesianPoint position, double angle, Mode keepRouteMode) throws CommandException, InternalFederateException;
}
