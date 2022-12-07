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

package org.eclipse.mosaic.fed.application.ambassador.simulation;

import org.eclipse.mosaic.fed.application.ambassador.ErrorRegister;
import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.TrafficLightApplication;
import org.eclipse.mosaic.fed.application.app.api.os.TrafficLightOperatingSystem;
import org.eclipse.mosaic.interactions.traffic.TrafficLightStateChange;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLight;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroup;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroupInfo;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightProgram;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightProgramPhase;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightState;
import org.eclipse.mosaic.lib.objects.v2x.etsi.cam.TrafficLightAwarenessData;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Simulation unit that represents a traffic light group.
 */
public class TrafficLightGroupUnit extends AbstractSimulationUnit implements TrafficLightOperatingSystem {

    /**
     * Traffic Light group, only used for this special type of RSU.
     */
    final private TrafficLightGroup trafficLightGroup;

    private TrafficLightGroupInfo trafficLightGroupInfo;

    private TrafficLightProgram currentProgram;

    /**
     * Creates a new traffic light group simulation unit.
     *
     * @param simulationUnitId     internal traffic light group identifier
     * @param trafficLightPosition traffic light position
     * @param trafficLightGroup    traffic light group identifier
     */
    public TrafficLightGroupUnit(
            String simulationUnitId, GeoPoint trafficLightPosition,
            TrafficLightGroup trafficLightGroup
    ) {
        super(simulationUnitId, trafficLightPosition);
        setRequiredOperatingSystem(TrafficLightOperatingSystem.class);

        this.trafficLightGroup = trafficLightGroup;
        this.currentProgram = Iterables.getLast(trafficLightGroup.getPrograms().values(), null);
    }

    @Override
    public GeoPoint getPosition() {
        return trafficLightGroup.getFirstPosition();
    }

    @Override
    public GeoPoint getInitialPosition() {
        return trafficLightGroup.getFirstPosition();
    }

    @Override
    public void processEvent(@Nonnull final Event event) throws Exception {
        // never remove the preProcessEvent call!
        final boolean preProcessed = super.preProcessEvent(event);

        // don't handle processed events
        if (preProcessed) {
            return;
        }

        final Object resource = event.getResource();

        // failsafe
        if (resource == null) {
            getOsLog().error("Event has no resource: {}", event);
            throw new RuntimeException(ErrorRegister.TRAFFIC_LIGHT_NoEventResource.toString());
        }

        if (resource instanceof TrafficLightGroupInfo) {
            onTrafficLightUpdate((TrafficLightGroupInfo) resource);
        } else {
            getOsLog().error("Unknown event resource: {}", event);
            throw new RuntimeException(ErrorRegister.TRAFFIC_LIGHT_UnknownEvent.toString());
        }
    }

    @Override
    public void setRemainingDurationOfCurrentPhase(long remainingDuration) {
        TrafficLightStateChange stateChangeInteraction = new TrafficLightStateChange(
                SimulationKernel.SimulationKernel.getCurrentSimulationTime(),
                getTrafficLightGroup().getGroupId()
        );

        stateChangeInteraction.setPhaseRemainingDuration(remainingDuration);
        sendInteractionToRti(stateChangeInteraction);
    }

    @Override
    public void switchToPhaseIndex(int newPhaseId) {
        checkPhaseIdValidity(currentProgram, newPhaseId);

        TrafficLightStateChange stateChangeInteraction = new TrafficLightStateChange(
                SimulationKernel.SimulationKernel.getCurrentSimulationTime(),
                getTrafficLightGroup().getGroupId()
        );

        stateChangeInteraction.setPhaseIndex(newPhaseId);
        sendInteractionToRti(stateChangeInteraction);
    }

    @Override
    public void switchToCustomState(List<TrafficLightState> stateList) {
        TrafficLightStateChange stateChangeInteraction = new TrafficLightStateChange(
                SimulationKernel.SimulationKernel.getCurrentSimulationTime(),
                getTrafficLightGroup().getGroupId()
        );

        stateChangeInteraction.setCustomState(stateList);
        sendInteractionToRti(stateChangeInteraction);
    }

    @Override
    public void switchToProgram(String newProgramId) {

        TrafficLightStateChange stateChangeInteraction = new TrafficLightStateChange(
                SimulationKernel.SimulationKernel.getCurrentSimulationTime(),
                getTrafficLightGroup().getGroupId()
        );

        stateChangeInteraction.setProgramId(newProgramId);
        sendInteractionToRti(stateChangeInteraction);
    }

    @Override
    public void switchToProgramAndPhase(String newProgramId, int phaseId) {
        checkPhaseIdValidity(trafficLightGroup.getProgramById(newProgramId), phaseId);

        TrafficLightStateChange stateChangeInteraction = new TrafficLightStateChange(
                SimulationKernel.SimulationKernel.getCurrentSimulationTime(),
                getTrafficLightGroup().getGroupId()
        );

        stateChangeInteraction.setProgramWithPhase(newProgramId, phaseId);
        sendInteractionToRti(stateChangeInteraction);
    }

