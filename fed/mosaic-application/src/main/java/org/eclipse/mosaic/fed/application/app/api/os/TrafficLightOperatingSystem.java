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

package org.eclipse.mosaic.fed.application.app.api.os;

import org.eclipse.mosaic.fed.application.ambassador.util.EventNicenessPriorityRegister;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLight;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroup;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightProgram;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightProgramPhase;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightState;

import java.util.Collection;
import java.util.List;

/**
 * Important: when using the methods of this OS be aware that the method
 * {@link org.eclipse.mosaic.fed.application.ambassador.simulation.TrafficLightGroupUnit}
 * {@code #onTrafficLightUpdate(TrafficLightGroupInfo)}
 * that will be called after receiving traffic light subscription results from SUMO,
 * has higher priority (see {@link EventNicenessPriorityRegister})
 * than processing of a not specific event in an application which doesn't happen inside of {@link AbstractApplication#onStartup()} method
 * and therefore is called *before* the event is processed in an application using this OS.
 */
public interface TrafficLightOperatingSystem extends OperatingSystem {

    /**
     * Returns all traffic light programs that are registered for a traffic light group using this operating system.
     *
     * @return all traffic light programs that are registered for a traffic light group
     */
    Collection<TrafficLightProgram> getAllPrograms();

    /**
     * Returns the traffic light program that is currently running on a traffic light group using this operating system.
     *
     * @return the current traffic light program
     */
    TrafficLightProgram getCurrentProgram();

    /**
     * Returns the traffic light program phase that is currently running on a traffic light group using this operating system.
     *
     * @return the current traffic light program phase
     */
    TrafficLightProgramPhase getCurrentPhase();

    /**
     * Returns all traffic lights a traffic light group using this operating system consist of
     *
     * @return all traffic lights within a traffic light group
     */
    Collection<TrafficLight> getAllTrafficLights();

    /**
     * Returns a sequence of states for a certain traffic light,
     * where each state represents a state of the traffic light during a phase within a traffic light program.
     * If there is no program with given id, a sequence of states for the current program is returned.
     *
     * @param trafficLightProgramId id of the traffic light program to look into
     * @param trafficLightId        id of a traffic light
     * @return a sequence of states for a certain traffic light within a traffic light program with given id
     * or a sequence of states for the current program.
     */
    Collection<TrafficLightState> getSignalSequence(String trafficLightProgramId, int trafficLightId);

    /**
     * Switches the phase in the current traffic light program to the phase with the given id.
     * Remaining phase duration will be reset.
     *
     * @param newPhaseId id of the phase it will be switched to
     */
    void switchToPhaseIndex(int newPhaseId);

    /**
     * Set the remaining duration of the current traffic light program phase of this traffic light
     * group.
     * Please consider only using numbers that fulfill the condition >= 0 to avoid unwanted changes.
     * Negative numbers usage will affect the next phase
     * so that the absolute value of the given negative number will be subtracted from the remaining duration of the next phase.
     *
     * @param remainingDuration the new phase remaining duration in millisecond
     */
    void setRemainingDurationOfCurrentPhase(long remainingDuration);

    /**
     * Switches all traffic lights which belong to this group to the given
     * state. The states will be held until switched to another list of states,
     * or to another program or phase index.
     *
     * @param stateList the list of states to set (one state for each traffic light in the correct order)
     */
    void switchToCustomState(List<TrafficLightState> stateList);

    /**
     * Sets a new traffic light program, identified by the given {@code newProgramId}, to
     * this traffic light group.
     * Important: when interacting with SUMO one should consider that after changing a program
     * the new program will NOT begin from its first phase,
     * but from the time at which the previous program was interrupted (and from the according phase).
     * Consider using the {@link #switchToProgramAndPhase(String newProgramId, int phaseId)} method to avoid this effect.
     * Therefore changing the program to the same program doesn't change anything.
     *
     * @param newProgramId Id of the new traffic light program.
     */
    void switchToProgram(String newProgramId);

    /**
     * Sets a new traffic light program with the given id to this traffic light group
     * and also sets the wanted phase for the new program.
     * Important: Internal this is made with *two* TraCI commands
     * because it's not possible to do it in one step while interacting with SUMO.
     *
     * @param newProgramId Id of the new traffic light program
     * @param phaseIndex   the index of the phase to switch to
     */
    void switchToProgramAndPhase(String newProgramId, int phaseIndex);

    /**
     * Returns a collection of incoming lanes controlled by this traffic light group.
     *
     * @return controlled lanes
     */
    Collection<String> getControlledLanes();

    /**
     * Getter for the traffic light group.
     *
     * @return Traffic light group
     */
    TrafficLightGroup getTrafficLightGroup();
}
