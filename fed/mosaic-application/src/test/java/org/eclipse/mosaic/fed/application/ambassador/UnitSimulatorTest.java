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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.mosaic.fed.application.ambassador.eventresources.RemoveVehicles;
import org.eclipse.mosaic.fed.application.ambassador.navigation.CentralNavigationComponent;
import org.eclipse.mosaic.fed.application.ambassador.simulation.AbstractSimulationUnit;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.CentralPerceptionComponent;
import org.eclipse.mosaic.fed.application.app.TestApplicationWithSpy;
import org.eclipse.mosaic.fed.application.app.TestVehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.Application;
import org.eclipse.mosaic.fed.application.app.api.ChargingStationApplication;
import org.eclipse.mosaic.fed.application.app.api.TrafficLightApplication;
import org.eclipse.mosaic.fed.application.app.api.TrafficManagementCenterApplication;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.fed.application.config.CApplicationAmbassador;
import org.eclipse.mosaic.interactions.mapping.ChargingStationRegistration;
import org.eclipse.mosaic.interactions.mapping.RsuRegistration;
import org.eclipse.mosaic.interactions.mapping.ServerRegistration;
import org.eclipse.mosaic.interactions.mapping.TmcRegistration;
import org.eclipse.mosaic.interactions.mapping.TrafficLightRegistration;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.lib.junit.IpResolverRule;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.lib.util.scheduling.EventManager;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.Interactable;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Tests the {@link UnitSimulator}.
 */
public class UnitSimulatorTest {

    private Interactable interactable = mock(Interactable.class);
    private List<Event> registeredEvents = new Vector<>();
    private EventManager eventManager = event -> registeredEvents.add(event);

    @Rule
    public SimulationKernelRule simulationKernel = new SimulationKernelRule(eventManager, interactable,
            Mockito.mock(CentralNavigationComponent.class), Mockito.mock(CentralPerceptionComponent.class));

    @Rule
    public IpResolverRule ipResolverRule = new IpResolverRule();

    @Before
    public void setup() {
        // clear the event list, which is used internally to catch events from the simulator
        registeredEvents.clear();

        SimulationKernel.SimulationKernel.setClassLoader(ClassLoader.getSystemClassLoader());
        SimulationKernel.SimulationKernel.setConfiguration(new CApplicationAmbassador());
    }

    /**
     * Removes all units from the simulator and checks, if
     * all units have been removed successfully.
     */
    @After
    public void tearDown() {
        UnitSimulator.UnitSimulator.removeAllSimulationUnits();
        assertEquals(0, UnitSimulator.UnitSimulator.getAllUnits().size());
        assertEquals(0, UnitSimulator.UnitSimulator.getChargingStations().size());
        assertEquals(0, UnitSimulator.UnitSimulator.getTrafficLights().size());
        assertEquals(0, UnitSimulator.UnitSimulator.getRoadSideUnits().size());
        assertEquals(0, UnitSimulator.UnitSimulator.getVehicles().size());
    }

    /**
     * Adds a vehicle station unit to the simulator and loads
     * its application. the setup and tearDown methods of the application
     * should be called, when the application is started by the simulator.
     * finally, the unit will be removed and it will be checked, if
     * the unit can be added again to the simulation.
     */
    @Test
    public void addAndRemoveSingleVehicle() {
        UnitSimulator sim = UnitSimulator.UnitSimulator;

        // Add Vehicles to simulation
        VehicleRegistration vehicleRegistrationInteraction =
                InteractionTestHelper.createVehicleRegistrationInteraction("veh_0", 4, TestVehicleApplication.class);

        // ADD VEHICLE
        sim.registerVehicle(5 * TIME.SECOND, vehicleRegistrationInteraction);

        VehicleApplication application = addAndLoadSingleUnit(sim, "veh_0");

        // REMOVE SINGLE UNIT
        sim.processEvent(eventManager.newEvent(0, sim).withResource(new RemoveVehicles(Collections.singletonList("veh_0"))).schedule());

        // VERIFY CALL OF: tearDown
        verify(application, times(1)).onShutdown();

        // remove all units -> tearDown should not be called
        sim.removeAllSimulationUnits();

        // VERIFY still one call of tearDown
        verify(application, times(1)).onShutdown();

        // try to add the unit again, remove it afterwards
        sim.registerVehicle(5 * TIME.SECOND, vehicleRegistrationInteraction);
    }

    /**
     * Adds a server unit to the simulator and loads
     * its application. the setup and tearDown methods of the application
     * should be called, when the application is started by the simulator.
     * finally, the unit will be removed and it will be checked, if
     * the unit can be added again to the simulation.
     */
    @Test
    public void addSingleServer() {
        UnitSimulator sim = UnitSimulator.UnitSimulator;

        // Add Vehicles to simulation
        ServerRegistration serverRegistration =
                InteractionTestHelper.createServerRegistration("server_0", 5, true);

        // ADD VEHICLE
        sim.registerServer(serverRegistration);

        Application application = addAndLoadSingleUnit(sim, "server_0");

        // VERIFY CALL OF: tearDown
        verify(application, times(1)).onStartup();

        // remove all units -> tearDown should not be called
        sim.removeAllSimulationUnits();

        // VERIFY still one call of tearDown
        verify(application, times(1)).onShutdown();

        // try to add the unit again
        sim.registerServer(serverRegistration);
        sim.removeAllSimulationUnits();
    }

