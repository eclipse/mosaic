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

package org.eclipse.mosaic.fed.application.ambassador;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.fed.application.ambassador.eventresources.StartApplications;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.navigation.CentralNavigationComponent;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.CentralPerceptionComponent;
import org.eclipse.mosaic.fed.application.app.TestAgentApplication;
import org.eclipse.mosaic.fed.application.app.TestApplicationWithSpy;
import org.eclipse.mosaic.fed.application.app.TestChargingStationApplication;
import org.eclipse.mosaic.fed.application.app.TestElectricVehicleApplication;
import org.eclipse.mosaic.fed.application.app.TestRoadSideUnitApplication;
import org.eclipse.mosaic.fed.application.app.TestServerApplication;
import org.eclipse.mosaic.fed.application.app.TestTrafficLightApplication;
import org.eclipse.mosaic.fed.application.app.TestTrafficManagementCenterApplication;
import org.eclipse.mosaic.fed.application.app.TestVehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.Application;
import org.eclipse.mosaic.interactions.application.ApplicationInteraction;
import org.eclipse.mosaic.interactions.communication.V2xMessageAcknowledgement;
import org.eclipse.mosaic.interactions.communication.V2xMessageReception;
import org.eclipse.mosaic.interactions.electricity.VehicleBatteryUpdates;
import org.eclipse.mosaic.interactions.mapping.TrafficLightRegistration;
import org.eclipse.mosaic.interactions.traffic.TrafficDetectorUpdates;
import org.eclipse.mosaic.interactions.traffic.TrafficLightUpdates;
import org.eclipse.mosaic.interactions.traffic.VehicleRoutesInitialization;
import org.eclipse.mosaic.interactions.traffic.VehicleTypesInitialization;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.interactions.vehicle.VehicleRouteRegistration;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.junit.GeoProjectionRule;
import org.eclipse.mosaic.lib.junit.IpResolverRule;
import org.eclipse.mosaic.lib.objects.traffic.InductionLoopInfo;
import org.eclipse.mosaic.lib.objects.traffic.LaneAreaDetectorInfo;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroup;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroupInfo;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.objects.v2x.V2xReceiverInformation;
import org.eclipse.mosaic.lib.objects.v2x.etsi.EtsiPayloadConfiguration;
import org.eclipse.mosaic.lib.objects.vehicle.BatteryData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.lib.util.junit.TestUtils;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.Interaction;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.RtiAmbassador;
import org.eclipse.mosaic.rti.api.parameters.AmbassadorParameter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.annotation.Nonnull;

/**
 * Tests the ApplicationAmbassador.
 */
public class ApplicationAmbassadorTest {

    public static final long END_TIME = 100 * TIME.SECOND;
    private RtiAmbassador rtiAmbassador;
    private long recentAdvanceTime = 0L;
    private ArrayList<Event> addedEvents = new ArrayList<>();

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Rule
    public SimulationKernelRule simulationKernel = new SimulationKernelRule(null, null,
            mock(CentralNavigationComponent.class), mock(CentralPerceptionComponent.class));

    @Rule
    public IpResolverRule ipResolverRule = new IpResolverRule();

    @Rule
    public GeoProjectionRule projectionRule = new GeoProjectionRule(GeoPoint.latLon(52.5, 13.4));

    /**
     * Setup and reset singletons.
     *
     * @throws IllegalValueException if requestAdvanceTime wasn't properly called
     */
    @Before
    public void setup() throws IllegalValueException {
        rtiAmbassador = mock(RtiAmbassador.class);

        // Catch the latest call of "requestAdvanceTime" in order to assert the last advance time the ambassador requests
        Answer<Void> timeAdvanceAnswer = invocation -> {
            recentAdvanceTime = invocation.getArgument(0);
            return null;
        };
        Mockito.doAnswer(timeAdvanceAnswer).when(rtiAmbassador).requestAdvanceTime(ArgumentMatchers.anyLong());
        Mockito.doAnswer(timeAdvanceAnswer).when(rtiAmbassador).requestAdvanceTime(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyByte()
        );
        Mockito.doAnswer((i) -> recentAdvanceTime).when(rtiAmbassador).getNextEventTimestamp();

        recentAdvanceTime = 0;
    }

