/*
 * Copyright (c) 2023 Fraunhofer FOKUS and others. All rights reserved.
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

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.TraciVersion;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.Status;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveVehicleTypeState;
import org.eclipse.mosaic.rti.api.InternalFederateException;

public class VehicleTypeGetVClass
        extends AbstractTraciCommand<String>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleTypeGetVClass {

    /**
     * Creates a new {@link VehicleTypeGetVClass} traci command.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/VehicleType_Value_Retrieval.html">VehicleType Value Retrieval</a>
     */
    @SuppressWarnings("WeakerAccess")
    public VehicleTypeGetVClass() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandRetrieveVehicleTypeState.COMMAND)
                .variable(CommandRetrieveVehicleTypeState.VAR_VCLASS)
                .writeStringParam(); // vehicle type name

        read()
                .skipBytes(2)
                .skipString()
                .readStringWithType();
    }

    public String execute(Bridge bridge, String vehicleTypeId) throws CommandException, InternalFederateException {
        return super.executeAndReturn(bridge, vehicleTypeId).orElse("passenger");
    }

    @Override
    protected String constructResult(Status status, Object... objects) {
        return (String) objects[0];
    }
}
