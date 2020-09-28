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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.fed.application.ambassador.simulation.VehicleParameters.VehicleParametersChangeRequest;
import org.eclipse.mosaic.lib.enums.LaneChangeMode;
import org.eclipse.mosaic.lib.enums.SpeedMode;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleParameter;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleParameter.VehicleParameterType;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;

import org.junit.Test;

import java.awt.Color;
import java.util.Collection;

/**
 * Test suite to validate Vehicle Behavior.
 */
public class VehicleBehaviorTest {

    private final VehicleType vehicleType = new VehicleType("PKW");

    @Test
    public void initialValues() {
        VehicleType vehicleType = spy(this.vehicleType);
        when(vehicleType.getColor()).thenReturn("yellow");

        final VehicleParameters behavior = new VehicleParameters(vehicleType);

        assertEquals(vehicleType.getSigma(), behavior.getImperfection(), 0.001d);
        assertEquals(vehicleType.getTau(), behavior.getReactionTime(), 0.001d);
        assertEquals(vehicleType.getAccel(), behavior.getMaxAcceleration(), 0.001d);
        assertEquals(vehicleType.getDecel(), behavior.getMaxDeceleration(), 0.001d);
        assertEquals(vehicleType.getEmergencyDecel(), behavior.getEmergencyDeceleration(), 0.001d);
        assertEquals(vehicleType.getMaxSpeed(), behavior.getMaxSpeed(), 0.001d);
        assertEquals(vehicleType.getMinGap(), behavior.getMinimumGap(), 0.001d);
        assertEquals(LaneChangeMode.DEFAULT, behavior.getLaneChangeMode());
        assertEquals(SpeedMode.DEFAULT, behavior.getSpeedMode());
        assertEquals(Color.YELLOW, behavior.getVehicleColor());
        assertEquals(1d, behavior.getSpeedFactor(), 0.001d);
    }

    @Test
    public void changeSpecificValues() {
        final VehicleParameters behavior = new VehicleParameters(vehicleType);

        final VehicleParametersChangeRequest changeRequest = new VehicleParametersChangeRequest(null, behavior);

        // RUN
        changeRequest.changeMaxSpeed(30d).changeMaxAcceleration(2.1).changeMaxDeceleration(3.4);

        // ASSERT (new behavior object)
        final VehicleParameters newBehavior = changeRequest.getUpdatedBehavior();
        assertEquals(behavior.getImperfection(), newBehavior.getImperfection(), 0.001d);
        assertEquals(behavior.getReactionTime(), newBehavior.getReactionTime(), 0.001d);
        assertEquals(2.1, newBehavior.getMaxAcceleration(), 0.001d);
        assertEquals(3.4, newBehavior.getMaxDeceleration(), 0.001d);
        assertEquals(30d, newBehavior.getMaxSpeed(), 0.001d);
        assertEquals(behavior.getMinimumGap(), newBehavior.getMinimumGap(), 0.001d);
        assertEquals(behavior.getLaneChangeMode(), newBehavior.getLaneChangeMode());
        assertEquals(behavior.getSpeedMode(), newBehavior.getSpeedMode());
        assertEquals(behavior.getSpeedFactor(), newBehavior.getSpeedFactor(), 0.001d);

        // ASSERT (parameter changes)
        assertEquals(3, changeRequest.getChangedParameters().size());
        assertParameterChange(VehicleParameterType.MAX_ACCELERATION, 2.1d, changeRequest.getChangedParameters());
        assertParameterChange(VehicleParameterType.MAX_DECELERATION, 3.4d, changeRequest.getChangedParameters());
        assertParameterChange(VehicleParameterType.MAX_SPEED, 30d, changeRequest.getChangedParameters());
    }

