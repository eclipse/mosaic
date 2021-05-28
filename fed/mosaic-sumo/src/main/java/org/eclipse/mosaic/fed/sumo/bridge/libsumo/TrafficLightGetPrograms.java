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
package org.eclipse.mosaic.fed.sumo.bridge.libsumo;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.SumoTrafficLightLogic;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.eclipse.sumo.libsumo.TraCILogic;
import org.eclipse.sumo.libsumo.TraCIPhase;
import org.eclipse.sumo.libsumo.TraCIPhaseVector;
import org.eclipse.sumo.libsumo.TrafficLight;

import java.util.ArrayList;
import java.util.List;

public class TrafficLightGetPrograms implements org.eclipse.mosaic.fed.sumo.bridge.api.TrafficLightGetPrograms {

    public List<SumoTrafficLightLogic> execute(Bridge bridge, String tlId) throws CommandException, InternalFederateException {
        List<SumoTrafficLightLogic> logics = new ArrayList<>();
        for (TraCILogic traciLogic : TrafficLight.getAllProgramLogics(tlId)) {
            List<SumoTrafficLightLogic.Phase> phases = new ArrayList<>();
            TraCIPhaseVector traciPhases = traciLogic.getPhases();
            for (TraCIPhase traciPhase : traciPhases) {
                phases.add(new SumoTrafficLightLogic.Phase((int) (traciPhase.getDuration() * TIME.MILLI_SECOND), traciPhase.getState()));
            }

            logics.add(new SumoTrafficLightLogic(traciLogic.getProgramID(), phases, traciLogic.getCurrentPhaseIndex()));
        }
        return logics;
    }
}
