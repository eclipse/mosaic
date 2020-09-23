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
 * This class represents the traci command which allows to set an imperfection value for the driver.
 */
public class VehicleSetImperfection extends AbstractVehicleSetSingleDoubleValue {

    /**
     * Creates a new {@link VehicleSetImperfection} object.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Vehicle_State.html">Vehicle State Change</a>
     */
    @SuppressWarnings("WeakerAccess")
    public VehicleSetImperfection() {
        super(TraciVersion.LOWEST, CommandChangeVehicleValue.VAR_IMPERFECTION);
    }
}
