/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveVehicleState;
import org.eclipse.mosaic.fed.sumo.bridge.traci.reader.StoppingPlaceReader;
import org.eclipse.mosaic.lib.objects.vehicle.StoppingPlace;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.List;
import java.util.Locale;

public class VehicleGetStops extends AbstractTraciCommand<List<StoppingPlace>>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleGetStops {

    /**
     * Creates a new {@link VehicleGetRouteId} traci command, which will
     * return the route-id the given vehicle is on, once executed.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/VehicleType_Value_Retrieval.html">VehicleType Value Retrieval</a>
     */
    @SuppressWarnings("WeakerAccess")
    public VehicleGetStops() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandRetrieveVehicleState.COMMAND)
                .variable(CommandRetrieveVehicleState.VAR_GET_STOPS)
                .writeVehicleIdParam()
                .writeIntParamWithType();

        read()
                .skipBytes(2)
                .skipString()
                .readComplex(new StoppingPlaceReader());
    }

    public List<StoppingPlace> execute(Bridge bridge, String vehicle, int maxStops) throws CommandException, InternalFederateException {
        return executeAndReturn(bridge, vehicle, maxStops).orElseThrow(
                () -> new CommandException(
                        String.format(Locale.ENGLISH, "Could not extract next stops of Vehicle: %s.", vehicle)
                )
        );
    }

    @Override
    protected List<StoppingPlace> constructResult(Status status, Object... objects) {
        return (List<StoppingPlace>) objects[0];
    }
}
