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

package org.eclipse.mosaic.fed.sumo.util;

import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightState;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrafficLightStateDecoder {

    public static List<TrafficLightState> createStateListFromEncodedString(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return new ArrayList<>();
        }
        return encoded.codePoints()
                .mapToObj(c -> createStateFromCharacter((char) c))
                .collect(Collectors.toList());
    }

    /**
     * Creates a TrafficLightState object from a character describing the traffic light state in a phase definition.
     * see https://sumo.dlr.de/docs/Simulation/Traffic_Lights.html#signal_state_definitions for more information
     *
     * @param stateChar a character describing the traffic light state in a phase definition
     * @return a TrafficLightState object
     */
    public static TrafficLightState createStateFromCharacter(Character stateChar) {
        switch (stateChar) {
            case 'G':
            case 'g':
                return new TrafficLightState(false, true, false);
            case 'r':
                return new TrafficLightState(true, false, false);
            case 'y':
            case 'Y':
                return new TrafficLightState(false, false, true);
            case 'u':
                return new TrafficLightState(true, false, true);
            case 'O': //off - no signal
                return new TrafficLightState(false, false, false);
            default:
                throw new IllegalArgumentException("Could not create a TrafficLightState from a phase definitions character " + stateChar);
        }
    }

    public static String encodeStateList(List<TrafficLightState> customStateList) {
        return customStateList.stream().map(
                (state) -> state.isGreen() ? "G"
                        : state.isRedYellow() ? "u"
                        : state.isRed() ? "r"
                        : state.isYellow() ? "y"
                        : "O"
        ).collect(Collectors.joining());
    }
}
