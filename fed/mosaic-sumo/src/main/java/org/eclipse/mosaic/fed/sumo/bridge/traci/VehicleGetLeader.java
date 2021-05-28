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

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.TraciVersion;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.LeadingVehicle;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.Status;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveVehicleState;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.TraciDatatypes;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

/**
 * This class represents the SUMO command which allows to get the leading vehicle.
 */
public class VehicleGetLeader
        extends AbstractTraciCommand<LeadingVehicle>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleGetLeader {

    /**
     * Creates a new {@link VehicleGetLeader} traci command,
     * which will return the id of the leading vehicle within
     * th given lookahead distance, once executed
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/VehicleType_Value_Retrieval.html">VehicleType Value Retrieval</a>
     */
    @SuppressWarnings("WeakerAccess")
    public VehicleGetLeader() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandRetrieveVehicleState.COMMAND)
                .variable(CommandRetrieveVehicleState.VAR_LEADER)
                .writeVehicleIdParam()
                .writeDoubleParamWithType();

        read()
                .skipBytes(2)
                .skipString()
                .expectByte(TraciDatatypes.COMPOUND)
                .expectInteger(2)
                .readVehicleIdWithType()
                .readDoubleWithType();
    }

    /**
     * This method executes the command with the given arguments in order to get the leading vehicle.
     *
     * @param bridge Connection to SUMO.
     * @param vehicle   Id of the vehicle.
     * @param lookahead look ahead.
     * @return Id of the leading vehicle.
     * @throws CommandException     if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public @Nullable
    LeadingVehicle execute(Bridge bridge, String vehicle, double lookahead) throws CommandException, InternalFederateException {
        return executeAndReturn(bridge, vehicle, lookahead).orElse(null);
    }

    @Override
    protected LeadingVehicle constructResult(Status status, Object... objects) {
        String leaderId = (String) objects[0];
        if (StringUtils.isEmpty(leaderId)) {
            return null;
        }
        return new LeadingVehicle(leaderId, (double) objects[1]);
    }
}
