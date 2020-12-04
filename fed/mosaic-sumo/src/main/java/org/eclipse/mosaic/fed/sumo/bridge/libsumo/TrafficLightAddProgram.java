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
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.SumoTrafficLightLogic;

import org.eclipse.sumo.libsumo.TraCILogic;
import org.eclipse.sumo.libsumo.TraCIPhase;
import org.eclipse.sumo.libsumo.TraCIPhaseVector;
import org.eclipse.sumo.libsumo.TrafficLight;

import java.util.List;

public class TrafficLightAddProgram implements org.eclipse.mosaic.fed.sumo.bridge.api.TrafficLightAddProgram {


    public void execute(Bridge con, String tlId, String programId, int phaseIndex, List<SumoTrafficLightLogic.Phase> phases) {
        TraCIPhaseVector traCIphases = new TraCIPhaseVector();
        for (SumoTrafficLightLogic.Phase phase : phases) {
            traCIphases.add(new TraCIPhase(phase.getDuration() / 1000d, phase.getPhaseDef()));
        }
        TraCILogic traCIlogic = new TraCILogic(programId, 0/*FIXME type??*/, phaseIndex, traCIphases);
        TrafficLight.setProgramLogic(tlId, traCIlogic);
    }
}
