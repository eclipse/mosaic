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

package org.eclipse.mosaic.fed.sns.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.fed.sns.ambassador.SimulationNode;
import org.eclipse.mosaic.lib.geo.CartesianArea;
import org.eclipse.mosaic.lib.geo.CartesianCircle;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.junit.GeoProjectionRule;
import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.model.delay.ConstantDelay;
import org.eclipse.mosaic.lib.model.delay.Delay;
import org.eclipse.mosaic.lib.model.delay.SimpleRandomDelay;
import org.eclipse.mosaic.lib.model.transmission.CTransmission;
import org.eclipse.mosaic.lib.model.transmission.TransmissionResult;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AdhocTransmissionModelTest {

    private SimpleAdhocTransmissionModel simpleTransmissionModel;
    private AdhocTransmissionModel sophisticatedTransmissionModel;

    private final RandomNumberGenerator rng = new DefaultRandomNumberGenerator(82937858189209L);

    private final Map<String, SimulationNode> allNodes = new HashMap<>();
    private final Set<String> nodesInRow = new HashSet<>();
    private final Map<String, SimulationNode> nodesRandomlyDistributed = new HashMap<>();

    @Rule
    public GeoProjectionRule coordinateTransformationRule = new GeoProjectionRule(GeoPoint.lonLat(15.48, 47.02));

    @Before
    public void setup() {

        simpleTransmissionModel = new SimpleAdhocTransmissionModel();
        ConstantDelay constantDelay = new ConstantDelay();
        constantDelay.delay = 1; // used to obtain single valued delays
        simpleTransmissionModel.simpleMultihopDelay = constantDelay;
        simpleTransmissionModel.simpleMultihopTransmission = new CTransmission();

        sophisticatedTransmissionModel = new SophisticatedAdhocTransmissionModel();

        double currentLatitude = 47.0174568;
        double currentLongitude = 15.4797683;
        double adhocRadius = 155d;
        // generate simulation entities, with constant latitudinal offset
        for (int i = 0; i < 21; ++i) {
            currentLatitude += 0.001;
            SimulationNode newNode = mock(SimulationNode.class);
            when(newNode.getPosition()).thenReturn(GeoPoint.latLon(currentLatitude, currentLongitude).toCartesian());
            when(newNode.getRadius()).thenReturn(adhocRadius);
            allNodes.put("" + i, newNode);
            nodesInRow.add("" + i);
        }

        double randomLatitude;
        double randomLongitude;
        // generate randomly distributed entities behind last entity
        for (int i = 21; i < 31; ++i) {
            randomLatitude = rng.nextDouble(currentLatitude, currentLatitude + 0.005);
            randomLongitude = rng.nextDouble(currentLongitude - 0.0025, currentLongitude + 0.0025);
            SimulationNode newNode = mock(SimulationNode.class);
            when(newNode.getPosition()).thenReturn(GeoPoint.latLon(randomLatitude, randomLongitude).toCartesian());
            when(newNode.getRadius()).thenReturn(adhocRadius);
            allNodes.put("" + i, newNode);
            nodesRandomlyDistributed.put("" + i, newNode);
        }
    }

    @Test
    public void simulateSingleHopBroadCast_allUnitsReachable_noPacketLoss() {
        // SETUP
        for (Map.Entry<String, SimulationNode> entry : allNodes.entrySet()) { // make all units reachable in radius
            when(entry.getValue().getRadius()).thenReturn(Double.MAX_VALUE);
        }

        Delay simpleRandomDelay = new SimpleRandomDelay();
        CTransmission transmission = new CTransmission();

        // Note: ttl parameter doesn't matter for direct transmission, the SNS would log a warning if TTL is != 1
        TransmissionParameter transmissionParameter = new TransmissionParameter(
                rng, simpleRandomDelay, transmission, 1
        );

        // RUN
        // use all entities as receivers
        Map<String, TransmissionResult> directTransmissionResults =
                simpleTransmissionModel.simulateTopocast("0", allNodes, transmissionParameter, allNodes);

        // ASSERT
        // unit without wifi capabilities can't receive transmission
        for (String entityName : directTransmissionResults.keySet()) {
            assertTrue(directTransmissionResults.get(entityName).success);
        }
    }

    @Test
    public void simulateSingleHopBroadCast_someUnitsReachable_noPacketLoss() {
        // SETUP
        Delay simpleRandomDelay = new SimpleRandomDelay();

        CTransmission transmission = new CTransmission();

        // Note: ttl parameter doesn't matter for direct transmission, the SNS would log a warning if TTL is != 1
        TransmissionParameter transmissionParameter = new TransmissionParameter(rng, simpleRandomDelay, transmission, 1
        );

        // RUN
        // increase range of sender
        when(allNodes.get("0").getRadius()).thenReturn(300d);
        // only use Receivers in range, this will be enforced by the transmission simulator
        Map<String, SimulationNode> receivers = getAllReceiversInRange("0");
        Map<String, TransmissionResult> directTransmissionResults =
                simpleTransmissionModel.simulateTopocast("0", receivers, transmissionParameter, allNodes);

        // ASSERT

        // all entities within communication radius should receive message (units 0 to 4)
        CartesianPoint senderPosition = allNodes.get("0").getPosition();
        double senderRadius = allNodes.get("0").getRadius();
        for (Map.Entry<String, TransmissionResult> transmissionResultEntry : directTransmissionResults.entrySet()) {
            CartesianPoint receiverPosition = allNodes.get(transmissionResultEntry.getKey()).getPosition();
            if (senderPosition.distanceTo(receiverPosition) <= senderRadius) {
                assertTrue(transmissionResultEntry.getValue().success);
            } else {
                assertFalse(transmissionResultEntry.getValue().success);
            }
        }
    }

    /**
     * The transmission for this test is also covered in the documentation with a simplified visualization.
     */
    @Test
    public void simulateGeoBroadcast_senderInDestinationArea_allReceiversReachable_noPacketLoss() {
        // SETUP
        TransmissionParameter transmissionParameter = generateTransmissionParameter_NoLoss(4);

        // RUN
        // use all randomly distributed nodes as receiver except sender
        Map<String, TransmissionResult> floodingTransmissionResults =
                sophisticatedTransmissionModel
                        .simulateGeocast("30", getAllRandomlyDistributedEntitiesRemoveSender("30"), transmissionParameter, allNodes);

        // ASSERT
        // there should be results for every anticipated receiver
        assertEquals(9, floodingTransmissionResults.size());
        for (Map.Entry<String, TransmissionResult> transmissionResultEntry : floodingTransmissionResults.entrySet()) {
            assertTrue(transmissionResultEntry.getValue().success);
            // number of hops should be equal to delay, since delay is set in a way to be a single valued integer
            assertEquals(transmissionResultEntry.getValue().delay, transmissionResultEntry.getValue().numberOfHops);
        }
        // Assert expected flooding steps
        assertEquals(1, floodingTransmissionResults.get("29").delay);
        assertEquals(2, floodingTransmissionResults.get("22").delay);
        assertEquals(2, floodingTransmissionResults.get("23").delay);
        assertEquals(3, floodingTransmissionResults.get("21").delay);
        assertEquals(3, floodingTransmissionResults.get("24").delay);
        assertEquals(3, floodingTransmissionResults.get("28").delay);
        assertEquals(4, floodingTransmissionResults.get("25").delay);
        assertEquals(4, floodingTransmissionResults.get("26").delay);
        assertEquals(4, floodingTransmissionResults.get("27").delay);
    }

    /**
     * This test limits the transmission radius of entities, so that not all receivers can be reached.
     */
    @Test
    public void simulateGeoBroadcast_senderInDestinationArea_someReceiversReachable_radiusToSmall_noPacketLoss() {
        // SETUP
        TransmissionParameter transmissionParameter = generateTransmissionParameter_NoLoss(4);
        // reduce transmission radius for all units
        for (Map.Entry<String, SimulationNode> entry : nodesRandomlyDistributed.entrySet()) {
            when(entry.getValue().getRadius()).thenReturn(145d);
        }
        // RUN

        // use all randomly distributed nodes as receiver
        Map<String, TransmissionResult> floodingTransmissionResults =
                sophisticatedTransmissionModel
                        .simulateGeocast("30", getAllRandomlyDistributedEntitiesRemoveSender("30"), transmissionParameter, allNodes);

        // ASSERT
        // there should be results for every anticipated receiver
        assertEquals(9, floodingTransmissionResults.size());
        String[] unreachableEntities = {"23", "26", "27", "28"};

        // Assert expected flooding steps
        for (Map.Entry<String, TransmissionResult> transmissionResultEntry : floodingTransmissionResults.entrySet()) {
            if (Arrays.asList(unreachableEntities).contains(transmissionResultEntry.getKey())) {
                assertFalse(transmissionResultEntry.getValue().success);
            } else {
                assertTrue(transmissionResultEntry.getValue().success);
            }
        }
        assertEquals(1, floodingTransmissionResults.get("29").delay);
        assertEquals(2, floodingTransmissionResults.get("22").delay);
        assertEquals(3, floodingTransmissionResults.get("21").delay);
        assertEquals(3, floodingTransmissionResults.get("24").delay);
        assertEquals(4, floodingTransmissionResults.get("25").delay);
    }

    /**
     * This test limits the Time to live, so that not all receivers can be reached.
     */
    @Test
    public void simulateGeoBroadcast_senderInDestinationArea_someReceiversReachable_ttlToSmall_noPacketLoss() {
        // SETUP
        // set ttl to small to reach all units in destination area
        TransmissionParameter transmissionParameter = generateTransmissionParameter_NoLoss(3);

        // RUN
        // use all randomly distributed nodes as receiver
        Map<String, TransmissionResult> floodingTransmissionResults = sophisticatedTransmissionModel.simulateGeocast(
                "30",
                getAllRandomlyDistributedEntitiesRemoveSender("30"),
                transmissionParameter,
                allNodes
        );

        // ASSERT
        // there should be results for every anticipated receiver
        assertEquals(9, floodingTransmissionResults.size());
        String[] unreachableEntities = {"25", "26", "27"};

        // Assert expected flooding steps
        for (Map.Entry<String, TransmissionResult> transmissionResultEntry : floodingTransmissionResults.entrySet()) {
            if (Arrays.asList(unreachableEntities).contains(transmissionResultEntry.getKey())) {
                assertFalse(transmissionResultEntry.getValue().success);
            } else {
                assertTrue(transmissionResultEntry.getValue().success);
            }
        }
        assertEquals(1, floodingTransmissionResults.get("29").delay);
        assertEquals(2, floodingTransmissionResults.get("22").delay);
        assertEquals(2, floodingTransmissionResults.get("23").delay);
        assertEquals(3, floodingTransmissionResults.get("21").delay);
        assertEquals(3, floodingTransmissionResults.get("28").delay);
        assertEquals(3, floodingTransmissionResults.get("24").delay);
    }

    @Test
    public void floodingTransmissionRecognizesSenderNotInDestination() {
        // SETUP
        TransmissionParameter transmissionParameter = generateTransmissionParameter_NoLoss(0);

        // RUN
        try {
            sophisticatedTransmissionModel
                    .simulateGeocast("0", getAllRandomlyDistributedEntitiesRemoveSender("0"), transmissionParameter, allNodes);
        } catch (RuntimeException e) {
            assertEquals("Sender has to be in destination area to use flooding as transmission model.", e.getMessage());
        }
    }

    /**
     * This test simulates the behaviour of the SNS, when Forwarding + Flooding is used.
     */
    @Test
    public void simulateGeoBroadcast_senderOutsideDestination_allReceiversReachable_noPacketLoss() {
        // SETUP
        // set ttl to a high amount, because the destination are needs to be approached first
        TransmissionParameter transmissionParameter = generateTransmissionParameter_NoLoss(30);

        // RUN
        // use randomly distributed entities as receivers
        Map<String, TransmissionResult> transmissionResult = sophisticatedTransmissionModel.simulateGeocast(
                "0",
                getAllRandomlyDistributedEntitiesRemoveSender("0"),
                transmissionParameter,
                allNodes
        );
        // ASSERT
        assertNotNull(transmissionResult);
        assertFalse(transmissionResult.isEmpty());
        assertEquals(10, transmissionResult.size());

        for (Map.Entry<String, TransmissionResult> resultEntry : transmissionResult.entrySet()) {
            assertTrue(resultEntry.getValue().success);
            // delay and number of hops have to larger than the forwarding values
            assertTrue(resultEntry.getValue().delay >= 20);
            assertTrue(resultEntry.getValue().numberOfHops >= 20);
        }
    }

    @Test
    public void simulateSingleHopBroadCast_fullLoss() {
        // SETUP
        when(allNodes.get("0").getRadius()).thenReturn(Double.MAX_VALUE); // make all units reachable in radius

        // Note: ttl parameter doesn't matter for direct transmission, the SNS would log a warning if TTL is != 1
        TransmissionParameter transmissionParameter = generateTransmissionParameter_FullLoss();

        // RUN
        // use all entities as receivers
        Map<String, TransmissionResult> directTransmissionResults =
                simpleTransmissionModel.simulateTopocast("0", allNodes, transmissionParameter, allNodes);

        // ASSERT
        // for the entity without wifi module there won't be any transmission attempts
        // since our delay has full loss, nobody should receive the message
        for (String entityName : directTransmissionResults.keySet()) {
            assertFalse(directTransmissionResults.get(entityName).success);
            assertEquals(transmissionParameter.transmission.maxRetries + 1, directTransmissionResults.get(entityName).attempts);
        }
    }

    @Test
    public void simulateTopocast_addressingSingleReceiver() {
        // SETUP
        TransmissionParameter transmissionParameter = generateTransmissionParameter_NoLoss(1);
        Map<String, SimulationNode> receiver = new HashMap<>();
        receiver.put("1", allNodes.get("1"));

        // RUN
        Map<String, TransmissionResult> directTransmissionResults =
                simpleTransmissionModel.simulateTopocast("0", receiver, transmissionParameter, allNodes);

        // ASSERT
        assertTrue(directTransmissionResults.get("1").success);
    }

    @Test
    public void simulateGeoBroadcast_senderInDestinationArea_FullLoss() {
        // SETUP
        TransmissionParameter transmissionParameter = generateTransmissionParameter_FullLoss();

        // RUN
        // use all randomly distributed nodes as receiver
        Map<String, TransmissionResult> floodingTransmissionResults = sophisticatedTransmissionModel.simulateGeocast(
                "30",
                getAllRandomlyDistributedEntitiesRemoveSender("30"),
                transmissionParameter,
                allNodes
        );

        // ASSERT
        // sender always has successful transmission
        floodingTransmissionResults.remove("30");
        // Assert expected flooding steps
        for (Map.Entry<String, TransmissionResult> transmissionResultEntry : floodingTransmissionResults.entrySet()) {
            assertFalse(transmissionResultEntry.getValue().success);
        }
    }

    @Test
    public void noLoss() {
        // SETUP + RUN
        double avgAttempts = simulateTransmissionBetweenTwoEntities(generateTransmissionParameter_NoLoss(1), 10000);

        //ASSERT
        assertEquals(1, avgAttempts, 0.001d);
    }

    @Test
    public void lowLoss() {
        // SETUP + RUN
        double avgAttempts = simulateTransmissionBetweenTwoEntities(generateTransmissionParameter_lowLoss(), 10000);

        //ASSERT
        assertEquals(1.25, avgAttempts, 0.1d);
    }

    @Test
    public void highLoss() {
        // SETUP +RUN
        double avgAttempts = simulateTransmissionBetweenTwoEntities(generateTransmissionParameter_highLoss(), 10000);

        //ASSERT
        assertEquals(5.0, avgAttempts, 0.1d);
    }

    private double simulateTransmissionBetweenTwoEntities(TransmissionParameter transmissionParameter, int iterations) {
        TransmissionResult tr;
        HashMap<String, SimulationNode> receiver = new HashMap<>();
        receiver.put("1", allNodes.get("1"));
        int totalAttempts = 0;
        for (int i = 0; i < iterations; i++) {
            tr = simpleTransmissionModel
                    .simulateTopocast("0", receiver, transmissionParameter, allNodes).entrySet().iterator().next().getValue();
            totalAttempts += tr.attempts;

            assertTrue(tr.success);
        }
        return totalAttempts / (double) iterations;
    }

    private TransmissionParameter generateTransmissionParameter_NoLoss(int ttl) {
        // constant delay so the flooding steps are easy to retrace
        ConstantDelay constantDelay = new ConstantDelay();
        constantDelay.delay = 1; // used to obtain single valued delays
        CTransmission transmission = new CTransmission();
        transmission.lossProbability = 0.0f;
        transmission.maxRetries = 0;

        return new TransmissionParameter(rng, constantDelay, transmission, ttl);
    }

    private TransmissionParameter generateTransmissionParameter_FullLoss() {
        // constant delay so the flooding steps are easy to retrace
        ConstantDelay constantDelay = new ConstantDelay();
        constantDelay.delay = 1; // used to obtain single valued delays
        CTransmission transmission = new CTransmission();
        transmission.lossProbability = 1d;
        transmission.maxRetries = 3;

        return new TransmissionParameter(rng, constantDelay, transmission, 0);
    }

    private TransmissionParameter generateTransmissionParameter_lowLoss() {
        // constant delay so the flooding steps are easy to retrace
        ConstantDelay constantDelay = new ConstantDelay();
        constantDelay.delay = 1; // used to obtain single valued delays
        CTransmission transmission = new CTransmission();
        transmission.lossProbability = 0.2d;
        transmission.maxRetries = Integer.MAX_VALUE;

        return new TransmissionParameter(rng, constantDelay, transmission, 0);
    }

    private TransmissionParameter generateTransmissionParameter_highLoss() {
        // constant delay so the flooding steps are easy to retrace
        ConstantDelay constantDelay = new ConstantDelay();
        constantDelay.delay = 1; // used to obtain single valued delays
        CTransmission transmission = new CTransmission();
        transmission.lossProbability = 0.8d;
        transmission.maxRetries = Integer.MAX_VALUE;
        return new TransmissionParameter(rng, constantDelay, transmission, 0);
    }

    private Map<String, SimulationNode> getAllRandomlyDistributedEntitiesRemoveSender(String sender) {
        Map<String, SimulationNode> receivers = new HashMap<>();
        nodesRandomlyDistributed.forEach((id, node) -> receivers.put(id, allNodes.get(id)));
        receivers.remove(sender);
        return receivers;
    }

    private Map<String, SimulationNode> getAllReceiversInRange(String senderName) {
        Map<String, SimulationNode> receivers = new HashMap<>();
        CartesianArea range = new CartesianCircle(
                allNodes.get(senderName).getPosition(),
                allNodes.get(senderName).getRadius()
        );
        allNodes.forEach((id, data) -> {
            if (range.contains(data.getPosition())) {
                if (!id.equals(senderName)) {
                    receivers.put(id, data);
                }
            }
        });
        return receivers;
    }
}
