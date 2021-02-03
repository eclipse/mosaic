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

import org.eclipse.mosaic.fed.mapping.config.units.CVehicle;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleDeparture;
import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * Tests if the departSpeed and departSpeedMode parameters work as expected.
 */
public class DepartSpeedTest {

    private CMappingAmbassador mapping;

    /**
     * Loads Barnim-Mapping from JSON file.
     */
    @Before
    public void setup() throws Exception {
        mapping = getMapping("/mapping/departspeed.json");
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

    @Test
    public void checkPreciseDepartSpeedMode() {
        CVehicle vehicle = mapping.vehicles.get(0);
        assertNotEquals(vehicle, null);
        assertEquals(100, vehicle.departSpeed, 0);
        assertEquals(VehicleDeparture.DepartSpeedMode.PRECISE, vehicle.departSpeedMode);
    }

    @Test
    public void checkRandomDepartSpeedMode() {
        CVehicle vehicle = mapping.vehicles.get(1);
        assertNotEquals(vehicle, null);
        assertEquals(200, vehicle.departSpeed, 0);
        assertEquals(VehicleDeparture.DepartSpeedMode.RANDOM, vehicle.departSpeedMode);
    }

    @Test
    public void checkMaximumDepartSpeedMode() {
        CVehicle vehicle = mapping.vehicles.get(2);
        assertNotEquals(vehicle, null);
        assertEquals(300, vehicle.departSpeed, 0);
        assertEquals(VehicleDeparture.DepartSpeedMode.MAXIMUM, vehicle.departSpeedMode);
    }

    @Test
    public void checkNoDepartSpeedMode() {
        CVehicle vehicle = mapping.vehicles.get(3);
        assertNotEquals(vehicle, null);
        assertEquals(500, vehicle.departSpeed, 0);
        assertEquals(VehicleDeparture.DepartSpeedMode.MAXIMUM, vehicle.departSpeedMode);
    }
}
