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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.ambassador.SimulationKernelRule;
import org.eclipse.mosaic.fed.application.ambassador.navigation.CentralNavigationComponent;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.AdHocModuleConfiguration;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.CentralPerceptionComponent;
import org.eclipse.mosaic.fed.application.app.TestVehicleApplication;
import org.eclipse.mosaic.fed.application.config.CApplicationAmbassador;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.lib.enums.DriveDirection;
import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.junit.EtsiPayloadConfigurationRule;
import org.eclipse.mosaic.lib.junit.IpResolverRule;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.objects.v2x.etsi.Cam;
import org.eclipse.mosaic.lib.objects.v2x.etsi.cam.AwarenessData;
import org.eclipse.mosaic.lib.objects.v2x.etsi.cam.VehicleAwarenessData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.lib.util.scheduling.EventManager;
import org.eclipse.mosaic.rti.api.Interactable;
import org.eclipse.mosaic.rti.api.Interaction;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Testsuite for vehicles.
 */
public class VehicleTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

    private EventManager eventManager = mock(EventManager.class);

    private Interactable interactable = mock(Interactable.class);

    @Rule
    public IpResolverRule ipResolverRule = new IpResolverRule();

    @Rule
    public EtsiPayloadConfigurationRule payloadConfigurationRule = new EtsiPayloadConfigurationRule();

    @Rule
    public SimulationKernelRule simulationKernelRule = new SimulationKernelRule(eventManager, interactable,
            mock(CentralNavigationComponent.class), mock(CentralPerceptionComponent.class));

    private final AtomicReference<Interaction> interactionSentByVehicle = new AtomicReference<>(null);

    @Before
    public void setup() throws Exception {
        Mockito.doAnswer((Answer<Void>) invocation -> {
            interactionSentByVehicle.set(invocation.getArgument(0));
            return null;
        }).when(interactable).triggerInteraction(any(Interaction.class));

        SimulationKernel.SimulationKernel.setConfiguration(new CApplicationAmbassador());
        SimulationKernel.SimulationKernel.setClassLoader(ClassLoader.getSystemClassLoader());
    }

    /**
     * A vehicle sends a CAM message, which contains VehicleAwarenessData. The contents
     * of the message sent to the ambassador via the simulation unit will be asserted.
     * Additionally, cell and wifi communication will be disabled one after another. CAM
     * message will not be sent if wifi communication is disabled.
     */
    @Test
    public void sendCam() throws Exception {
        // SETUP vehicle values
        final double speed = 3.14d;
        final double heading = -20.3d;
        final DriveDirection direction = DriveDirection.FORWARD;
        final int laneIndex = 0;
        final GeoPoint position = GeoPoint.latLon(53.5, 13.4);

        VehicleUnit vehicle = prepareVehicleWithVehicleInfo(speed, heading, direction, laneIndex, position);
        vehicle.getAdHocModule().enable(new AdHocModuleConfiguration()
                .addRadio()
                .channel(AdHocChannel.CCH)
                .power(100)
                .create());

        // RUN sendCam
        vehicle.getAdHocModule().sendCam();

        // ASSERT CAM message and AwarenessData
        assertCam(speed, heading, direction, laneIndex, position, interactionSentByVehicle.get());

        // PREPARE disable cell communication
        interactionSentByVehicle.set(null);
        vehicle.getCellModule().disable();

        // RUN again, CAM will be sent without cell communication
        vehicle.getAdHocModule().sendCam();

        // ASSERT CAM message and AwarenessData
        assertCam(speed, heading, direction, laneIndex, position, interactionSentByVehicle.get());

        // PREPARE disable WiFi communication
        vehicle.getAdHocModule().disable();
        interactionSentByVehicle.set(null);

        // RUN again, no data will be sent, since WiFi is disabled
        vehicle.getAdHocModule().sendCam();

        // ASSERT no msg sent
        Assert.assertNull(interactionSentByVehicle.get());
    }

    private VehicleUnit prepareVehicleWithVehicleInfo(
            final double speed,
            final double heading,
            final DriveDirection direction,
            final int laneIndex,
            final GeoPoint position
    ) throws Exception {
        // SETUP vehicle
        VehicleData vehInfo = mock(VehicleData.class);
        when(vehInfo.getSpeed()).thenReturn(speed);
        when(vehInfo.getHeading()).thenReturn(heading);
        when(vehInfo.getDriveDirection()).thenReturn(direction);
        when(vehInfo.getPosition()).thenReturn(position);

        IRoadPosition roadPosition = mock(IRoadPosition.class);
        when(roadPosition.getLaneIndex()).thenReturn(laneIndex);
        when(vehInfo.getRoadPosition()).thenReturn(roadPosition);

        VehicleUnit vehicle = new VehicleUnit("veh_0", new VehicleType("default"), position);
        vehicle.loadApplications(Collections.singletonList(TestVehicleApplication.class.getCanonicalName()));
        vehicle.processEvent(new Event(0, vehicle.getApplications().get(0), vehInfo));
        return vehicle;
    }

    private void assertCam(
            final double expectedSpeed,
            final double expectedHeading,
            final DriveDirection expectedDirection,
            final int expectedLaneIndex,
            final GeoPoint expectedPosition,
            final Interaction interaction
    ) {
        Assert.assertNotNull(interaction);
        Assert.assertTrue(interaction instanceof V2xMessageTransmission);

        final V2xMessage message = ((V2xMessageTransmission) interaction).getMessage();
        Assert.assertEquals("Cam", message.getSimpleClassName());
        Assert.assertEquals(98, message.getPayLoad().getActualLength());
        Assert.assertEquals(200, message.getPayLoad().getMinimalLength());

        final V2xMessage concreteMsg = SimulationKernel.SimulationKernel.getV2xMessageCache().getItem(message.getId());
        Assert.assertTrue(concreteMsg instanceof Cam);
        Assert.assertEquals(98, concreteMsg.getPayLoad().getActualLength());
        Assert.assertEquals(200, concreteMsg.getPayLoad().getMinimalLength());

        Assert.assertEquals(expectedPosition, ((Cam) concreteMsg).getPosition());

        final AwarenessData awarenessData = ((Cam) concreteMsg).getAwarenessData();
        Assert.assertTrue(awarenessData instanceof VehicleAwarenessData);
        Assert.assertEquals(expectedSpeed, ((VehicleAwarenessData) awarenessData).getSpeed(), 0.001d);
        Assert.assertEquals(expectedHeading, ((VehicleAwarenessData) awarenessData).getHeading(), 0.001d);
        Assert.assertEquals(expectedDirection, ((VehicleAwarenessData) awarenessData).getDirection());
        Assert.assertEquals(expectedLaneIndex, ((VehicleAwarenessData) awarenessData).getLaneIndex());
        Assert.assertEquals(VehicleClass.Car, ((VehicleAwarenessData) awarenessData).getVehicleClass());
    }
}
