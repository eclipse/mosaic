/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.sumo.bridge.libsumo;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;

import org.eclipse.sumo.libsumo.TraCILink;
import org.eclipse.sumo.libsumo.TraCILinkVector;
import org.eclipse.sumo.libsumo.TrafficLight;

import java.util.ArrayList;
import java.util.List;

public class TrafficLightGetControlledLinks implements org.eclipse.mosaic.fed.sumo.bridge.api.TrafficLightGetControlledLinks {


    public List<TrafficLightControlledLink> execute(Bridge bridge, String tlId) throws CommandException {
        try {
            List<TrafficLightControlledLink> controlledLinks = new ArrayList<>();
            int i = 0;
            for (TraCILinkVector links : TrafficLight.getControlledLinks(tlId)) {
                for (TraCILink link : links) {
                    controlledLinks.add(
                            new TrafficLightControlledLink(i++, link.getFromLane(), link.getToLane())
                    );
                }
            }
            return controlledLinks;
        } catch (IllegalArgumentException e) {
            throw new CommandException("Could not read list of controlled links for Traffic Light: " + tlId);
        }
    }

}