    /**
     * Adds a traffic light station unit to the simulator and loads
     * its application. the setup and tearDown methods of the application
     * should be called, when the application is started by the simulator.
     * finally, the unit will be removed and it will be checked, if
     * the unit can be added again to the simulation.
     */
    @Test
    public void addAndRemoveSingleTrafficLight() {
        UnitSimulator sim = UnitSimulator.UnitSimulator;

        // Add Vehicles to simulation
        TrafficLightRegistration trafficLightRegistrationMessage = InteractionTestHelper.createTrafficLightRegistration("tl_0", 5, true);

        // ADD TRAFFIC LIGHT
        sim.registerTrafficLight(trafficLightRegistrationMessage);

        TrafficLightApplication application = addAndLoadSingleUnit(sim, "tl_0");

        // REMOVE SINGLE UNIT
        sim.removeAllSimulationUnits();

        // VERIFY CALL OF: tearDown
        verify(application, times(1)).onShutdown();

        // try to add the unit again
        sim.registerTrafficLight(trafficLightRegistrationMessage);
        sim.removeAllSimulationUnits();
    }

    /**
     * Adds a single charging station unit to the simulator and loads
     * its application. the setup and tearDown methods of the application
     * should be called, when the application is started by the simulator.
     * finally, the unit will be removed and it will be checked, if
     * the unit can be added again to the simulation.
     */
    @Test
    public void addAndRemoveSingleChargingStation() {
        UnitSimulator sim = UnitSimulator.UnitSimulator;

        // Add Vehicles to simulation
        ChargingStationRegistration chargingStationRegistration = InteractionTestHelper.createChargingStationRegistration("cs_0", 5, true);

        // ADD TRAFFIC LIGHT
        sim.registerChargingStation(chargingStationRegistration);

        ChargingStationApplication application = addAndLoadSingleUnit(sim, "cs_0");

        // REMOVE SINGLE UNIT
        sim.removeAllSimulationUnits();

        // VERIFY CALL OF: tearDown
        verify(application, times(1)).onShutdown();

        // try to add the unit again
        sim.registerChargingStation(chargingStationRegistration);
        sim.removeAllSimulationUnits();
    }

    /**
     * Adds a single traffic management center unit to the simulator and loads
     * its application. the setup and tearDown methods of the application
     * should be called, when the application is started by the simulator.
     * finally, the unit will be removed and it will be checked, if
     * the unit can be added again to the simulation.
     */
    @Test
    public void addAndRemoveSingleTrafficManagementCenter() {
        UnitSimulator sim = UnitSimulator.UnitSimulator;

        // Add Vehicles to simulation
        TmcRegistration tmcRegistration = InteractionTestHelper.createTmcRegistrationWithInductionLoops("tmc_0", 5, true, "ind1");

        // ADD TRAFFIC LIGHT
        sim.registerTmc(tmcRegistration);

        TrafficManagementCenterApplication application = addAndLoadSingleUnit(sim, "tmc_0");

        // REMOVE SINGLE UNIT
        sim.removeAllSimulationUnits();

        // VERIFY CALL OF: tearDown
        verify(application, times(1)).onShutdown();

        // try to add the unit again
        sim.registerTmc(tmcRegistration);
        sim.removeAllSimulationUnits();
    }

    /**
     * Adds a single road side unit to the simulator and loads
     * its application. the setup and tearDown methods of the application
     * should be called, when the application is started by the simulator.
     * finally, the unit will be removed and it will be checked, if
     * the unit can be added again to the simulation.
     */
    @Test
    public void addAndRemoveSingleRoadSideUnit() {
        UnitSimulator sim = UnitSimulator.UnitSimulator;

        // Add Vehicles to simulation
        RsuRegistration rsuRegistration = InteractionTestHelper.createRsuRegistration("rsu_0", 5, true);

        // ADD RSU
        sim.registerRsu(rsuRegistration);

        Application application = addAndLoadSingleUnit(sim, "rsu_0");

        // REMOVE SINGLE UNIT
        sim.removeAllSimulationUnits();

        // VERIFY CALL OF: tearDown
        verify(application, times(1)).onShutdown();

        // try to add the unit again
        sim.registerRsu(rsuRegistration);
        sim.removeAllSimulationUnits();
    }


    /**
     * Adds a charging station unit to the simulator twice with the same id.
     * An exception should occur.
     */
    @Test(expected = RuntimeException.class)
    public void addChargingStationTwice() {
        UnitSimulator sim = UnitSimulator.UnitSimulator;

        ChargingStationRegistration chargingStationRegistration = InteractionTestHelper.createChargingStationRegistration("cs_0", 0, true);
        sim.registerChargingStation(chargingStationRegistration);
        sim.registerChargingStation(chargingStationRegistration);
    }

