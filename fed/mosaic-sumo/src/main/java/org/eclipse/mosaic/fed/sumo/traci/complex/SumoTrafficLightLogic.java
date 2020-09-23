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

package org.eclipse.mosaic.fed.sumo.traci.complex;

import java.util.Collections;
import java.util.List;

/**
 * This class represents a traffic light program based on SUMO definition
 * as described on https://sumo.dlr.de/docs/Simulation/Traffic_Lights.html#defining_new_tls-programs.
 */
public class SumoTrafficLightLogic extends AbstractSubscriptionResult {

    private final String logicId;
    private final List<Phase> phases;
    private final int currentPhase;

    /**
     * Creates a new {@link SumoTrafficLightLogic} object.
     *
     * @param logicId      The id of the traffic light logic.
     * @param phases       List of switch phases.
     * @param currentPhase The index of the phase.
     */
    public SumoTrafficLightLogic(String logicId, List<Phase> phases, int currentPhase) {
        this.logicId = logicId;
        this.phases = phases;
        this.currentPhase = currentPhase;
    }

    /**
     * Getter for the Id of the traffic light logic.
     *
     * @return The logic Id.
     */
    public String getLogicId() {
        return logicId;
    }

    /**
     * Getter for the switch phases.
     *
     * @return List of switch phases.
     */
    public List<Phase> getPhases() {
        return Collections.unmodifiableList(phases);
    }

    /**
     * Getter for the Index of the phase.
     *
     * @return the index of the phase.
     */
    public int getCurrentPhase() {
        return currentPhase;
    }

    /**
     * A helper class represents the switch phases of the traffic light.
     */
    public static class Phase {

        private final int durationMs;
        private final String phaseDef;

        public Phase(int durationMs, String phaseDef) {
            this.durationMs = durationMs;
            this.phaseDef = phaseDef;
        }

        public int getDuration() {
            return durationMs;
        }

        public String getPhaseDef() {
            return phaseDef;
        }
    }
}