    @Test
    public void changeAllValues() {
        final VehicleParameters behavior = new VehicleParameters(vehicleType);

        final VehicleParametersChangeRequest changeRequest = new VehicleParametersChangeRequest(null, behavior);

        // RUN
        changeRequest.changeImperfection(0.33d);
        changeRequest.changeReactionTime(1.5d);
        changeRequest.changeMaxSpeed(60d);
        changeRequest.changeMaxAcceleration(1.8d);
        changeRequest.changeMaxDeceleration(2.8d);
        changeRequest.changeEmergencyDeceleration(3.0d);
        changeRequest.changeMinimumGap(5d);
        changeRequest.changeSpeedFactor(1.2d);
        changeRequest.changeLaneChangeMode(LaneChangeMode.COOPERATIVE);
        changeRequest.changeSpeedMode(SpeedMode.AGGRESSIVE);

        // ASSERT (new behavior object)
        final VehicleParameters newBehavior = changeRequest.getUpdatedBehavior();
        assertEquals(0.33d, newBehavior.getImperfection(), 0.001d);
        assertEquals(1.5d, newBehavior.getReactionTime(), 0.001d);
        assertEquals(1.8d, newBehavior.getMaxAcceleration(), 0.001d);
        assertEquals(2.8d, newBehavior.getMaxDeceleration(), 0.001d);
        assertEquals(3.0d, newBehavior.getEmergencyDeceleration(), 0.001d);
        assertEquals(60d, newBehavior.getMaxSpeed(), 0.001d);
        assertEquals(5d, newBehavior.getMinimumGap(), 0.001d);
        assertEquals(LaneChangeMode.COOPERATIVE, newBehavior.getLaneChangeMode());
        assertEquals(SpeedMode.AGGRESSIVE, newBehavior.getSpeedMode());
        assertEquals(1.2d, newBehavior.getSpeedFactor(), 0.001d);

        // ASSERT (parameter changes)
        assertEquals(10, changeRequest.getChangedParameters().size());
        assertParameterChange(VehicleParameterType.IMPERFECTION, 0.33d, changeRequest.getChangedParameters());
        assertParameterChange(VehicleParameterType.REACTION_TIME, 1.5d, changeRequest.getChangedParameters());
        assertParameterChange(VehicleParameterType.MAX_ACCELERATION, 1.8d, changeRequest.getChangedParameters());
        assertParameterChange(VehicleParameterType.MAX_DECELERATION, 2.8d, changeRequest.getChangedParameters());
        assertParameterChange(VehicleParameterType.EMERGENCY_DECELERATION, 3.0d, changeRequest.getChangedParameters());
        assertParameterChange(VehicleParameterType.MAX_SPEED, 60d, changeRequest.getChangedParameters());
        assertParameterChange(VehicleParameterType.MIN_GAP, 5d, changeRequest.getChangedParameters());
        assertParameterChange(VehicleParameterType.SPEED_FACTOR, 1.2d, changeRequest.getChangedParameters());
        assertParameterChange(VehicleParameterType.LANE_CHANGE_MODE, LaneChangeMode.COOPERATIVE, changeRequest.getChangedParameters());
        assertParameterChange(VehicleParameterType.SPEED_MODE, SpeedMode.AGGRESSIVE, changeRequest.getChangedParameters());
    }

    @Test(expected = IllegalStateException.class)
    public void changeAfterRequestApplied_throwException() {
        final VehicleParameters behavior = new VehicleParameters(vehicleType);

        // SETUP
        final VehicleParametersChangeRequest changeRequest = new VehicleParametersChangeRequest(null, behavior);
        changeRequest.getUpdatedBehavior(); // <- invalidates the request, no changes are allowed anymore

        // RUN (is expected to throw exception)
        changeRequest.changeLaneChangeMode(LaneChangeMode.DEFAULT);
    }

    private void assertParameterChange(VehicleParameterType expectedParameterKey, Object expectedParameterValue,
                                       Collection<VehicleParameter> vehicleParameters) {
        for (VehicleParameter parameter : vehicleParameters) {
            if (parameter.getParameterType() == expectedParameterKey) {
                assertEquals(expectedParameterValue, parameter.getValue());
                return;
            }
        }
        fail(expectedParameterKey + " not found in changed parameters");
    }

}
