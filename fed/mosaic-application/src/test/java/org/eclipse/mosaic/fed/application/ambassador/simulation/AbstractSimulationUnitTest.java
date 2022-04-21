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

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.ambassador.SimulationKernelRule;
import org.eclipse.mosaic.fed.application.ambassador.navigation.CentralNavigationComponent;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.CentralPerceptionComponent;
import org.eclipse.mosaic.fed.application.app.TestElectricVehicleApplication;
import org.eclipse.mosaic.fed.application.app.TestVehicleApplication;
import org.eclipse.mosaic.fed.application.config.CApplicationAmbassador;
import org.eclipse.mosaic.lib.junit.IpResolverRule;
import org.eclipse.mosaic.lib.objects.vehicle.BatteryData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.lib.util.scheduling.EventManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class AbstractSimulationUnitTest {


    private List<Event> registeredEvents = new Vector<>();
    private EventManager eventManager = event -> registeredEvents.add(event);

    @Rule
    public IpResolverRule ipRes = new IpResolverRule();

    @Rule
    public SimulationKernelRule simKernel = new SimulationKernelRule(Mockito.mock(EventManager.class), null,
            Mockito.mock(CentralNavigationComponent.class), Mockito.mock(CentralPerceptionComponent.class));

    @Before
    public void before() {
        SimulationKernel.SimulationKernel.setConfiguration(new CApplicationAmbassador());
        SimulationKernel.SimulationKernel.setClassLoader(ClassLoader.getSystemClassLoader());
    }

    @Test
    public void loadApplication_VehicleApplicationOnElectricVehicle() throws Exception {
        ElectricVehicleUnit unit = new ElectricVehicleUnit("veh_0", new VehicleType("Default"), null);
        unit.loadApplications(Collections.singletonList(TestVehicleApplication.class.getCanonicalName()));

        unit.processEvent(eventManager.newEvent(0, unit).withResource(Mockito.mock(VehicleData.class)).schedule());
        // no error should be thrown
        unit.processEvent(eventManager.newEvent(0, unit).withResource(new BatteryData(0, "veh_0")).schedule());

        TestVehicleApplication app = unit.getApplicationsIterator(TestVehicleApplication.class).iterator().next();
        Mockito.verify(app.getApplicationSpy()).onVehicleUpdated(any(), any());
    }

    @Test
    public void loadApplication_VehicleApplicationOnVehicle() throws Exception {
        AbstractSimulationUnit unit = new VehicleUnit("veh_0", new VehicleType("Default"), null);
        unit.loadApplications(Collections.singletonList(TestVehicleApplication.class.getCanonicalName()));

        unit.processEvent(eventManager.newEvent(0, unit).withResource(Mockito.mock(VehicleData.class)).schedule());

        TestVehicleApplication app = unit.getApplicationsIterator(TestVehicleApplication.class).iterator().next();
        Mockito.verify(app.getApplicationSpy()).onVehicleUpdated(any(), any());
    }

    @Test
    public void loadApplication_ElectricVehicleApplicationOnElectricVehicle() throws Exception {
        AbstractSimulationUnit unit = new ElectricVehicleUnit("veh_0", new VehicleType("Default"), null);
        unit.loadApplications(Collections.singletonList(TestElectricVehicleApplication.class.getCanonicalName()));

        unit.processEvent(eventManager.newEvent(0, unit).withResource(Mockito.mock(VehicleData.class)).schedule());
        // no error should be thrown
        unit.processEvent(eventManager.newEvent(0, unit).withResource(new BatteryData(0, "veh_0")).schedule());

        TestElectricVehicleApplication app = unit.getApplicationsIterator(TestElectricVehicleApplication.class).iterator().next();
        Mockito.verify(app.getApplicationSpy()).onVehicleUpdated(any(), any());
        Mockito.verify(app.getApplicationSpy()).onBatteryDataUpdated(any(), any());
    }

    @Test(expected = RuntimeException.class)
    public void loadApplication_ElectricVehicleApplicationOnVehicle_fail() {
        AbstractSimulationUnit unit = new VehicleUnit("veh_0", null, null);
        unit.loadApplications(Collections.singletonList(TestElectricVehicleApplication.class.getCanonicalName())); // should throw an error
    }

}
