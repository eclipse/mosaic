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
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.Status;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.SumoTrafficLightLogic;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveTrafficLightValue;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.TraciDatatypes;
import org.eclipse.mosaic.fed.sumo.bridge.traci.reader.ListTraciReader;
import org.eclipse.mosaic.fed.sumo.bridge.traci.reader.TrafficLightProgramTraciReader;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * Retrieves complete definitions of all traffic light programs. Does only work with SUMO >= 1.1.0. With
 * prior API versions an empty list is returned.
 */
public class TrafficLightGetPrograms
        extends AbstractTraciCommand<List<SumoTrafficLightLogic>>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.TrafficLightGetPrograms {

    /**
     * Creates a new {@link TrafficLightGetState} object.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Traffic_Lights_Value_Retrieval.html">Traffic Lights Value Retrieval</a>
     */
    @SuppressWarnings("WeakerAccess")
    public TrafficLightGetPrograms() {
        super(TraciVersion.API_19);

        write()
                .command(CommandRetrieveTrafficLightValue.COMMAND)
                .variable(CommandRetrieveTrafficLightValue.VAR_COMPLETE_DEFINITION)
                .writeStringParam();

        read()
                .skipBytes(2)
                .skipString()
                .expectByte(TraciDatatypes.COMPOUND)
                .readComplex(new ListTraciReader<>(new TrafficLightProgramTraciReader()));
    }

    /**
     * This method executes the command with the given arguments in order to get the complete definitions of all traffic light programs.
     *
     * @param con  Connection to Traci.
     * @param tlId Id of the traffic light.
     * @return The traffic light programs.
     * @throws CommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public List<SumoTrafficLightLogic> execute(Bridge con, String tlId) throws CommandException, InternalFederateException {
        return executeAndReturn(con, tlId).orElse(Lists.newArrayList());
    }

    @Override
    protected List<SumoTrafficLightLogic> constructResult(Status status, Object... objects) {
        List<?> intermediateResult = (List<?>) objects[0];
        List<SumoTrafficLightLogic> result = new ArrayList<>();
        for (Object element : intermediateResult) {
            // testing all elements for proper types
            if (element instanceof SumoTrafficLightLogic) {
                result.add((SumoTrafficLightLogic) element);
            }
        }
        return result;
    }
}
