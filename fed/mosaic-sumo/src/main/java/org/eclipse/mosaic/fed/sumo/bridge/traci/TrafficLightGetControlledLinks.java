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
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveTrafficLightValue;
import org.eclipse.mosaic.fed.sumo.bridge.traci.reader.ListTraciReader;
import org.eclipse.mosaic.fed.sumo.bridge.traci.reader.StringTraciReader;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class represents the SUMO command which allows to get the links controlled by a traffic light.
 */
public class TrafficLightGetControlledLinks
        extends AbstractTraciCommand<List<org.eclipse.mosaic.fed.sumo.bridge.api.TrafficLightGetControlledLinks.TrafficLightControlledLink>>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.TrafficLightGetControlledLinks {

    /**
     * Creates a {@link TrafficLightGetControlledLinks} traci command.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Traffic_Lights_Value_Retrieval.html">Traffic Lights Value Retrieval</a>
     */
    @SuppressWarnings("WeakerAccess")
    public TrafficLightGetControlledLinks() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandRetrieveTrafficLightValue.COMMAND)
                .variable(CommandRetrieveTrafficLightValue.VAR_CONTROLLED_LINKS)
                .writeStringParam();

        read()
                .skipBytes(2)
                .skipString()
                .skipByte()
                .skipInteger()
                .skipByte()
                .readComplex(
                        new ListTraciReader<>(
                                new ListTraciReader<>(
                                        new ListTraciReader<>(new StringTraciReader(), true), true)));
    }

    public List<TrafficLightControlledLink> execute(Bridge bridge, String tlId) throws CommandException, InternalFederateException {
        return super.executeAndReturn(bridge, tlId).orElseThrow(
                () -> new CommandException(
                        String.format(Locale.ENGLISH, "Could not read list of controlled Links for TrafficLight %s.", tlId)
                )
        );
    }

    @Override
    protected List<TrafficLightControlledLink> constructResult(Status status, Object... objects) {
        if (objects[0] instanceof Iterable) {
            List<List<List<String>>> rawResult = (List<List<List<String>>>) objects[0];
            List<TrafficLightControlledLink> result = new ArrayList<>(rawResult.size());
            int i = 0;
            for (List<List<String>> signals : rawResult) {
                for (List<String> controlledLinks : signals) {
                    Validate.isTrue(controlledLinks.size() == 3, "Could not read controlled links of traffic light");
                    result.add(new TrafficLightControlledLink(i, controlledLinks.get(0), controlledLinks.get(1)));
                }
                i++;
            }
            return result;
        } else {
            throw new IllegalArgumentException("Could not construct raw results from the objects that came from TraCI.");
        }
    }
}
