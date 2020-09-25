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

package org.eclipse.mosaic.lib.objects.trafficlight;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrafficLightGroupTest {

    private TrafficLightGroup trafficLightGroup;
    private List<TrafficLight> trafficLights;
    private TrafficLightProgram program;

    @Before
    public void constructTrafficLightGroup() {
        Map<String, TrafficLightProgram> programs = new HashMap<>();

        TrafficLightState state1 = new TrafficLightState(false, true, false);
        TrafficLightState state2 = new TrafficLightState(true, false, true);
        List<TrafficLightProgramPhase> phases = Lists.newArrayList(new TrafficLightProgramPhase(0, 1_000_000_000, Lists.newArrayList(state1, state2)),
                new TrafficLightProgramPhase(1, 2_000_000_000, Lists.newArrayList(state2, state1)));
        program = new TrafficLightProgram("0", phases, 0);
        programs.put("0", program);

        trafficLights = Lists.newArrayList(
                new TrafficLight(0,
                    GeoPoint.latLon(0.0, 0.0),
                    //just examples, may not match the real lanes
                    "32935480_21677261_21668930_21677261_0",
                    "32935480_21668930_27537748_21668930_0",
                    state1),

                new TrafficLight(1,
                        GeoPoint.latLon(0.0, 0.0),
                        //just examples, may not match the real lanes
                        "32935480_21677261_21668930_21677261_1",
                        "32935480_21668930_27537748_21668930_1",
                        state2));

        //id is an example from Tiergarten scenario
        trafficLightGroup = new TrafficLightGroup("21668930", programs, trafficLights);
    }

    @Test
    public void getGroupId() {
        assertEquals("The id of a traffic light group didn't match the expected one", "21668930", trafficLightGroup.getGroupId());
    }

    @Test
    public void getTrafficLights() {
        assertEquals("The traffic lights included in a traffic light group didn't match the expected ones", trafficLights, trafficLightGroup.getTrafficLights());
    }

    @Test
    public void getTrafficLightPrograms() {
        assertEquals("The traffic lights included in a traffic light group didn't match the expected ones", program, trafficLightGroup.getPrograms().get("0"));
    }

    @Test
    public void getGeoPosition() {
        //we just get a position of the first traffic light in the group
        assertEquals(GeoPoint.latLon(0.0, 0.0), trafficLightGroup.getFirstPosition());
    }

    @Test
    public void testSerializationRoundtrip() throws IOException, InternalFederateException {
        // RUN + ASSERT
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
        try (DataOutputStream dataOutputStream = new DataOutputStream(baos)) {
            trafficLightGroup.toDataOutput(dataOutputStream);
        }
        byte[] bytes = baos.toByteArray();

        TrafficLightGroup roundtripGroup;
        try (DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bytes))) {
            roundtripGroup = new TrafficLightGroup(dataInputStream);
        }

        assertEquals(trafficLightGroup.getGroupId(), roundtripGroup.getGroupId());
        assertEquals(trafficLightGroup.getFirstPosition(), roundtripGroup.getFirstPosition());
        assertEquals(trafficLightGroup.getPrograms().size(), roundtripGroup.getPrograms().size());
        for (int i = 0; i < trafficLightGroup.getPrograms().size(); i++) {
            assertEquals(trafficLightGroup.getProgramById(i + "").getProgramId(), roundtripGroup.getProgramById(i + "").getProgramId());
            assertEquals(trafficLightGroup.getProgramById(i + "").getCurrentPhaseIndex(), roundtripGroup.getProgramById(i + "").getCurrentPhaseIndex());
            assertEquals(trafficLightGroup.getProgramById(i + "").getProgramDuration(), roundtripGroup.getProgramById(i + "").getProgramDuration());
            assertEquals(trafficLightGroup.getProgramById(i + "").getPhases().size(), roundtripGroup.getProgramById(i + "").getPhases().size());

            for (int j = 0; j < trafficLightGroup.getProgramById(i + "").getPhases().size(); j++) {
                assertEquals(trafficLightGroup.getProgramById(i + "").getPhases().get(j).getIndex(), roundtripGroup.getProgramById(i + "").getPhases().get(j).getIndex());
                assertEquals(trafficLightGroup.getProgramById(i + "").getPhases().get(j).getConfiguredDuration(), roundtripGroup.getProgramById(i + "").getPhases().get(j).getConfiguredDuration());
                assertEquals(trafficLightGroup.getProgramById(i + "").getPhases().get(j).getRemainingDuration(), roundtripGroup.getProgramById(i + "").getPhases().get(j).getRemainingDuration());
                assertTrue(trafficLightGroup.getProgramById(i + "").getPhases().get(j).equalsOtherPhase(roundtripGroup.getProgramById(i + "").getPhases().get(j)));
            }
        }

    }

}