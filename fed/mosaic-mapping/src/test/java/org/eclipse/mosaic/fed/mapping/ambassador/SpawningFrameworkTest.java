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

package org.eclipse.mosaic.fed.mapping.ambassador;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.mosaic.fed.mapping.config.CMappingAmbassador;
import org.eclipse.mosaic.fed.mapping.config.CPrototype;
import org.eclipse.mosaic.fed.mapping.config.units.CChargingStation;
import org.eclipse.mosaic.fed.mapping.config.units.CRoadSideUnit;
import org.eclipse.mosaic.fed.mapping.config.units.CServer;
import org.eclipse.mosaic.fed.mapping.config.units.CTrafficLight;
import org.eclipse.mosaic.fed.mapping.config.units.CTrafficManagementCenter;
import org.eclipse.mosaic.fed.mapping.config.units.CVehicle;
import org.eclipse.mosaic.interactions.mapping.ChargingStationRegistration;
import org.eclipse.mosaic.interactions.mapping.RsuRegistration;
import org.eclipse.mosaic.interactions.mapping.ServerRegistration;
import org.eclipse.mosaic.interactions.mapping.TmcRegistration;
import org.eclipse.mosaic.interactions.mapping.TrafficLightRegistration;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.interactions.mapping.advanced.ScenarioTrafficLightRegistration;
import org.eclipse.mosaic.interactions.traffic.VehicleTypesInitialization;
import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.RtiAmbassador;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Tests for {@link SpawningFramework}.
 */
public class SpawningFrameworkTest {

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    public RtiAmbassador rti;

    private RandomNumberGenerator rng;

    @Before
    public void setup() {
        rng = new DefaultRandomNumberGenerator(0L);
    }

    @Test
    public void generateVehicleTypesInitialization_noSpawner_noVehicleTypes() {
        //SETUP
        CMappingAmbassador framework = new CMappingAmbassador();
        framework.prototypes = Lists.newArrayList(newPrototype("prototype1"));

        //RUN
        SpawningFramework spawningFramework = new SpawningFramework(framework, null, rti, rng);

        //ASSERT
        VehicleTypesInitialization vehicleTypesInitialization = spawningFramework.generateVehicleTypesInitialization();
        assertNotNull(vehicleTypesInitialization);
        assertTrue(vehicleTypesInitialization.getTypes().isEmpty());
    }

    @Test
    public void generateVehicleTypesInitialization_twoPrototypesOneSpawner_oneVehicleType() {
        //SETUP
        CMappingAmbassador framework = new CMappingAmbassador();
        framework.prototypes = Lists.newArrayList(newPrototype("prototype1"), newPrototype("prototype2"));
        framework.vehicles = Lists.newArrayList(newSpawner("prototype1"));

        //RUN
        SpawningFramework spawningFramework = new SpawningFramework(framework, null, rti, rng);

        //ASSERT
        VehicleTypesInitialization vehicleTypesInitialization = spawningFramework.generateVehicleTypesInitialization();
        assertNotNull(vehicleTypesInitialization);
        assertEquals(1, vehicleTypesInitialization.getTypes().size());

        VehicleType type = vehicleTypesInitialization.getTypes().get("prototype1");
        assertEquals("prototype1", type.getName());
    }

    @Test
    public void initVehiclesRsusTls() throws IllegalValueException, InternalFederateException {
        //SETUP
        CMappingAmbassador framework = new CMappingAmbassador();
        framework.prototypes = Lists.newArrayList(newPrototype("prototype"));
        framework.vehicles = Lists.newArrayList(newSpawner("prototype"));
        framework.rsus = Lists.newArrayList(newRsu("prototype"));
        framework.trafficLights = Lists.newArrayList(newTl("prototype"));

        ScenarioTrafficLightRegistration scenarioTrafficLightRegistration = MappingAmbassadorTest.createScenarioTrafficLightRegistration();

        //RUN
        SpawningFramework spawningFramework = new SpawningFramework(framework, scenarioTrafficLightRegistration, rti, rng);
        spawningFramework.timeAdvance(0, rti, rng);

        //ASSERT
        verify(rti, times(1)).triggerInteraction(isA(VehicleRegistration.class));
        verify(rti, times(1)).triggerInteraction(isA(RsuRegistration.class));
        verify(rti, times(1)).triggerInteraction(isA(TrafficLightRegistration.class));

        verify(rti, never()).triggerInteraction(isA(ChargingStationRegistration.class));
        verify(rti, never()).triggerInteraction(isA(TmcRegistration.class));
    }