    @Override
    public Collection<TrafficLightState> getSignalSequence(String trafficLightProgramId, int trafficLightId) {
        List<TrafficLightProgram> result = getAllPrograms().stream()
                .filter(program -> program.getProgramId().equals(trafficLightProgramId))
                .collect(Collectors.toList());
        if (!result.isEmpty()) {
            return result.get(0).getSignalSequence(trafficLightId);
        }

        return currentProgram.getSignalSequence(trafficLightId);
    }

    /**
     * Checks if the given phase id is valid for the given traffic light program
     * and throws an IllegalArgumentException if not.
     *
     * @param program traffic light program
     * @param phaseId program phase that should be check
     */
    private void checkPhaseIdValidity(TrafficLightProgram program, int phaseId) {
        if (phaseId < 0 || phaseId > (program.getPhases().size() - 1)) {
            throw new IllegalArgumentException("Given traffic light program " + program.getProgramId() + " doesn't have a phase with index " + phaseId);
        }
    }

    @Override
    public Collection<String> getControlledLanes() {
        //we need a set since different traffic lights can control same lanes
        //e.g. when one traffic light stands for driving forward and another for turning to the left
        Collection<String> controlledLanes = new LinkedHashSet<>();
        for (TrafficLight trafficLight : trafficLightGroup.getTrafficLights()) {
            controlledLanes.add(trafficLight.getIncomingLane());
        }
        return controlledLanes;
    }

    @Override
    public TrafficLightGroup getTrafficLightGroup() {
        return trafficLightGroup;
    }

    @Override
    public Collection<TrafficLightProgram> getAllPrograms() {
        return trafficLightGroup.getPrograms().values();
    }

    @Override
    public TrafficLightProgram getCurrentProgram() {
        return currentProgram;
    }

    @Override
    public TrafficLightProgramPhase getCurrentPhase() {
        return currentProgram.getCurrentPhase();
    }

    @Override
    public List<TrafficLight> getAllTrafficLights() {
        return trafficLightGroup.getTrafficLights();
    }

    @Override
    public CamBuilder assembleCamMessage(CamBuilder camBuilder) {
        camBuilder.awarenessData(new TrafficLightAwarenessData((this.getTrafficLightGroup())))
                .position(getPosition());

        for (CommunicationApplication communicationApplication : getApplicationsIterator(CommunicationApplication.class)) {
            communicationApplication.onCamBuilding(camBuilder);
        }
        return camBuilder;
    }

    private void onTrafficLightUpdate(final TrafficLightGroupInfo trafficLightGroupInfo) {
        // if the program should be set or changed, we set or change it
        if (currentProgram == null || !currentProgram.getProgramId().equals(trafficLightGroupInfo.getCurrentProgramId())) {
            currentProgram = trafficLightGroup.getPrograms().get(trafficLightGroupInfo.getCurrentProgramId());
        }

        // we also update the phase and the remaining duration of it
        currentProgram.setCurrentPhase(trafficLightGroupInfo.getCurrentPhaseIndex());
        currentProgram.getCurrentPhase().setRemainingDuration(trafficLightGroupInfo.getAssumedTimeOfNextSwitch() - getSimulationTime());

        if (trafficLightGroupInfo.getCurrentState() != null) {
            adjustTrafficLightsToPhaseChange(trafficLightGroupInfo.getCurrentState(), getAllTrafficLights());
        } else {
            adjustTrafficLightsToPhaseChange(currentProgram.getCurrentPhase().getStates(), getAllTrafficLights());
        }
        // if trafficLightGroupInfo hasn't been set, previous and current state will be the same
        TrafficLightGroupInfo previousState = ObjectUtils.defaultIfNull(this.trafficLightGroupInfo, trafficLightGroupInfo);
        this.trafficLightGroupInfo = trafficLightGroupInfo;

        for (TrafficLightApplication application : getApplicationsIterator(TrafficLightApplication.class)) {
            application.onTrafficLightGroupUpdated(previousState, trafficLightGroupInfo);
        }
    }

    /**
     * Changes the states of the given traffic lights according to the list of states.
     */
    private void adjustTrafficLightsToPhaseChange(List<TrafficLightState> newPhaseStates, List<TrafficLight> trafficLightsToAdjust) {
        Iterator<TrafficLightState> statesIterator = newPhaseStates.iterator();
        Iterator<TrafficLight> trafficLightsIterator = trafficLightsToAdjust.iterator();
        while (statesIterator.hasNext() && trafficLightsIterator.hasNext()) {
            TrafficLightState state = statesIterator.next();
            TrafficLight trafficLight = trafficLightsIterator.next();
            trafficLight.setCurrentState(state);
        }
    }
}
