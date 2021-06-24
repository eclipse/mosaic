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

package org.eclipse.mosaic.fed.sumo.bridge.traci;

import org.eclipse.mosaic.fed.sumo.bridge.TraciVersion;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandChangeVehicleValue;

/**
 * This class represents the SUMO command which allows to set the lane-change-mode as following.
 * - Strategic (change lanes to continue the route)
 * - Cooperative (change in order to allow others to change)
 * - Speed gain (the other lane allows for faster driving)
 * - Obligation to drive on the right
 */
public class VehicleSetLaneChangeMode
        extends AbstractVehicleSetSingleIntegerValue
        implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetLaneChangeMode {

    /**
     * Creates a new {@link VehicleSetLaneChangeMode} object.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Vehicle_State.html">Vehicle State Change</a>
     */
    @SuppressWarnings("WeakerAccess")
    public VehicleSetLaneChangeMode() {
        super(TraciVersion.LOWEST, CommandChangeVehicleValue.VAR_LANE_CHANGE_MODE);
    }
}
