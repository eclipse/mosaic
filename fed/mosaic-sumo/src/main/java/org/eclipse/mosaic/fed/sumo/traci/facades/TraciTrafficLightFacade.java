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

package org.eclipse.mosaic.fed.sumo.traci.facades;

import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.TraciConnection;
import org.eclipse.mosaic.fed.sumo.traci.commands.JunctionGetPosition;
import org.eclipse.mosaic.fed.sumo.traci.commands.TrafficLightGetControlledLanes;
import org.eclipse.mosaic.fed.sumo.traci.commands.TrafficLightGetControlledLinks;
import org.eclipse.mosaic.fed.sumo.traci.commands.TrafficLightGetCurrentPhase;
import org.eclipse.mosaic.fed.sumo.traci.commands.TrafficLightGetCurrentProgram;
import org.eclipse.mosaic.fed.sumo.traci.commands.TrafficLightGetPrograms;
import org.eclipse.mosaic.fed.sumo.traci.commands.TrafficLightGetState;
import org.eclipse.mosaic.fed.sumo.traci.commands.TrafficLightGetTimeOfNextSwitch;
import org.eclipse.mosaic.fed.sumo.traci.commands.TrafficLightSetPhaseIndex;
import org.eclipse.mosaic.fed.sumo.traci.commands.TrafficLightSetProgram;
import org.eclipse.mosaic.fed.sumo.traci.commands.TrafficLightSetRemainingPhaseDuration;
import org.eclipse.mosaic.fed.sumo.traci.commands.TrafficLightSetState;
import org.eclipse.mosaic.fed.sumo.traci.complex.SumoTrafficLightLogic;
import org.eclipse.mosaic.fed.sumo.util.TrafficLightStateDecoder;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLight;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroup;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightProgram;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightProgramPhase;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightState;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TraciTrafficLightFacade {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final TraciConnection traciConnection;

    private final TrafficLightSetProgram setProgram;
    private final TrafficLightSetPhaseIndex setPhaseIndex;
    private final TrafficLightSetState setState;
    private final TrafficLightSetRemainingPhaseDuration setPhaseRemainingDuration;
    private final TrafficLightGetCurrentProgram getCurrentProgram;
    private final TrafficLightGetPrograms getProgramDefinitions;
    private final TrafficLightGetCurrentPhase getCurrentPhase;
    private final TrafficLightGetState getCurrentState;
    private final TrafficLightGetTimeOfNextSwitch getNextSwitchTime;
    private final TrafficLightGetControlledLanes getControlledLanes;
    private final TrafficLightGetControlledLinks getControlledLinks;
    private final JunctionGetPosition getJunctionPosition;

    /**
     * Creates a new {@link TraciTrafficLightFacade} object.
     *
     * @param traciConnection Connection to Traci.
     */
    public TraciTrafficLightFacade(TraciConnection traciConnection) {
        this.traciConnection = traciConnection;

        setProgram = traciConnection.getCommandRegister().getOrCreate(TrafficLightSetProgram.class);
        setPhaseIndex = traciConnection.getCommandRegister().getOrCreate(TrafficLightSetPhaseIndex.class);
        setState = traciConnection.getCommandRegister().getOrCreate(TrafficLightSetState.class);
        setPhaseRemainingDuration = traciConnection.getCommandRegister().getOrCreate(TrafficLightSetRemainingPhaseDuration.class);
        getCurrentProgram = traciConnection.getCommandRegister().getOrCreate(TrafficLightGetCurrentProgram.class);
        getProgramDefinitions = traciConnection.getCommandRegister().getOrCreate(TrafficLightGetPrograms.class);
        getCurrentPhase = traciConnection.getCommandRegister().getOrCreate(TrafficLightGetCurrentPhase.class);
        getCurrentState = traciConnection.getCommandRegister().getOrCreate(TrafficLightGetState.class);
        getNextSwitchTime = traciConnection.getCommandRegister().getOrCreate(TrafficLightGetTimeOfNextSwitch.class);
        getControlledLanes = traciConnection.getCommandRegister().getOrCreate(TrafficLightGetControlledLanes.class);
        getControlledLinks = traciConnection.getCommandRegister().getOrCreate(TrafficLightGetControlledLinks.class);
        getJunctionPosition = traciConnection.getCommandRegister().getOrCreate(JunctionGetPosition.class);
    }

    /**
     * Getter for the current traffic light program running on a certain traffic light group with the given id.
     *
     * @param trafficLightGroupId The group Id of a traffic light.
     * @return Current program of a certain traffic light group.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public String getCurrentProgram(String trafficLightGroupId) throws InternalFederateException {
        try {
            return getCurrentProgram.execute(traciConnection, trafficLightGroupId);
        } catch (TraciCommandException e) {
            throw new InternalFederateException("Could not retrieve current program of traffic light " + trafficLightGroupId, e);
        }
    }

    /**
     * Getter for the current phase of a traffic light program that is currently running on the traffic light group with the given id.
     *
     * @param trafficLightGroupId Traffic light group id.
     * @return Current phase of a traffic light program of a certain traffic light group.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public int getCurrentPhase(String trafficLightGroupId) throws InternalFederateException {
        try {
            return getCurrentPhase.execute(traciConnection, trafficLightGroupId);
        } catch (TraciCommandException e) {
            throw new InternalFederateException("Could not retrieve current phase of traffic light", e);
        }
    }

    /**
     * Getter for the assumed time of the next switch from the current phase to the next one
     * of the current traffic light program of a certain traffic light group.
     *
     * @param trafficLightGroupId Traffic light group id.
     * @return Assumed time of next phase switch in seconds.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public double getNextSwitchTime(String trafficLightGroupId) throws InternalFederateException {
        try {
            return getNextSwitchTime.execute(traciConnection, trafficLightGroupId);
        } catch (TraciCommandException e) {
            throw new InternalFederateException("Could not retrieve assumed next switch time of traffic light", e);
        }
    }

    /**
     * Getter for the controlled lanes by the traffic light.
     *
     * @param trafficLightGroupId The group Id of a traffic light.
     * @return List of the controlled lanes.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public Collection<String> getControlledLanes(String trafficLightGroupId) throws InternalFederateException {
        try {
            return getControlledLanes.execute(traciConnection, trafficLightGroupId);
        } catch (TraciCommandException e) {
            throw new InternalFederateException("Could not retrieve controlled lanes of traffic light", e);
        }
    }

    /**
     * Getter for the traffic light group.
     *
     * @param trafficLightGroupId the Id of the traffic light group.
     * @return The traffic light group.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public TrafficLightGroup getTrafficLightGroup(String trafficLightGroupId) throws InternalFederateException {
        try {
            GeoPoint junctionPosition = getJunctionPosition.execute(traciConnection, trafficLightGroupId).getGeographicPosition();

            final List<SumoTrafficLightLogic> programDefinitions = getProgramDefinitions.execute(traciConnection, trafficLightGroupId);
            final Map<String, TrafficLightProgram> trafficLightPrograms = transformDefinitionsIntoPrograms(programDefinitions);

            final List<TrafficLightGetControlledLinks.TrafficLightControlledLink> controlledLinks
                    = getControlledLinks.execute(traciConnection, trafficLightGroupId);
            final List<TrafficLight> trafficLights =
                    createTrafficLights(
                            trafficLightPrograms.get(getCurrentProgram(trafficLightGroupId)),
                            controlledLinks,
                            junctionPosition
                    );

            return new TrafficLightGroup(trafficLightGroupId, trafficLightPrograms, trafficLights);
        } catch (TraciCommandException e) {
            throw new InternalFederateException(e);
        }
    }

    /**
     * Builds TrafficLight objects based on the current program of a traffic light group and controlled links of this group.
     */
    private List<TrafficLight> createTrafficLights(
            TrafficLightProgram currentProgram,
            List<TrafficLightGetControlledLinks.TrafficLightControlledLink> controlledLinks,
            GeoPoint junctionPosition
    ) {

        List<TrafficLight> trafficLights = new ArrayList<>();
        int id = 0;
        for (TrafficLightState state : currentProgram.getCurrentPhase().getStates()) {
            if (id == controlledLinks.size()) {
                log.warn("There seem to be more states than links controlled by the TrafficLightProgram.");
                break;
            } else {
                trafficLights.add(
                        new TrafficLight(
                                id, junctionPosition, controlledLinks.get(id).getIncoming(), controlledLinks.get(id).getOutgoing(), state
                        )
                );
            }
            id++;
        }
        return trafficLights;
    }

    /**
     * Transforms traffic light group programs as they described in form of traffic light logic by SUMO
     * into TrafficLightProgram objects.
     *
     * @param programDefinitions Traffic light group programs as they described by SUMO.
     * @return Map with TrafficLightPrograms as values and their ids as keys.
     */
    private Map<String, TrafficLightProgram> transformDefinitionsIntoPrograms(List<SumoTrafficLightLogic> programDefinitions) {
        Map<String, TrafficLightProgram> programs = new LinkedHashMap<>();
        for (SumoTrafficLightLogic programDefinition : programDefinitions) {

            int phaseId = 0;
            List<TrafficLightProgramPhase> phases = new ArrayList<>();
            for (SumoTrafficLightLogic.Phase phaseLogic : programDefinition.getPhases()) {
                List<TrafficLightState> states = TrafficLightStateDecoder.createStateListFromEncodedString(phaseLogic.getPhaseDef());
                phases.add(new TrafficLightProgramPhase(phaseId, (long) phaseLogic.getDuration() * 1000, states));
                phaseId++;
            }
            TrafficLightProgram program =
                    new TrafficLightProgram(programDefinition.getLogicId(), phases, programDefinition.getCurrentPhase());
            programs.put(programDefinition.getLogicId(), program);
        }

        return programs;
    }

    /**
     * Returns the list of states representing the current state of each traffic light belonging to the group.
     *
     * @param trafficLightGroupId the ID of the traffic light group
     * @return the current state of the traffic light group
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public List<TrafficLightState> getCurrentStates(String trafficLightGroupId) throws InternalFederateException {
        try {
            return TrafficLightStateDecoder.createStateListFromEncodedString(getCurrentState.execute(traciConnection, trafficLightGroupId));
        } catch (TraciCommandException e) {
            return null;
        }
    }

    /**
     * Setter for the remaining phase duration.
     *
     * @param trafficLightGroupId     The Id of the traffic light group.
     * @param phaseRemainingDurationS The phase remaining duration in [s].
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setPhaseRemainingDuration(String trafficLightGroupId, double phaseRemainingDurationS) throws InternalFederateException {
        try {
            setPhaseRemainingDuration.execute(traciConnection, trafficLightGroupId, phaseRemainingDurationS);
        } catch (TraciCommandException e) {
            throw new InternalFederateException("Could not set remaining phase duration for traffic light", e);
        }
    }

    /**
     * Sets a program with the given id to a traffic light group with the given io.
     *
     * @param trafficLightGroupId a traffic light group id
     * @param programId           a program id
     * @throws InternalFederateException if couldn't set program for traffic light
     */
    public void setProgramById(String trafficLightGroupId, String programId) throws InternalFederateException {
        try {
            setProgram.execute(traciConnection, trafficLightGroupId, programId);
        } catch (TraciCommandException e) {
            throw new InternalFederateException("Could not set program for traffic light", e);
        }
    }

    /**
     * Setter for the remaining phase duration.
     *
     * @param trafficLightGroupId The Id of the traffic light group.
     * @param phaseId             The phase id within the current phase.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setPhaseIndex(String trafficLightGroupId, int phaseId) throws InternalFederateException {
        try {
            setPhaseIndex.execute(traciConnection, trafficLightGroupId, phaseId);
        } catch (TraciCommandException e) {
            throw new InternalFederateException("Could not change phase for traffic light group " + trafficLightGroupId, e);
        }
    }

    /**
     * Setter for the remaining phase duration.
     *
     * @param trafficLightGroupId The Id of the traffic light group.
     * @param stateList           The list of states for each traffic lights.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void setPhase(String trafficLightGroupId, List<TrafficLightState> stateList) throws InternalFederateException {
        try {
            setState.execute(traciConnection, trafficLightGroupId, TrafficLightStateDecoder.encodeStateList(stateList));
        } catch (TraciCommandException e) {
            throw new InternalFederateException("Could not change state for traffic light group " + trafficLightGroupId, e);
        }
    }
}