    @After
    public void tearDown() {
        TestUtils.setPrivateField(EtsiPayloadConfiguration.class, "globalConfiguration", null);
    }

    /**
     * Tests, if Jars are loaded by the Ambassador when it is initialized. For this purpose,
     * a Jar with classes has been created inside this package, which is loaded by the ambassador and
     * added to the class path on runtime. After the initialization the classes inside the Jar should be
     * available via the class path.
     */
    @Test
    public void loadJarOnStartup() throws Exception {
        //SETUP
        File applicationConfig = new File(this.getClass().getResource("/application_config.json").toURI());
        AmbassadorParameter applicationParams = new AmbassadorParameter("application", applicationConfig);

        try {
            //PRE-ASSERT, make sure it cannot found by class loader BEFORE actual jar loading
            SimulationKernel.SimulationKernel.getClassLoader().loadClass(
                    "load.from.jar.VehicleApplication"
            );
            fail();
        } catch (Throwable e) {
            //ok
        }

        // RUN initialize Application, which searches for Jars in the same directory, where the ambassador configuration is in
        new ApplicationAmbassador(applicationParams);

        // ASSERT if classes in Jar could be loaded successfully. This class is in "application-from-jar.jar" only.
        SimulationKernel.SimulationKernel.getClassLoader().loadClass(
                "load.from.jar.VehicleApplication"
        );
    }

    /**
     * The ApplicationAmbassador receives an VehicleRegistration interaction. The application of the vehicle will
     * be added to the simulator and initialized. After the simulation has been finished, the application
     * will tear down.
     */
    @Test
    public void processInteraction_VehicleRegistration() throws InternalFederateException, IOException {

        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);
        assertEquals("Time advance should be requested for the end of the simulation", END_TIME, recentAdvanceTime);

        TestVehicleApplication app = testAddUnit(
                ambassador,
                "veh_0",
                InteractionTestHelper.createVehicleRegistrationInteraction("veh_0", 5, TestVehicleApplication.class)
        );

        // verify that setUp has been called on the application of the unit
        Mockito.verify(app.getApplicationSpy()).onStartup();