    @Test
    public void initChargingStationsTmcsServers() throws IllegalValueException, InternalFederateException {
        //SETUP
        CMappingAmbassador mappingAmbassadorConfig = new CMappingAmbassador();
        mappingAmbassadorConfig.prototypes = Lists.newArrayList(newPrototype("prototype"));
        mappingAmbassadorConfig.chargingStations = Lists.newArrayList(newChargingStation("chType"));
        mappingAmbassadorConfig.tmcs = Lists.newArrayList(newTmc("tmcType"));
        mappingAmbassadorConfig.servers = Lists.newArrayList(newServer("serverType"));

        ScenarioTrafficLightRegistration scenarioTrafficLightRegistration = MappingAmbassadorTest.createScenarioTrafficLightRegistration();

        //RUN
        SpawningFramework spawningFramework = new SpawningFramework(mappingAmbassadorConfig, scenarioTrafficLightRegistration, rti, rng);
        spawningFramework.timeAdvance(0, rti, rng);

        //ASSERT
        verify(rti, times(1)).triggerInteraction(isA(ChargingStationRegistration.class));
        verify(rti, times(1)).triggerInteraction(isA(TmcRegistration.class));
        verify(rti, times(1)).triggerInteraction(isA(ServerRegistration.class));
        verify(rti, times(1)).triggerInteraction(isA(TrafficLightRegistration.class));

        verify(rti, never()).triggerInteraction(isA(VehicleRegistration.class));
        verify(rti, never()).triggerInteraction(isA(RsuRegistration.class));
    }

    @Test
    public void odMatrixMappers() throws InternalFederateException, IllegalValueException, URISyntaxException, InstantiationException {
        //SETUP
        URI jsonConfig = getClass().getClassLoader().getResource("mapping_config_matrix_mappers.json").toURI();
        ObjectInstantiation<CMappingAmbassador> oi = new ObjectInstantiation<>(CMappingAmbassador.class);
        CMappingAmbassador framework = oi.readFile(new File(jsonConfig));
        framework.prototypes = Lists.newArrayList(newPrototype("prototype"));

        //RUN
        SpawningFramework spawningFramework = new SpawningFramework(framework, null, rti, rng);
        for (int i = 0; i < 3600; i++) {
            spawningFramework.timeAdvance(i * TIME.SECOND, rti, rng);
        }

        //ASSERT:
        // 7 vehicles should be spawned during one hour of simulation (according to sum of odValues in matrixMappers)
        verify(rti, times(7)).triggerInteraction(isA(VehicleRegistration.class));
    }

    private CTrafficManagementCenter newTmc(String prototype) {
        CTrafficManagementCenter trafficManagementCenterConfiguration = new CTrafficManagementCenter();
        trafficManagementCenterConfiguration.name = prototype;
        return trafficManagementCenterConfiguration;
    }

    private CServer newServer(String prototype) {
        CServer serverConfiguration = new CServer();
        serverConfiguration.name = prototype;
        return serverConfiguration;
    }

    private CChargingStation newChargingStation(String prototype) {
        CChargingStation chargingStationConfiguration = new CChargingStation();
        chargingStationConfiguration.name = prototype;
        return chargingStationConfiguration;
    }

    private CTrafficLight newTl(String prototype) {
        CTrafficLight trafficLightConfiguration = new CTrafficLight();
        trafficLightConfiguration.name = prototype;
        return trafficLightConfiguration;
    }

    private CRoadSideUnit newRsu(String prototype) {
        CRoadSideUnit roadSideUnitConfiguration = new CRoadSideUnit();
        roadSideUnitConfiguration.name = prototype;
        return roadSideUnitConfiguration;
    }

    private CVehicle newSpawner(String prototype) {
        CVehicle spawner = new CVehicle();
        spawner.route = "1";
        spawner.startingTime = 0;
        spawner.targetFlow = 1200;
        spawner.maxNumberVehicles = 100;
        spawner.types = Lists.newArrayList(newPrototype(prototype));
        return spawner;
    }

    private CPrototype newPrototype(String name) {
        CPrototype p = new CPrototype();
        p.name = name;
        return p;
    }

}