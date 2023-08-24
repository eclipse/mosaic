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
import org.eclipse.mosaic.fed.mapping.config.units.CTrafficLight;
import org.eclipse.mosaic.fed.mapping.config.units.CVehicle;
import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests if loading of Tiergarten-Mapping works like expected.
 */
public class TiergartenMappingTest {

    private static final double EPSILON = 1e-5;

    private CMappingAmbassador mapping;

    /**
     * Loads Tiergarten-Mapping from JSON file.
     */
    @Before
    public void setup() throws InstantiationException {
        mapping = getMapping("/mapping/Tiergarten.json");
        assertNotEquals(mapping, null);
    }

    /**
     * Loads a Mapping JSON file.
     *
     * @param mappingPath Path of the mapping file to load.
     * @return Corresponding {@link CMappingAmbassador}.
     */
    private CMappingAmbassador getMapping(String mappingPath) throws InstantiationException {
        return new ObjectInstantiation<>(CMappingAmbassador.class).read(getClass().getResourceAsStream(mappingPath));
    }

    /**
     * Tests every possible value of every {@link CPrototype} defined in the mapping file.
     */
    @Test
    public void checkPrototypes() {
        assertNotNull(mapping.prototypes);
        assertEquals(4, mapping.prototypes.size());

        // CPrototype "PKW"
        CPrototype pkw = mapping.prototypes.get(0);
        assertNotNull(pkw);
        assertEquals(pkw.name, "PKW");
        assertNull(pkw.group);
        assertNull(pkw.vehicleClass);
        assertEquals(pkw.applications.size(), 2);
        assertEquals(pkw.applications.get(0), "org.eclipse.mosaic.app.tutorials.tiergarten.vehicle.TiergartenVehicle");
        assertEquals(pkw.applications.get(1), "org.eclipse.mosaic.app.tutorials.tiergarten.vehicle.TiergartenVehicleSlave");
        assertEquals(pkw.accel, (Double) 1.5);
        assertEquals(pkw.decel, (Double) 4.5);
        assertEquals(pkw.length, (Double) 5.0);
        assertEquals(pkw.maxSpeed, (Double) 10.0);
        assertEquals(pkw.minGap, (Double) 2.5);
        assertEquals(pkw.sigma, (Double) 0.5);
        assertEquals(pkw.tau, (Double) 1.0);
        assertNull(pkw.weight);

        // CPrototype "TLPKW"
        CPrototype tlpkw = mapping.prototypes.get(1);
        assertNotNull(tlpkw);
        assertEquals(tlpkw.name, "TLPKW");
        assertNull(tlpkw.group);
        assertNull(tlpkw.vehicleClass);
        assertEquals(tlpkw.applications.size(), 1);
        assertEquals(tlpkw.applications.get(0), "org.eclipse.mosaic.app.tutorials.tiergarten.vehicle.TrafficLightVehicle");
        assertNull(tlpkw.accel);
        assertNull(tlpkw.decel);
        assertNull(tlpkw.length);
        assertNull(tlpkw.maxSpeed);
        assertNull(tlpkw.minGap);
        assertNull(tlpkw.sigma);
        assertNull(tlpkw.tau);
        assertNull(tlpkw.weight);

        // CPrototype "RSU"
        CPrototype rsu = mapping.prototypes.get(2);
        assertNotNull(rsu);
        assertEquals(rsu.name, "RSU");
        assertNull(rsu.group);
        assertNull(rsu.vehicleClass);
        assertEquals(rsu.applications.size(), 1);
        assertEquals(rsu.applications.get(0), "org.eclipse.mosaic.app.tutorials.tiergarten.rsu.TiergartenRSU");
        assertNull(rsu.accel);
        assertNull(rsu.decel);
        assertNull(rsu.length);
        assertNull(rsu.maxSpeed);
        assertNull(rsu.minGap);
        assertNull(rsu.sigma);
        assertNull(rsu.tau);
        assertNull(rsu.weight);

        // CPrototype "TrafficLight"
        CPrototype trafficLight = mapping.prototypes.get(3);
        assertNotNull(trafficLight);
        assertEquals(trafficLight.name, "TrafficLight");
        assertNull(trafficLight.group);
        assertNull(trafficLight.vehicleClass);
        assertEquals(trafficLight.applications.size(), 1);
        assertEquals(trafficLight.applications.get(0), "org.eclipse.mosaic.app.tutorials.tiergarten.trafficLight.TrafficLightApp");
        assertNull(trafficLight.accel);
        assertNull(trafficLight.decel);
        assertNull(trafficLight.length);
        assertNull(trafficLight.maxSpeed);
        assertNull(trafficLight.minGap);
        assertNull(trafficLight.sigma);
        assertNull(trafficLight.tau);
        assertNull(trafficLight.weight);
    }

