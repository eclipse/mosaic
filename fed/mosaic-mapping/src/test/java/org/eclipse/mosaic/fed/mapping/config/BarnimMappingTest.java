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

package org.eclipse.mosaic.fed.mapping.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.fed.mapping.config.units.CRoadSideUnit;
import org.eclipse.mosaic.fed.mapping.config.units.CVehicle;
import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;
import org.eclipse.mosaic.rti.TIME;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;


/**
 * Tests if loading of Barnim-Mapping works like expected.
 */
public class BarnimMappingTest {

    private static final double EPSILON = 1e-5;

    private CMappingAmbassador mapping;

    /**
     * Loads Barnim-Mapping from JSON file.
     */
    @Before
    public void setup() throws Exception {
        mapping = getMapping("/mapping/Barnim.json");
        assertNotEquals(mapping, null);
    }

    /**
     * Loads a Mapping JSON file.
     *
     * @param mappingPath Path of the mapping file to load.
     * @return Corresponding {@link CMappingAmbassador}.
     */
    private CMappingAmbassador getMapping(String mappingPath) throws IOException, InstantiationException {
        try (InputStream input = getClass().getResourceAsStream(mappingPath)) {
            return new ObjectInstantiation<>(CMappingAmbassador.class).read(input);
        }
    }

    /**
     * Tests every possible values of every {@link CPrototype} defined in the mapping file.
     */
    @Test
    public void checkPrototypes() {
        assertNotNull(mapping.prototypes);
        assertEquals(3, mapping.prototypes.size());

        // CPrototype "PKW"
        CPrototype pkw = mapping.prototypes.get(0);
        assertNotNull(pkw);
        assertEquals(pkw.name, "PKW");
        assertNull(pkw.group);
        assertNull(pkw.vehicleClass);
        assertNull(pkw.applications);
        assertEquals(pkw.accel, new Double(2.6));
        assertEquals(pkw.decel, new Double(4.5));
        assertEquals(pkw.length, new Double(5.0));
        assertEquals(pkw.maxSpeed, new Double(70.0));
        assertEquals(pkw.minGap, new Double(2.5));
        assertEquals(pkw.sigma, new Double(0.5));
        assertEquals(pkw.tau, new Double(1));
        assertNull(pkw.weight);

        // CPrototype "electricPKW"
        CPrototype electricPKW = mapping.prototypes.get(1);
        assertNotNull(electricPKW);
        assertEquals(electricPKW.name, "electricPKW");
        assertNull(electricPKW.applications);
        assertNull(electricPKW.group);
        assertEquals(electricPKW.vehicleClass, VehicleClass.ElectricVehicle);
        assertEquals(electricPKW.accel, new Double(2.6));
        assertEquals(electricPKW.decel, new Double(4.5));
        assertEquals(electricPKW.length, new Double(5.0));
        assertEquals(electricPKW.maxSpeed, new Double(40.0));
        assertEquals(electricPKW.minGap, new Double(2.5));
        assertEquals(electricPKW.sigma, new Double(0.5));
        assertEquals(electricPKW.tau, new Double(1));
        assertNull(electricPKW.weight);

        // CPrototype UNNAMED ("WeatherServer")
        CPrototype weatherServer = mapping.prototypes.get(2);
        assertNotNull(weatherServer);
        assertEquals(weatherServer.name, "WeatherServer");
        assertNull(weatherServer.group);
        assertNull(weatherServer.vehicleClass);
        assertEquals(weatherServer.applications.size(), 1);
        assertEquals(weatherServer.applications.get(0), "org.eclipse.mosaic.app.tutorials.barnim.WeatherServer");
        assertNull(weatherServer.accel);
        assertNull(weatherServer.decel);
        assertNull(weatherServer.length);
        assertNull(weatherServer.maxSpeed);
        assertNull(weatherServer.minGap);
        assertNull(weatherServer.sigma);
        assertNull(weatherServer.tau);
        assertNull(weatherServer.weight);
    }

    /**
     * Barnim-Mapping:
     * Tests every possible values of every {@link CRoadSideUnit} defined in the mapping file.
     */
    @Test
    public void checkRsus() {
        assertNotNull(mapping.rsus);
        assertEquals(1, mapping.rsus.size());

        // RSU "WeatherServer"
        CRoadSideUnit rsu = mapping.rsus.get(0);
        assertNotEquals(rsu, null);
        assertNull(rsu.applications);
        assertNull(rsu.group);
        assertEquals(rsu.name, "WeatherServer");
        assertEquals(rsu.position.getLatitude(), 52.65027421760045, EPSILON);
        assertEquals(rsu.position.getLongitude(), 13.545005321502686, EPSILON);
    }

