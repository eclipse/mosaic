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

package org.eclipse.mosaic.fed.sumo.traci.commands;

import org.eclipse.mosaic.fed.sumo.traci.TraciVersion;
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandChangeVehicleValue;

/**
 * This class represents the traci command which allows to set the speed mode for the vehicle.
 * Per default, the vehicle may only drive slower than the maximum permitted speed on the route and
 * it follows the right-of-way rules. Furthermore, tt may not exceed the bounds on acceleration and deceleration.
 */
public class VehicleSetSpeedMode extends AbstractVehicleSetSingleIntegerValue {

    /**
     * Creates a new {@link VehicleSetSpeedMode} object.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Vehicle_State.html">Vehicle State Change</a>
     */
    public VehicleSetSpeedMode() {
        super(TraciVersion.LOWEST, CommandChangeVehicleValue.VAR_SPEED_MODE);
    }
}