    /**
     * Adds a road side unit to the simulator twice with the same id.
     * An exception should occur.
     */
    @Test(expected = RuntimeException.class)
    public void addRoadSideUnitTwice() {
        UnitSimulator sim = UnitSimulator.UnitSimulator;

        RsuRegistration rsuRegistration = InteractionTestHelper.createRsuRegistration("rsu_0", 0, true);
        sim.registerRsu(rsuRegistration);
        sim.registerRsu(rsuRegistration);
    }

    /**
     * Adds a traffic light unit to the simulator twice with the same id.
     * An exception should occur.
     */
    @Test(expected = RuntimeException.class)
    public void addTrafficLightTwice() {
        UnitSimulator sim = UnitSimulator.UnitSimulator;

        TrafficLightRegistration trafficLightRegistration = InteractionTestHelper.createTrafficLightRegistration("tl_0", 0, true);
        sim.registerTrafficLight(trafficLightRegistration);
        sim.registerTrafficLight(trafficLightRegistration);
    }

    /**
     * Adds a vehicle unit to the simulator twice with the same id.
     * An exception should occur.
     */
    @Test(expected = RuntimeException.class)
    public void addVehicleTwice() {
        UnitSimulator sim = UnitSimulator.UnitSimulator;

        VehicleRegistration vehicleRegistration =
                InteractionTestHelper.createVehicleRegistrationInteraction("veh_0", 0, TestVehicleApplication.class);
        sim.registerVehicle(0, vehicleRegistration);
        sim.registerVehicle(0, vehicleRegistration);
    }

    /**
     * Adds a vehicle unit without an application. No
     * unit should be added to the simulator
     */
    @Test
    public void addVehicleWithoutApplication() {
        UnitSimulator sim = UnitSimulator.UnitSimulator;

        VehicleRegistration vehicleRegistration = InteractionTestHelper.createVehicleRegistrationInteraction("veh_0", 0, null);
        sim.registerVehicle(0, vehicleRegistration);
        assertEquals(0, sim.getAllUnits().size());
        assertEquals(0, sim.getVehicles().size());
    }

    /**
     * Adds a traffic light unit without an application. No
     * unit should be added to the simulator
     */
    @Test
    public void addTrafficLightWithoutApplication() {
        UnitSimulator sim = UnitSimulator.UnitSimulator;

        TrafficLightRegistration trafficLightRegistration = InteractionTestHelper.createTrafficLightRegistration("tl_0", 0, false);
        sim.registerTrafficLight(trafficLightRegistration);
        assertEquals(0, sim.getAllUnits().size());
        assertEquals(0, sim.getTrafficLights().size());
    }

    /**
     * Adds a charging station unit without an application. No
     * unit should be added to the simulator
     */
    @Test
    public void addChargingStationWithoutApplication() {
        UnitSimulator sim = UnitSimulator.UnitSimulator;

        ChargingStationRegistration chargingStationRegistration = InteractionTestHelper.createChargingStationRegistration("cs_0", 0, false);
        sim.registerChargingStation(chargingStationRegistration);
        assertEquals(0, sim.getAllUnits().size());
        assertEquals(0, sim.getChargingStations().size());
    }

    /**
     * Adds a road side unit without an application. No
     * unit should be added to the simulator
     */
    @Test
    public void addRoadSideUnitWithoutApplication() {
        UnitSimulator sim = UnitSimulator.UnitSimulator;

        RsuRegistration rsuRegistration = InteractionTestHelper.createRsuRegistration("rsu_0", 0, false);
        sim.registerRsu(rsuRegistration);
        assertEquals(0, sim.getAllUnits().size());
        assertEquals(0, sim.getRoadSideUnits().size());
    }

    /**
     * Adds a server unit without an application. No
     * unit should be added to the simulator
     */
    @Test
    public void addServerUnitWithoutApplication() {
        UnitSimulator sim = UnitSimulator.UnitSimulator;

        ServerRegistration serverRegistration = InteractionTestHelper.createServerRegistration("server_0", 0, false);
        sim.registerServer(serverRegistration);
        assertEquals(0, sim.getAllUnits().size());
        assertEquals(0, sim.getRoadSideUnits().size());
    }


    @SuppressWarnings("unchecked")
    private <SPY_APP_CLASS extends Application> SPY_APP_CLASS addAndLoadSingleUnit(UnitSimulator sim, String id) {
        AbstractSimulationUnit unit = sim.getAllUnits().get(id);

        assertNotNull(unit);
        assertEquals(id, unit.getId());

        assertEquals(1, registeredEvents.size());

        Event event = registeredEvents.get(0);
        assertEquals(5 * TIME.SECOND, event.getTime());

        // PROCESS EVENT -> START APPLICATION
        sim.processEvent(event);

        assertEquals(1, unit.getApplications().size());

        TestApplicationWithSpy<SPY_APP_CLASS> application = (TestApplicationWithSpy<SPY_APP_CLASS>) unit.getApplications().get(0);
        SPY_APP_CLASS applicationSpy = application.getApplicationSpy();

        // VERIFY APPLICATION CALL: setUp
        verify(applicationSpy, times(1)).onStartup();

        return applicationSpy;
    }

}
