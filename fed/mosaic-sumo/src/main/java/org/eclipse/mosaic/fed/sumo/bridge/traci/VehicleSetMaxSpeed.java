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
 * This class represents the traci command which allows to set a maximum speed for the vehicle.
 */
public class VehicleSetMaxSpeed
        extends AbstractVehicleSetSingleDoubleValue
        implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetMaxSpeed {

    /**
     * Creates a new {@link VehicleSetMaxSpeed} object.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Vehicle_State.html">Vehicle State Change</a>
     */
    public VehicleSetMaxSpeed() {
        super(TraciVersion.LOWEST, CommandChangeVehicleValue.VAR_MAX_SPEED);
    }
}
