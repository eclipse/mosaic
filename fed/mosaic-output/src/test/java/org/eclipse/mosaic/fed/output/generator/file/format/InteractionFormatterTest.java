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

package org.eclipse.mosaic.fed.output.generator.file.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.mosaic.interactions.communication.V2xMessageReception;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.junit.IpResolverRule;
import org.eclipse.mosaic.lib.objects.addressing.AdHocMessageRoutingBuilder;
import org.eclipse.mosaic.lib.objects.addressing.IpResolver;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for the {@link InteractionFormatter}.
 */
public class InteractionFormatterTest {

    @Rule
    public IpResolverRule ipresolverRule = new IpResolverRule();

    private List<String> vehicleUpdatesMethods;
    private Map<String, List<List<String>>> vehicleUpdatesDef;
    private VehicleUpdates vehicleUpdates;
    private VehicleUpdates moveUpdates2;
    private VehicleUpdates vehicleUpdates3;
    private VehicleUpdates vehicleUpdates4;
    private VehicleUpdates vehicleUpdatesNull1;
    private VehicleUpdates vehicleUpdatesNull2;
    private V2xMessageReception recvInteraction;
    private MyInteraction interactionCollection;
    private VehicleData v1;
    private VehicleData v2;
    private VehicleData v3;
    private VehicleData v4;
    private VehicleData v5;
    private VehicleData v6NullPos;

    @Before
    public void setUp() {
        v1 = new VehicleData.Builder(111, "1").position(GeoPoint.lonLat(11, 10), null).create();
        v2 = new VehicleData.Builder(222, "2").position(GeoPoint.lonLat(22, 20), null).create();
        v3 = new VehicleData.Builder(333, "3").position(GeoPoint.lonLat(33, 30), null).create();
        v4 = new VehicleData.Builder(444, "4").position(GeoPoint.lonLat(44, 40), null).create();
        v5 = new VehicleData.Builder(555, "5").position(GeoPoint.lonLat(44, 40), null).create();
        v6NullPos = new VehicleData.Builder(444, "6").position(null, null).create();

        List<VehicleData> add = new ArrayList<>();
        List<VehicleData> update = new ArrayList<>();
        List<VehicleData> update1 = new ArrayList<>();
        List<VehicleData> update2 = new ArrayList<>();
        List<VehicleData> updateNullPosition = new ArrayList<>();
        List<String> remove = new ArrayList<>();

        update.add(v1);
        update.add(v2);
        add.add(v3);
        add.add(v4);
        update1.add(v1);
        update1.add(v5);
        update1.add(v2);
        update2.add(v5);
        update2.add(v2);
        updateNullPosition.add(v6NullPos);
        updateNullPosition.add(v1);

        vehicleUpdates = new VehicleUpdates(0, add, update, remove);
        moveUpdates2 = new VehicleUpdates(1300000000, update, add, remove);
        vehicleUpdates3 = new VehicleUpdates(1400000000, add, update1, remove);
        vehicleUpdates4 = new VehicleUpdates(1500000000, add, update2, remove);
        vehicleUpdatesNull1 = new VehicleUpdates(1600000000, null, null, null);
        vehicleUpdatesNull2 = new VehicleUpdates(1700000000, null, updateNullPosition, null);

        IpResolver.getSingleton().registerHost("veh_0");

        // ======================================================

        AdHocMessageRoutingBuilder adHocMessageRoutingBuilder = new AdHocMessageRoutingBuilder("veh_0", null);
        MessageRouting messageRouting = adHocMessageRoutingBuilder.channel(AdHocChannel.CCH).broadcast().topological().build();

        V2xMessage em = new V2xMessage.Empty(messageRouting);

        // ======================================================

        recvInteraction = new V2xMessageReception(2, "5", em.getId(), null);

        // ======================================================

        List<VehicleUpdates> interactions = new ArrayList<>();
        interactions.add(vehicleUpdates);
        interactions.add(moveUpdates2);

        interactionCollection = new MyInteraction(interactions);

        // ======================================================

        vehicleUpdatesDef = new HashMap<>();

        List<List<String>> list = new ArrayList<>();

        vehicleUpdatesMethods = new ArrayList<>();

        list.add(vehicleUpdatesMethods);

        vehicleUpdatesDef.put("VehicleUpdates", list);
    }

    @Test
    public void testNormalIteration() throws Exception {

        vehicleUpdatesMethods.clear();
        vehicleUpdatesMethods.add("\"Test\"");
        vehicleUpdatesMethods.add("Time");
        vehicleUpdatesMethods.add("Updated:Name");
        vehicleUpdatesMethods.add("Updated:Position.Latitude");
        vehicleUpdatesMethods.add("Updated:Position.Longitude");

        InteractionFormatter interactionFormatter = new InteractionFormatter(';', '.', vehicleUpdatesDef);
        String interaction = interactionFormatter.format(vehicleUpdates);
        String[] expected = {"Test;0;1;10.0;11.0\n", "Test;0;2;20.0;22.0\n"};

        assertEquals(expected[0] + expected[1], interaction);
    }