    /**
     * Barnim-Mapping:
     * Tests every possible value of every {@link CVehicle} defined in the mapping file.
     */
    @Test
    public void checkVehicles() {
        assertNotNull(mapping.vehicles);
        assertEquals(mapping.vehicles.size(), 1);

        // Vehicle
        CVehicle vehicle = mapping.vehicles.get(0);
        assertNotEquals(vehicle, null);
        assertNull(vehicle.destination);
        assertNull(vehicle.origin);
        assertTrue(vehicle.deterministic);
        assertNull(vehicle.group);
        assertNull(vehicle.lanes);
        assertEquals(vehicle.maxNumberVehicles, new Integer(120));
        assertNull(vehicle.maxTime);
        assertEquals(vehicle.pos, 0);
        assertEquals(vehicle.route, "1");
        assertEquals(5 * TIME.SECOND, vehicle.startingTime);
        checkVehicleTypes(vehicle);
    }

    /**
     * Tests every possible value of every {@link CPrototype} defined in the given {@link CVehicle}.
     */
    private void checkVehicleTypes(CVehicle vehicle) {
        assertNotEquals(vehicle.types, null);
        assertEquals(vehicle.types.size(), 4);

        CPrototype CPrototype1 = vehicle.types.get(0);
        assertNotNull(CPrototype1);
        assertEquals(CPrototype1.name, "PKW");
        assertNull(CPrototype1.accel);
        assertNull(CPrototype1.decel);
        assertNull(CPrototype1.length);
        assertNull(CPrototype1.maxSpeed);
        assertNull(CPrototype1.minGap);
        assertNull(CPrototype1.sigma);
        assertNull(CPrototype1.tau);
        assertEquals(CPrototype1.weight, new Double(0.1));
        assertNotEquals(CPrototype1.applications, null);
        assertEquals(CPrototype1.applications.size(), 2);
        assertNotEquals(CPrototype1.applications.get(0), null);
        assertEquals(CPrototype1.applications.get(0), "org.eclipse.mosaic.app.tutorials.barnim.WeatherWarningAppCell");
        assertNotEquals(CPrototype1.applications.get(1), null);
        assertEquals(CPrototype1.applications.get(1), "org.eclipse.mosaic.app.tutorials.barnim.SlowDownApp");

        CPrototype CPrototype2 = vehicle.types.get(1);
        assertNotNull(CPrototype2);
        assertEquals(CPrototype2.name, "PKW");
        assertNull(CPrototype2.accel);
        assertNull(CPrototype2.decel);
        assertNull(CPrototype2.length);
        assertNull(CPrototype2.maxSpeed);
        assertNull(CPrototype2.minGap);
        assertNull(CPrototype2.sigma);
        assertNull(CPrototype2.tau);
        assertEquals(CPrototype2.weight, new Double(0.2));
        assertNotEquals(CPrototype2.applications, null);
        assertEquals(CPrototype2.applications.size(), 2);
        assertNotEquals(CPrototype2.applications.get(0), null);
        assertEquals(CPrototype2.applications.get(0), "org.eclipse.mosaic.app.tutorials.barnim.WeatherWarningApp");
        assertNotEquals(CPrototype2.applications.get(1), null);
        assertEquals(CPrototype2.applications.get(1), "org.eclipse.mosaic.app.tutorials.barnim.SlowDownApp");

        CPrototype CPrototype3 = vehicle.types.get(2);
        assertNotNull(CPrototype3);
        assertEquals(CPrototype3.name, "PKW");
        assertNull(CPrototype3.accel);
        assertNull(CPrototype3.decel);
        assertNull(CPrototype3.length);
        assertNull(CPrototype3.maxSpeed);
        assertNull(CPrototype3.minGap);
        assertNull(CPrototype3.sigma);
        assertNull(CPrototype3.tau);
        assertEquals(CPrototype3.weight, new Double(0.6));
        assertNotEquals(CPrototype3.applications, null);
        assertEquals(CPrototype3.applications.size(), 1);
        assertNotEquals(CPrototype3.applications.get(0), null);
        assertEquals(CPrototype3.applications.get(0), "org.eclipse.mosaic.app.tutorials.barnim.SlowDownApp");

        CPrototype CPrototype4 = vehicle.types.get(3);
        assertNotNull(CPrototype4);
        assertEquals(CPrototype4.name, "electricPKW");
        assertNull(CPrototype4.accel);
        assertNull(CPrototype4.decel);
        assertNull(CPrototype4.length);
        assertNull(CPrototype4.maxSpeed);
        assertNull(CPrototype4.minGap);
        assertNull(CPrototype4.sigma);
        assertNull(CPrototype4.tau);
        assertEquals(CPrototype4.weight, new Double(0.1));
        assertNotEquals(CPrototype4.applications, null);
        assertEquals(CPrototype4.applications.size(), 1);
        assertNotEquals(CPrototype4.applications.get(0), null);
        assertEquals(CPrototype4.applications.get(0), "org.eclipse.mosaic.app.tutorials.barnim.SlowDownApp");
    }
}
