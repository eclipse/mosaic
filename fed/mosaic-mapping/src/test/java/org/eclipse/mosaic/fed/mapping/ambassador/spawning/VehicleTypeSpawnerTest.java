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
 */

package org.eclipse.mosaic.fed.mapping.ambassador.spawning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.mosaic.fed.mapping.config.CPrototype;
import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;

import org.junit.Test;

public class VehicleTypeSpawnerTest {

    @Test
    public void fillInPrototype() {
        //SETUP
        CPrototype a = new CPrototype();
        a.name = "Car";
        a.vehicleClass = VehicleClass.AutomatedVehicle;
        a.maxSpeed = 100d;

        VehicleTypeSpawner vehicleType = new VehicleTypeSpawner(a);

        //PRE-ASSERT
        assertEquals("Car", vehicleType.getPrototype());
        assertEquals(VehicleClass.AutomatedVehicle, vehicleType.getVehicleClass());
        assertEquals(100d, vehicleType.convertType().getMaxSpeed(), 0.1d);
        assertNull(vehicleType.getGroup());


        CPrototype b = new CPrototype();
        b.name = "Car";
        b.vehicleClass = VehicleClass.Car;
        b.tau = 0.8;

        //RUN
        vehicleType.fillInPrototype(b);

        //ASSERT
        assertEquals("Car", vehicleType.getPrototype());
        assertEquals(VehicleClass.AutomatedVehicle, vehicleType.getVehicleClass());
        assertEquals(100d, vehicleType.convertType().getMaxSpeed(), 0.1d);
        assertEquals(0.8d, vehicleType.convertType().getTau(), 0.1d);
        assertNull(vehicleType.getGroup());

    }

    @Test
    public void deviateWithBounds() {
        RandomNumberGenerator randomNumberGenerator = new DefaultRandomNumberGenerator(98891723L);

        CPrototype a = new CPrototype();
        a.name = "Car";
        a.vehicleClass = VehicleClass.AutomatedVehicle;
        a.tau = 1.5;

        VehicleTypeSpawner vehicleType = new VehicleTypeSpawner(a);

        double mean = 1.5;
        double sigma = 0.2;
        int totalBelow1Sigma = 0;
        int totalBelow2Sigma = 0;
        int totalBelow3Sigma = 0;

        for (int i = 0; i < 100000; i++) {

            //RUN calculate next random value
            double randomValue = vehicleType.deviateWithBounds(randomNumberGenerator, mean, sigma);

            if (Math.abs(randomValue - mean) < sigma - 0.0001) {
                totalBelow1Sigma++;
            }
            if (Math.abs(randomValue - mean) < (2 * sigma - 0.0001)) {
                totalBelow2Sigma++;
            }
            if (Math.abs(randomValue - mean) < 3 * sigma - 0.0001) {
                totalBelow3Sigma++;
            }
        }

        assertEquals(0.683, totalBelow1Sigma / 100000d, 0.001);
        assertEquals(0.954, totalBelow2Sigma / 100000d, 0.001);
        assertEquals(1.0, totalBelow3Sigma / 100000d, 0.001);
    }


}