    @Test
    public void testNormalIterationDifferentDecimalSeperator() throws Exception {

        vehicleUpdatesMethods.clear();
        vehicleUpdatesMethods.add("\"Test\"");
        vehicleUpdatesMethods.add("Time");
        vehicleUpdatesMethods.add("Updated:Name");
        vehicleUpdatesMethods.add("Updated:Position.Latitude");
        vehicleUpdatesMethods.add("Updated:Position.Longitude");

        InteractionFormatter interactionFormatter = new InteractionFormatter(';', ',', vehicleUpdatesDef);
        String interaction = interactionFormatter.format(vehicleUpdates);
        String[] expected = {"Test;0;1;10,0;11,0\n", "Test;0;2;20,0;22,0\n"};

        assertEquals(expected[0] + expected[1], interaction);
    }

    @Test
    public void testFiltering_minMax() throws Exception {

        vehicleUpdatesMethods.clear();
        vehicleUpdatesMethods.add("\"Test\"");
        vehicleUpdatesMethods.add("Time[min=1200000000,max=1400000000]");
        vehicleUpdatesMethods.add("Updated:Name");

        InteractionFormatter interactionFormatter = new InteractionFormatter(';', '.', vehicleUpdatesDef);

        assertEquals("", interactionFormatter.format(vehicleUpdates));
        assertEquals("Test;1300000000;3\nTest;1300000000;4\n", interactionFormatter.format(moveUpdates2));
        assertEquals("Test;1400000000;1\nTest;1400000000;5\nTest;1400000000;2\n", interactionFormatter.format(vehicleUpdates3));
        assertEquals("", interactionFormatter.format(vehicleUpdates4));
    }

    @Test
    public void testFiltering_max_eq() throws Exception {

        vehicleUpdatesMethods.clear();
        vehicleUpdatesMethods.add("\"Test\"");
        vehicleUpdatesMethods.add("Time[max=1400000000]");
        vehicleUpdatesMethods.add("Updated:Name[eq=1]");

        InteractionFormatter interactionFormatter = new InteractionFormatter(';', '.', vehicleUpdatesDef);

        assertEquals("Test;0;1\n", interactionFormatter.format(vehicleUpdates));
        assertEquals("", interactionFormatter.format(moveUpdates2));
        assertEquals("Test;1400000000;1\n", interactionFormatter.format(vehicleUpdates3));
        assertEquals("", interactionFormatter.format(vehicleUpdates4));
    }

    @Test
    public void testFiltering_regex() throws Exception {

        vehicleUpdatesMethods.clear();
        vehicleUpdatesMethods.add("\"Test\"");
        vehicleUpdatesMethods.add("Time");
        vehicleUpdatesMethods.add("Updated:Name[regex=[1-2]{1}]");

        InteractionFormatter interactionFormatter = new InteractionFormatter(';', '.', vehicleUpdatesDef);

        assertEquals("Test;0;1\nTest;0;2\n", interactionFormatter.format(vehicleUpdates));
        assertEquals("", interactionFormatter.format(moveUpdates2));
        assertEquals("Test;1400000000;1\nTest;1400000000;2\n", interactionFormatter.format(vehicleUpdates3));
        assertEquals("Test;1500000000;2\n", interactionFormatter.format(vehicleUpdates4));
    }


    @Test
    public void testTwoIterators() throws Exception {

        vehicleUpdatesMethods.clear();
        vehicleUpdatesMethods.add("\"Test\"");
        vehicleUpdatesMethods.add("Updated:Name");
        vehicleUpdatesMethods.add("Added:Name");

        InteractionFormatter interactionFormatter = new InteractionFormatter(';', '.', vehicleUpdatesDef);

        String[] expected = {"Test;1;3\n", "Test;1;4\n", "Test;2;3\n", "Test;2;4\n"};

        assertEquals(expected[0] + expected[1] + expected[2] + expected[3], interactionFormatter.format(vehicleUpdates));
    }


    @Test
    public void testTwoLevelsIteration() throws Exception {
        Map<String, List<List<String>>> interactionDef = new HashMap<>();

        List<List<String>> list = new ArrayList<>();

        ArrayList<String> methods = new ArrayList<>();

        methods.add("MessageList:Updated:Name");

        list.add(methods);

        interactionDef.put("MyInteraction", list);

        InteractionFormatter interactionFormatter = new InteractionFormatter(';', '.', interactionDef);

        String[] expected = {"1\n", "2\n", "3\n", "4\n"};

        assertEquals(expected[0] + expected[1] + expected[2] + expected[3], interactionFormatter.format(interactionCollection));
    }


    @Test
    public void testExtendedMethod1() throws Exception {
        Map<String, List<List<String>>> recvInteractiongDef = new HashMap<>();

        List<List<String>> list = new ArrayList<>();

        ArrayList<String> methods = new ArrayList<>();

        methods.add("\"RECV\"");
        methods.add("ReceiverName");

        list.add(methods);

        recvInteractiongDef.put("V2xMessageReception", list);

        InteractionFormatter interactionFormatter = new InteractionFormatter(';', '.', recvInteractiongDef);

        String[] expected = {"RECV;5\n"};

        assertEquals(expected[0], interactionFormatter.format(recvInteraction));
    }