        // tears down all applications
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
        Mockito.verify(app.getApplicationSpy()).onShutdown();
    }

    /**
     * Asserts that adding a vehicle does not result in application start.
     * Application is supposed to start at vehicle movement only.
     */
    @Test
    public void registerVehicle_noStartApplication() throws Exception {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);
        assertEquals("Time advance should be requested for the end of the simulation", END_TIME, recentAdvanceTime);

        TestVehicleApplication app = testAddUnit(
                ambassador,
                "veh_0",
                InteractionTestHelper.createVehicleRegistrationInteraction("veh_0", 5, TestVehicleApplication.class)
        );

        // assert that no start application event has been fired additionally to the one fired by testAddUnit()
        assertEquals(1, countStartApplicationEvents());

        // verify that setUp has been called on the application of the unit
        Mockito.verify(app.getApplicationSpy()).onStartup();

        // tears down all applications
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
        Mockito.verify(app.getApplicationSpy()).onShutdown();
    }

    /**
     * The ApplicationAmbassador receives an VehicleRegistration interaction for an electric vehicle with a normal VehicleApplication.
     * The application of the vehicle will be added to the simulator and initialized.
     * After the simulation has been finished, the application will tear down.
     */
    @Test
    public void processInteraction_VehicleRegistration_ElectricVehicleWithVehicleApp() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);
        assertEquals("Time advance should be requested for the end of the simulation", END_TIME, recentAdvanceTime);

        TestVehicleApplication app = testAddUnit(
                ambassador,
                "veh_0",
                InteractionTestHelper.createVehicleRegistration_ElectricVehicle("veh_0", 5, TestVehicleApplication.class)
        );

        // verify that setUp has been called on the application of the unit
        Mockito.verify(app.getApplicationSpy()).onStartup();

        // tears down all applications
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
        Mockito.verify(app.getApplicationSpy()).onShutdown();
    }

    /**
     * The ApplicationAmbassador receives an VehicleRegistration interaction for an electric vehicle with an ElectricVehicleApplication.
     * The application of the vehicle will be added to the simulator and initialized.
     * After the simulation has been finished, the application will tear down.
     */
    @Test
    public void processInteraction_VehicleRegistration_ElectricVehicleWithElectricVehicleApp() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);

        TestElectricVehicleApplication app = testAddUnit(
                ambassador,
                "veh_0",
                InteractionTestHelper.createVehicleRegistration_ElectricVehicle("veh_0", 5, TestElectricVehicleApplication.class)
        );

        // verify that setUp has been called on the application of the unit
        Mockito.verify(app.getApplicationSpy()).onStartup();

        // tears down all applications
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
        Mockito.verify(app.getApplicationSpy()).onShutdown();
    }

    /**
     * The ApplicationAmbassador receives an VehicleRegistration Interaction for a vehicle with an ElectricVehicleApplication and
     * is expected to throw an exception.
     */
    @Test(expected = InternalFederateException.class)
    public void processInteraction_VehicleRegistration_VehicleWithElectricVehicleApp_Fail() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, 100 * TIME.SECOND);

        testAddUnit(
                ambassador,
                "veh_0",
                InteractionTestHelper.createVehicleRegistrationInteraction("veh_0", 5, TestElectricVehicleApplication.class)
        );
    }

    /**
     * The ApplicationAmbassador receives an AgentRegistration interaction with a AgentApplication.
     * The application of the agent will be added to the simulator and initialized.
     * After the simulation has been finished, the application will tear down.
     */
    @Test
    public void processInteraction_AgentRegistration() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);
        assertEquals("Time advance should be requested for the end of the simulation", END_TIME, recentAdvanceTime);

        TestAgentApplication app = testAddUnit(
                ambassador,
                "agent_0",
                InteractionTestHelper.createAgentRegistrationInteraction("agent_0", 5, TestAgentApplication.class)
        );

        // verify that setUp has been called on the application of the unit
        Mockito.verify(app.getApplicationSpy()).onStartup();

        // tears down all applications
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
        Mockito.verify(app.getApplicationSpy()).onShutdown();
    }


    /**
     * After a vehicle has been added to the ApplicationAmbassador, it receives an V2xMessageReception Interaction.
     * The application of the vehicle should receive this interaction after the ambassador has processed the message #
     * and the corresponding event.
     */
    @Test
    public void processInteraction_V2xMessageReception() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);

        // add unit to ambassador
        TestVehicleApplication app = testAddUnit(
                ambassador,
                "veh_0",
                InteractionTestHelper.createVehicleRegistrationInteraction("veh_0", 5, TestVehicleApplication.class)
        );

        // store mocked message in cache
        final V2xMessage v2xMessage = mock(V2xMessage.class);
        SimulationKernel.SimulationKernel.getV2xMessageCache().putItem(9 * TIME.SECOND, v2xMessage);

        // RUN: Send message V2xMessageReception
        V2xReceiverInformation information = new V2xReceiverInformation(10 * TIME.SECOND);
        V2xMessageReception v2xMessageReception = new V2xMessageReception(10 * TIME.SECOND, "veh_0", 0, information);
        ambassador.processInteraction(v2xMessageReception);

        // ASSERT + RUN: process event, which has been created by the ambassador
        assertEquals(v2xMessageReception.getTime(), recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);

        // ASSERT: Verify, that the method v2xMessageReception has been
        // called in the application by the simulation kernel with the expected arguments
        Mockito.verify(app.getApplicationSpy()).onMessageReceived(argThat(argument -> argument.getMessage() == v2xMessage));

        // finish simulation
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
    }

    /**
     * After a vehicle has been added to the ApplicationAmbassador, the ambassador received
     * various VehicleUpdates interactions, each with information for added vehicles, updated vehicles,
     * and removed vehicles.
     * It will be checked, if the methods beforeUpdateVehicleInfo and afterUpdateVehicleInfo are called for the application,
     * and that the position of the vehicle is updated correctly.
     */
    @Test
    public void processInteraction_VehicleUpdates() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);

        // add unit to ambassador
        TestVehicleApplication app = testAddUnit(
                ambassador,
                "veh_0",
                InteractionTestHelper.createVehicleRegistrationInteraction("veh_0", 5, TestVehicleApplication.class)
        );

        // create vehicle Info Interaction
        final GeoPoint geoPointAdd = GeoPoint.lonLat(13, 53);
        final GeoPoint geoPointUpdate = GeoPoint.lonLat(13.5, 53.5);
        final VehicleData vehInfo = mock(VehicleData.class);
        when(vehInfo.getTime()).thenReturn(10 * TIME.SECOND);
        when(vehInfo.getName()).thenReturn("veh_0");
        when(vehInfo.getPosition()).thenReturn(geoPointAdd);

        // Send Interaction and process event at requested advance time
        VehicleUpdates vehicleUpdates = new VehicleUpdates(
                10 * TIME.SECOND,
                Collections.singletonList(vehInfo),
                Collections.emptyList(),
                Collections.emptyList()
        );
        ambassador.processInteraction(vehicleUpdates);

        assertEquals(vehicleUpdates.getTime(), recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);

        // Assert that update info methods have been called twice in application and
        // that the position has been updated (first call after adding vehicle, second call after updating vehicle)
        Mockito.verify(app.getApplicationSpy(), Mockito.times(2)).onVehicleUpdated(any(), any());
        assertEquals(geoPointAdd, app.getOperatingSystem().getNavigationModule().getCurrentPosition());

        // Update position of vehicle info
        when(vehInfo.getTime()).thenReturn(20 * TIME.SECOND);
        when(vehInfo.getPosition()).thenReturn(geoPointUpdate);

        // Send interaction with information about updated vehicle and process event at requested advance time
        ambassador.processInteraction(new VehicleUpdates(
                20 * TIME.SECOND,
                Collections.emptyList(),
                Collections.singletonList(vehInfo),
                Collections.emptyList())
        );
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);

        // Assert that update info methods have been called again in application and that the position has been updated to the new position
        Mockito.verify(app.getApplicationSpy(), Mockito.times(3)).onVehicleUpdated(any(), any());
        assertEquals(geoPointUpdate, app.getOperatingSystem().getNavigationModule().getCurrentPosition());

        // Send interaction with information about removed vehicle and process event at requested advance time
        ambassador.processInteraction(new VehicleUpdates(
                30 * TIME.SECOND,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList("veh_0")));
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);

        // ASSERT that tear down of application is called when removing the vehicle
        Mockito.verify(app.getApplicationSpy()).onShutdown();

        // finish simulation
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();

        // ASSERT that tear down of application is not called again
        Mockito.verify(app.getApplicationSpy()).onShutdown();
    }

    /**
     * Tests, if a BatteryData is set on the correct vehicle.
     */
    @Test
    public void processInteraction_ElectricVehicleInformationUpdate() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);

        // add unit to ambassador
        TestElectricVehicleApplication app = testAddUnit(
                ambassador,
                "veh_0",
                InteractionTestHelper.createVehicleRegistration_ElectricVehicle("veh_0", 5, TestElectricVehicleApplication.class)
        );

        List<BatteryData> updated = new Vector<>();
        updated.add(new BatteryData(5 * TIME.NANO_SECOND, "veh_0"));
        updated.add(new BatteryData(TIME.NANO_SECOND, "veh_0"));
        updated.add(new BatteryData(8 * TIME.NANO_SECOND, "veh_1"));
        VehicleBatteryUpdates vehicleBatteryUpdates = new VehicleBatteryUpdates(10 * TIME.SECOND, updated);

        // RUN: send interaction and process events
        ambassador.processInteraction(vehicleBatteryUpdates);
        assertEquals(vehicleBatteryUpdates.getTime(), recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);

        // ASSERT that the correct methods are called
        Mockito.verify(app.getApplicationSpy(), Mockito.times(2)).onBatteryDataUpdated(any(), any());

        // finish simulation
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
    }

    /**
     * The ApplicationAmbassador receives an ApplicationInteraction interaction. The method
     * onApplicationInteraction of the application of the vehicle will be called by the ambassador.
     */
    @Test
    public void processInteraction_ApplicationInteraction() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);

        TestVehicleApplication app = testAddUnit(
                ambassador,
                "veh_0",
                InteractionTestHelper.createVehicleRegistrationInteraction("veh_0", 5, TestVehicleApplication.class)
        );

        @SuppressWarnings("serial") final ApplicationInteraction applicationInteraction =
                new ApplicationInteraction(10 * TIME.SECOND, "veh_0") {};
        ambassador.processInteraction(applicationInteraction);

        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        // ASSERT that the interaction is received by the application
        Mockito.verify(app.getApplicationSpy()).onInteractionReceived(ArgumentMatchers.same(applicationInteraction));

        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
    }

    /**
     * The UnableToDeliverV2XMessage receives an ApplicationInteraction interaction. The method
     * unableToSendV2XMessage of the application of the vehicle, which wanted to send an interaction
     * will be called by the ambassador.
     */
    @Test
    public void processInteraction_UnableToDeliverV2xMessage() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // SETUP init ambassador
        ambassador.initialize(0L, END_TIME);

        // SETUP add unit to ambassador
        TestVehicleApplication app = testAddUnit(
                ambassador,
                "veh_0",
                InteractionTestHelper.createVehicleRegistrationInteraction("veh_0", 5, TestVehicleApplication.class)
        );

        // PREPARE interaction, which gets lost
        final V2xMessage v2xMessage = mock(V2xMessage.class);
        when(v2xMessage.getId()).thenReturn(1);
        SimulationKernel.SimulationKernel.getV2xMessageCache().putItem(9 * TIME.SECOND, v2xMessage);

        // PREPARE unableToSendV2XMessage
        final V2xMessageAcknowledgement v2xMessageAcknowledgement =
                Mockito.spy(new V2xMessageAcknowledgement(10 * TIME.SECOND, 1, "veh_0"));

        // RUN send interaction and process event
        ambassador.processInteraction(v2xMessageAcknowledgement);
        assertEquals(v2xMessageAcknowledgement.getTime(), recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);

        // ASSERT that the interaction is received by the application
        Mockito.verify(app.getApplicationSpy()).onAcknowledgementReceived(
                eq(new ReceivedAcknowledgement(v2xMessage, null))
        );

        // finish simulation
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
    }

    /**
     * The ApplicationAmbassador receives an RsuRegistration interaction. The application of the rsu will
     * be added to the simulator and initialized. After the simulation has been finished, the application
     * will tear down.
     */
    @Test
    public void processInteraction_RsuRegistration() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);

        TestRoadSideUnitApplication app = testAddUnit(ambassador, "rsu_0", InteractionTestHelper.createRsuRegistration("rsu_0", 5, true));

        //  verify that setUp has been called on the application of the unit
        Mockito.verify(app.getApplicationSpy()).onStartup();

        // tears down all applications
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
        Mockito.verify(app.getApplicationSpy()).onShutdown();
    }

    /**
     * The ApplicationAmbassador receives an TrafficLightRegistration interaction. The application of the traffic light will
     * be added to the simulator and initialized. After the simulation has been finished, the application
     * will tear down.
     */
    @Test
    public void processInteraction_TrafficLightRegistration() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);

        TestTrafficLightApplication app =
                testAddUnit(ambassador, "tl_0", InteractionTestHelper.createTrafficLightRegistration("tl_0", 5, true));

        // verify that setUp has been called on the application of the unit
        Mockito.verify(app.getApplicationSpy()).onStartup();

        // tears down all applications
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
        Mockito.verify(app.getApplicationSpy()).onShutdown();
    }

    /**
     * Tests if the change of the traffic light group via a TrafficLightUpdate interaction
     * updates the traffic light group of the corresponding simulation unit.
     */
    @Test
    public void processInteraction_TrafficLightUpdate() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);

        TrafficLightRegistration trafficLightRegistration = InteractionTestHelper.createTrafficLightRegistration("tl_0", 5, true);

        // add unit to ambassador
        TestTrafficLightApplication app = testAddUnit(
                ambassador,
                "tl_0",
                trafficLightRegistration
        );

        // RUN: Send interaction V2xMessageReception
        final TrafficLightGroup tlg = trafficLightRegistration.getTrafficLightGroup();
        final TrafficLightGroupInfo newTrafficLightGroupInfo = new TrafficLightGroupInfo("trafficLightGroupId", "0", 0, 6000, null);
        Map<String, TrafficLightGroupInfo> trafficLightGroupInfoMap = new HashMap();
        trafficLightGroupInfoMap.put("trafficLightGroupId", newTrafficLightGroupInfo);

        TrafficLightUpdates trafficLightUpdates = new TrafficLightUpdates(10 * TIME.SECOND, trafficLightGroupInfoMap);
        ambassador.processInteraction(trafficLightUpdates);

        // ASSERT + RUN: process event, which has been created by the ambassador
        assertEquals(trafficLightUpdates.getTime(), recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);

        // ASSERT that relevant methods have been called and that the traffic light group has been set
        Mockito.verify(app.getApplicationSpy()).onTrafficLightGroupUpdated(any(), any());
        assertSame(tlg, app.getOperatingSystem().getTrafficLightGroup());

        // finish simulation
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
    }

    /**
     * Tests, if the VehicleTypesInitialization interaction propagates all vehicle types
     * to the SimulationKernel.
     */
    @Test
    public void processInteraction_VehicleTypesInitialization() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);

        // Prepare and send vehicle types
        final Map<String, VehicleType> types = new HashMap<>();
        types.put("type1", new VehicleType("Type One"));
        types.put("type2", new VehicleType("Type Two"));
        final VehicleTypesInitialization vehicleTypesInitialization = new VehicleTypesInitialization(10 * TIME.SECOND, types);
        ambassador.processInteraction(vehicleTypesInitialization);

        // ASSERT: vehicles types have been propagated
        assertEquals(types.get("type1"), SimulationKernel.SimulationKernel.getVehicleTypes().get("type1"));
        assertEquals(types.get("type2"), SimulationKernel.SimulationKernel.getVehicleTypes().get("type2"));

        // finish simulation
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
    }

    /**
     * Tests, if the VehicleRoutesInitialization propagates all vehicle routes
     * to the SimulationKernel.
     */
    @Test
    public void processInteraction_VehicleRoutesInitialization() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);

        // Prepare and send vehicle routes
        final Map<String, VehicleRoute> routes = new HashMap<>();
        routes.put("0", new VehicleRoute("0", Arrays.asList("a_b", "b_c", "c_d"), Arrays.asList("a", "b", "c", "d"), 1000d));
        routes.put("1", new VehicleRoute("1", Arrays.asList("d_c", "c_b", "b_a"), Arrays.asList("d", "c", "b", "a"), 1000d));
        final VehicleRoutesInitialization vehicleRoutesInitialization = new VehicleRoutesInitialization(10 * TIME.SECOND, routes);
        ambassador.processInteraction(vehicleRoutesInitialization);

        // ASSERT: vehicles routes have been propagated
        assertEquals(routes.get("0"), SimulationKernel.SimulationKernel.getRoutes().get("0"));
        assertEquals(routes.get("1"), SimulationKernel.SimulationKernel.getRoutes().get("1"));

        // finish simulation
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
    }

    /**
     * The ApplicationAmbassador receives an VehicleRouteRegistration interaction. The new route
     * will be added to the global set of routes in the SimulationKernel.
     */
    @Test
    public void processInteraction_VehicleRouteRegistration() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);

        // PREPARE VehicleRouteRegistration interaction
        VehicleRoute route = mock(VehicleRoute.class);
        when(route.getId()).thenReturn("0");
        VehicleRouteRegistration newVehicleRouteRegistration = new VehicleRouteRegistration(10 * TIME.SECOND, route);

        // RUN process interaction
        ambassador.processInteraction(newVehicleRouteRegistration);

        // ASSERT route has been propagated
        assertSame(route, SimulationKernel.SimulationKernel.getRoutes().get("0"));

        // tears down all applications
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
    }

    /**
     * The ApplicationAmbassador receives an ChargingStationRegistration interaction. The application of the charging station will
     * be added to the simulator and initialized. After the simulation has been finished, the application
     * will tear down.
     */
    @Test
    public void processInteraction_ChargingStationRegistration() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);

        // register charging stations
        TestChargingStationApplication app = testAddUnit(
                ambassador,
                "cs_0",
                InteractionTestHelper.createChargingStationRegistration("cs_0", 5, true)
        );

        // verify that setUp has been called on the application of the unit
        Mockito.verify(app.getApplicationSpy()).onStartup();

        // tears down all applications
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
        Mockito.verify(app.getApplicationSpy()).onShutdown();
    }

    /**
     * The ApplicationAmbassador receives an {@link org.eclipse.mosaic.interactions.mapping.TmcRegistration} interaction.
     * The application of the traffic management center will be added to the simulator and initialized.
     * After the simulation has been finished, the application will tear down.
     */
    @Test
    public void processInteraction_AddedTrafficManagementCenter() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);

        TestTrafficManagementCenterApplication app = testAddUnit(
                ambassador,
                "tmc_0",
                InteractionTestHelper.createTmcRegistrationWithInductionLoops("tmc_0", 5, true)
        );

        // verify that setUp has been called on the application of the unit
        Mockito.verify(app.getApplicationSpy()).onStartup();

        // tears down all applications
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
        Mockito.verify(app.getApplicationSpy()).onShutdown();
    }

    @Test
    public void processInteraction_TrafficDetectorUpdates_inductionLoops() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);

        TestTrafficManagementCenterApplication app = testAddUnit(
                ambassador,
                "tmc_0",
                InteractionTestHelper.createTmcRegistrationWithInductionLoops(
                        "tmc_0",
                        5, true,
                        "induction_loop_1",
                        "induction_loop_2")
        );

        // RUN: Send interaction V2xMessageReception
        final List<InductionLoopInfo> inductionLoopInfos = Lists.newArrayList(
                new InductionLoopInfo.Builder(0, "induction_loop_1").create(),
                new InductionLoopInfo.Builder(0, "induction_loop_2").create()
        );
        final TrafficDetectorUpdates trafficDetectorUpdates = new TrafficDetectorUpdates(
                10 * TIME.SECOND,
                Lists.newArrayList(), inductionLoopInfos
        );
        ambassador.processInteraction(trafficDetectorUpdates);

        // ASSERT + RUN: process event which has been created by the ambassador
        assertEquals(trafficDetectorUpdates.getTime(), recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);

        // ASSERT that  relevant methods have been called and that the traffic light group has been set
        Mockito.verify(app.getApplicationSpy(), times(1)).onInductionLoopUpdated(any());
        Mockito.verify(app.getApplicationSpy(), never()).onLaneAreaDetectorUpdated(any());

        // finish simulation
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
    }

    @Test
    public void processInteraction_TrafficDetectorUpdates_noInductionLoops() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);

        TestTrafficManagementCenterApplication app = testAddUnit(
                ambassador,
                "tmc_0",
                InteractionTestHelper.createTmcRegistrationWithInductionLoops("tmc_0", 5, true, "induction_loop_x")
        );

        final long previousAdvanceTime = recentAdvanceTime;

        // RUN: Send interaction V2xMessageReception
        final List<InductionLoopInfo> inductionLoopInfo = Lists.newArrayList(
                new InductionLoopInfo.Builder(0, "induction_loop_1").create()
        );
        final TrafficDetectorUpdates trafficDetectorUpdates = new TrafficDetectorUpdates(
                10 * TIME.SECOND,
                Lists.newArrayList(), inductionLoopInfo
        );
        ambassador.processInteraction(trafficDetectorUpdates);

        // ASSERT + RUN: process event, which has been created by the ambassador
        assertEquals(previousAdvanceTime, recentAdvanceTime);

        // ASSERT that  relevant methods have been called and that the traffic light group has been set
        Mockito.verify(app.getApplicationSpy(), never()).onInductionLoopUpdated(any());
        Mockito.verify(app.getApplicationSpy(), never()).onLaneAreaDetectorUpdated(any());

        // finish simulation
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
    }

    @Test
    public void processInteraction_TrafficDetectorUpdates_laneAreaDetectors() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);

        TestTrafficManagementCenterApplication app = testAddUnit(
                ambassador,
                "tmc_0",
                InteractionTestHelper.createTmcRegistrationWithLaneAreaDetectors(
                        "tmc_0",
                        5,
                        true,
                        "lane_area_1",
                        "lane_area_2"
                )
        );
        // RUN: Send interaction V2xMessageReception
        final List<InductionLoopInfo> inductionLoopInfo = Lists.newArrayList(
                new InductionLoopInfo.Builder(0, "induction_loop_2").create()
        );
        final List<LaneAreaDetectorInfo> laneAreaDetectors = Lists.newArrayList(
                new LaneAreaDetectorInfo.Builder(0, "lane_area_1").create(),
                new LaneAreaDetectorInfo.Builder(0, "lane_area_2").create()
        );
        final TrafficDetectorUpdates trafficDetectorUpdates = new TrafficDetectorUpdates(
                10 * TIME.SECOND,
                laneAreaDetectors,
                inductionLoopInfo
        );
        ambassador.processInteraction(trafficDetectorUpdates);

        // ASSERT + RUN: process event, which has been created by the ambassador
        assertEquals(trafficDetectorUpdates.getTime(), recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);

        // ASSERT that  relevant methods have been called and that the traffic light group has been set
        Mockito.verify(app.getApplicationSpy(), never()).onInductionLoopUpdated(any());
        Mockito.verify(app.getApplicationSpy(), times(1)).onLaneAreaDetectorUpdated(any());

        // finish simulation
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
    }

    /**
     * The ApplicationAmbassador receives an {@link org.eclipse.mosaic.interactions.mapping.ServerRegistration} interaction.
     * The application of the server will be added to the simulator and initialized.
     * After the simulation has been finished, the application will tear down.
     */
    @Test
    public void processInteraction_ServerRegistration() throws InternalFederateException, IOException {
        final ApplicationAmbassador ambassador = createAmbassador();

        // init ambassador
        ambassador.initialize(0L, END_TIME);

        TestServerApplication app = testAddUnit(
                ambassador,
                "server_0",
                InteractionTestHelper.createServerRegistration("server_0", 5, true)
        );

        // verify that setUp has been called on the application of the unit
        Mockito.verify(app.getApplicationSpy()).onStartup();

        // tears down all applications
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);
        ambassador.processTimeAdvanceGrant(END_TIME);
        ambassador.finishSimulation();
        Mockito.verify(app.getApplicationSpy()).onShutdown();
    }

    private <TEST_APP extends TestApplicationWithSpy<? extends Application>> TEST_APP testAddUnit(final ApplicationAmbassador ambassador, final String unitId, final Interaction interaction) throws InternalFederateException {
        addedEvents = new ArrayList<>();
        // add unit on simulation time 5s
        ambassador.processInteraction(interaction);
        final VehicleData vehInfo = mock(VehicleData.class);
        when(vehInfo.getTime()).thenReturn(5 * TIME.SECOND);
        when(vehInfo.getName()).thenReturn("veh_0");

        VehicleUpdates movements = new VehicleUpdates(
                5 * TIME.SECOND,
                Collections.singletonList(vehInfo),
                new ArrayList<>(),
                new ArrayList<>()
        );
        ambassador.processInteraction(movements);
        Assert.assertEquals(interaction.getTime(), recentAdvanceTime);
        Assert.assertTrue(interactionTimeEquals(interaction.getTime()));
        Assert.assertTrue(countStartApplicationEvents() > 0);
        Assert.assertNotNull(UnitSimulator.UnitSimulator.getAllUnits().get(unitId));

        // should initialize the application
        ambassador.processTimeAdvanceGrant(recentAdvanceTime);

        @SuppressWarnings("unchecked")
        TEST_APP app = (TEST_APP) Iterables.getFirst(UnitSimulator.UnitSimulator.getAllUnits().get(unitId).getApplications(), null);

        // assert that the requested advance time of the ambassador did not change
        Assert.assertEquals(interaction.getTime(), recentAdvanceTime);

        return app;
    }

    private boolean interactionTimeEquals(long time) {
        for (Event e : addedEvents) {
            if (e.getTime() == time) {
                return true;
            }
        }
        return false;
    }

    private int countStartApplicationEvents() {
        int i = 0;
        for (Event e : addedEvents) {
            if (e.getResource() instanceof StartApplications) {
                i++;
            }
        }
        return i;
    }

    private ApplicationAmbassador createAmbassador() throws IOException {
        AmbassadorParameter applicationParams;
        try {
            applicationParams = new AmbassadorParameter("application", tmpFolder.newFile());
            ApplicationAmbassador ambassador = new ApplicationAmbassador(applicationParams) {
                public void addEvent(@Nonnull Event event) {
                    super.addEvent(event);
                    // store the latest event in order to
                    ApplicationAmbassadorTest.this.addedEvents.add(event);
                }
            };
            ambassador.setRtiAmbassador(rtiAmbassador);
            return ambassador;
        } catch (IOException e) {
            Assert.fail("Initialize the ambassador");
            throw e;
        }
    }

}
