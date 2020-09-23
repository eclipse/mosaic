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

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciCommand;
import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.TraciConnection;
import org.eclipse.mosaic.fed.sumo.traci.TraciVersion;
import org.eclipse.mosaic.fed.sumo.traci.complex.Status;
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandRetrieveTrafficLightValue;
import org.eclipse.mosaic.fed.sumo.traci.reader.ListTraciReader;
import org.eclipse.mosaic.fed.sumo.traci.reader.StringTraciReader;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class represents the traci command which allows to get the links controlled by a traffic light.
 */
public class TrafficLightGetControlledLinks extends AbstractTraciCommand<List<TrafficLightGetControlledLinks.TrafficLightControlledLink>> {

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

    public List<TrafficLightControlledLink> execute(TraciConnection traciConnection, String tlId) throws TraciCommandException, InternalFederateException {
        return super.executeAndReturn(traciConnection, tlId).orElseThrow(
                () -> new TraciCommandException(
                        String.format(Locale.ENGLISH, "Couldn't get controlled Links for TrafficLight %s.", tlId),
                        new Status((byte) Status.STATUS_ERR, "")
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

    /**
     * Helper-class to represent a traffic light link.
     */
    public static class TrafficLightControlledLink {
        private final int signalIndex;
        private final String incoming;
        private final String outgoing;

        TrafficLightControlledLink(int signalIndex, String incoming, String outgoing) {
            this.signalIndex = signalIndex;
            this.incoming = incoming;
            this.outgoing = outgoing;
        }

        @SuppressWarnings("unused")
        public int getSignalIndex() {
            return signalIndex;
        }

        public String getIncoming() {
            return incoming;
        }

        public String getOutgoing() {
            return outgoing;
        }
    }
}