    @Test
    public void testInvalidMessageType() throws Exception {
        Map<String, List<List<String>>> interactionDef = new HashMap<>();
        List<List<String>> list = new ArrayList<>();
        ArrayList<String> methods = new ArrayList<>();
        list.add(methods);

        interactionDef.put("VehicleUpdates", list);
        new InteractionFormatter(';', '.', interactionDef);
        interactionDef.clear();

        interactionDef.put("V2xMessageReception", list);
        new InteractionFormatter(';', '.', interactionDef);
        interactionDef.clear();

        interactionDef.put("V2xMessageTransmission", list);
        new InteractionFormatter(';', '.', interactionDef);
        interactionDef.clear();

        interactionDef.put("TrafficLightRegistration", list);
        new InteractionFormatter(';', '.', interactionDef);
        interactionDef.clear();

        interactionDef.put("RsuRegistration", list);
        new InteractionFormatter(';', '.', interactionDef);
        interactionDef.clear();

        interactionDef.put("VehicleRegistration", list);
        new InteractionFormatter(';', '.', interactionDef);
        interactionDef.clear();

        try {
            interactionDef.put("Interaction", list);
            new InteractionFormatter(';', '.', interactionDef);
            fail();
        } catch (ClassNotFoundException e) {
            assertEquals("Interaction is an unknown parameter in the visualizeMessage method set.", e.getMessage());
        }
    }

    @Test
    public void testWrongMethodDefinitions() throws Exception {

        vehicleUpdatesMethods.clear();
        vehicleUpdatesMethods.add("");
        try {
            new InteractionFormatter(';', '.', vehicleUpdatesDef);
            fail();
        } catch (NoSuchMethodException e) {
            assertEquals("Method() is not supported "
                    + "in either basic or extended method set of "
                    + "org.eclipse.mosaic.interactions.traffic.VehicleUpdates", e.getMessage());
        }

        vehicleUpdatesMethods.clear();
        vehicleUpdatesMethods.add("Time.ID");
        try {
            new InteractionFormatter(';', '.', vehicleUpdatesDef);
            fail();
        } catch (NoSuchMethodException e) {
            assertEquals("Method(ID) is not supported "
                    + "in either basic or extended method set of "
                    + "long", e.getMessage());
        }

        vehicleUpdatesMethods.clear();
        vehicleUpdatesMethods.add("Time.");
        try {
            new InteractionFormatter(';', '.', vehicleUpdatesDef);
            fail();
        } catch (NoSuchMethodException e) {
            assertEquals("Method() is not supported "
                    + "in either basic or extended method set of "
                    + "long", e.getMessage());
        }

        vehicleUpdatesMethods.clear();
        vehicleUpdatesMethods.add("Updated:What");
        try {
            new InteractionFormatter(';', '.', vehicleUpdatesDef);
            fail();
        } catch (NoSuchMethodException e) {
            assertEquals("Method(What) is not supported"
                    + " in either basic or extended method set of"
                    + " org.eclipse.mosaic.lib.objects.vehicle.VehicleData", e.getMessage());
        }

        vehicleUpdatesMethods.clear();
        vehicleUpdatesMethods.add("Updated:Position:Latitude");
        try {
            new InteractionFormatter(';', '.', vehicleUpdatesDef);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Method defined by (Position) does not return a Collection.", e.getMessage());
        }

    }

    @Test
    public void testNullPointer() throws Exception {
        vehicleUpdatesMethods.clear();
        vehicleUpdatesMethods.add("\"Test\"");
        vehicleUpdatesMethods.add("Time");
        vehicleUpdatesMethods.add("Updated:Name");
        vehicleUpdatesMethods.add("Updated:Position.Latitude");
        vehicleUpdatesMethods.add("Updated:Position.Longitude");

        InteractionFormatter interactionFormatter = new InteractionFormatter(';', '.', vehicleUpdatesDef);

        String[] expected = {"Test;1600000000;null;null;null\n",
                "Test;1700000000;6;null;null\n",
                "Test;1700000000;1;10.0;11.0\n",
                "Test;1700000000;1;null;null\n"};

        assertEquals(expected[0], interactionFormatter.format(vehicleUpdatesNull1));

        assertEquals(expected[1] + expected[2], interactionFormatter.format(vehicleUpdatesNull2));

        vehicleUpdatesMethods.clear();
        vehicleUpdatesMethods.add("\"Test\"");
        vehicleUpdatesMethods.add("Time");
        vehicleUpdatesMethods.add("Updated:Name");
        vehicleUpdatesMethods.add("Added:Position.Latitude");
        vehicleUpdatesMethods.add("Added:Position.Longitude");

        interactionFormatter = new InteractionFormatter(';', '.', vehicleUpdatesDef);

        assertEquals(expected[1] + expected[3], interactionFormatter.format(vehicleUpdatesNull2));


    }
}