    /**
     * Tests every possible value of every {@link CTrafficLight} defined in the mapping file.
     */
    @Test
    public void checkTrafficLights() {
        assertNotNull(mapping.trafficLights);
        assertEquals(2, mapping.trafficLights.size());

        // TrafficLight "27011311"
        CTrafficLight trafficLight1 = mapping.trafficLights.get(0);
        assertNotNull(trafficLight1);
        assertEquals(trafficLight1.name, "TrafficLight");
        assertNull(trafficLight1.group);
        assertEquals(trafficLight1.tlGroupId, "27011311");
        assertNull(trafficLight1.applications);

        // TrafficLight "252864801"
        CTrafficLight trafficLight2 = mapping.trafficLights.get(1);
        assertNotNull(trafficLight2);
        assertEquals(trafficLight2.name, "TrafficLight");
        assertNull(trafficLight2.group);
        assertEquals(trafficLight2.tlGroupId, "252864801");
        assertNull(trafficLight2.applications);
    }

    /**
     * Tests every possible value of every {@link CRoadSideUnit} defined in the mapping file.
     */
    @Test
    public void checkRsus() {
        assertNotNull(mapping.rsus);
        assertEquals(1, mapping.rsus.size());

        // RSU
        CRoadSideUnit rsu = mapping.rsus.get(0);
        assertNotNull(rsu);
        assertNull(rsu.applications);
        assertNull(rsu.group);
        assertEquals(rsu.name, "RSU");
        assertEquals(rsu.position.getLatitude(), 52.513060766781614, EPSILON);
        assertEquals(rsu.position.getLongitude(), 13.32891047000885, EPSILON);
    }

    /**
     * Tests every possible value of every {@link CVehicle} defined in the mapping file.
     */
    @Test
    public void checkVehicles() {
        assertNotNull(mapping.vehicles);
        assertEquals(mapping.vehicles.size(), 4);

        // Vehicle 1
        CVehicle vehicle1 = mapping.vehicles.get(0);
        assertNotNull(vehicle1);
        assertNull(vehicle1.destination);
        assertNull(vehicle1.origin);
        assertTrue(vehicle1.fixedorder);
        assertNull(vehicle1.group);
        assertNull(vehicle1.lanes);
        assertEquals(vehicle1.maxNumberVehicles, (Integer) 1);
        assertNull(vehicle1.maxTime);
        assertEquals(vehicle1.pos, 0);
        assertEquals(vehicle1.route, "0");
        assertEquals(1.0, vehicle1.startingTime, 0.0);
        assertNotNull(vehicle1.types);
        assertEquals(vehicle1.types.size(), 1);

        CPrototype CPrototype1 = vehicle1.types.get(0);
        assertNotNull(CPrototype1);
        assertEquals(CPrototype1.name, "PKW");
        assertNull(CPrototype1.accel);
        assertNull(CPrototype1.decel);
        assertNull(CPrototype1.length);
        assertNull(CPrototype1.maxSpeed);
        assertNull(CPrototype1.minGap);
        assertNull(CPrototype1.sigma);
        assertNull(CPrototype1.tau);
        assertNull(CPrototype1.weight);
        assertNull(CPrototype1.applications);

        // Vehicle 2
        CVehicle vehicle2 = mapping.vehicles.get(1);
        assertNotNull(vehicle2);
        assertNull(vehicle2.destination);
        assertNull(vehicle2.origin);
        assertTrue(vehicle2.fixedorder);
        assertNull(vehicle2.group);
        assertNull(vehicle2.lanes);
        assertEquals((Integer) 1, vehicle2.maxNumberVehicles);
        assertNull(vehicle2.maxTime);
        assertEquals(vehicle2.pos, 0);
        assertEquals(vehicle2.route, "0");
        assertEquals(5.0, vehicle2.startingTime, 0.0);
        assertNotNull(vehicle2.types);
        assertEquals(1, vehicle2.types.size());

        CPrototype CPrototype2 = vehicle2.types.get(0);
        assertNotNull(CPrototype2);
        assertEquals(CPrototype2.name, "PKW");
        assertNull(CPrototype2.accel);
        assertNull(CPrototype2.decel);
        assertNull(CPrototype2.length);
        assertNull(CPrototype2.maxSpeed);
        assertNull(CPrototype2.minGap);
        assertNull(CPrototype2.sigma);
        assertNull(CPrototype2.tau);
        assertNull(CPrototype2.weight);
        assertNull(CPrototype2.applications);

        // Vehicle 3
        CVehicle vehicle3 = mapping.vehicles.get(2);
        assertNotNull(vehicle3);
        assertNull(vehicle3.destination);
        assertNull(vehicle3.origin);
        assertTrue(vehicle3.fixedorder);
        assertNull(vehicle3.group);
        assertNull(vehicle3.lanes);
        assertEquals(vehicle3.maxNumberVehicles, (Integer) 1);
        assertNull(vehicle3.maxTime);
        assertEquals(vehicle3.pos, 0);
        assertEquals(vehicle3.route, "0");
        assertEquals(8.0, vehicle3.startingTime, 0.0);
        assertNotNull(vehicle3.types);
        assertEquals(1, vehicle3.types.size());

        CPrototype CPrototype3 = vehicle3.types.get(0);
        assertNotNull(CPrototype3);
        assertEquals(CPrototype3.name, "TLPKW");
        assertNull(CPrototype3.accel);
        assertNull(CPrototype3.decel);
        assertNull(CPrototype3.length);
        assertNull(CPrototype3.maxSpeed);
        assertNull(CPrototype3.minGap);
        assertNull(CPrototype3.sigma);
        assertNull(CPrototype3.tau);
        assertNull(CPrototype3.weight);
        assertNull(CPrototype3.applications);

        // Vehicle 4
        CVehicle vehicle4 = mapping.vehicles.get(3);
        assertNotNull(vehicle4);
        assertNull(vehicle4.destination);
        assertNull(vehicle4.origin);
        assertTrue(vehicle4.fixedorder);
        assertNull(vehicle4.group);
        assertNull(vehicle4.lanes);
        assertEquals((Integer) 1, vehicle4.maxNumberVehicles);
        assertNull(vehicle4.maxTime);
        assertEquals(0, vehicle4.pos);
        assertEquals("0", vehicle4.route);
        assertEquals(10.0, vehicle4.startingTime, 0.0);
        assertNotNull(vehicle4.types);
        assertEquals(1, vehicle4.types.size());

        CPrototype CPrototype4 = vehicle4.types.get(0);
        assertNotNull(CPrototype4);
        assertEquals("PKW", CPrototype4.name);
        assertNull(CPrototype4.accel);
        assertNull(CPrototype4.decel);
        assertNull(CPrototype4.length);
        assertNull(CPrototype4.maxSpeed);
        assertNull(CPrototype4.minGap);
        assertNull(CPrototype4.sigma);
        assertNull(CPrototype4.tau);
        assertNull(CPrototype4.weight);
        assertNull(CPrototype4.applications);
    }
}
