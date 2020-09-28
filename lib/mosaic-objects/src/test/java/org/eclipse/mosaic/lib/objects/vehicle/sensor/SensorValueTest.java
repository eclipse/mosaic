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

package org.eclipse.mosaic.lib.objects.vehicle.sensor;

import org.eclipse.mosaic.lib.objects.vehicle.sensor.SensorValue.SensorStatus;

import org.junit.Assert;
import org.junit.Test;

public class SensorValueTest {

    @Test
    public void sensorNotEquippedTest() throws Exception {
        SensorValue senVal = new SensorValue(-1);
        Assert.assertEquals(SensorStatus.NOT_EQUIPPED, senVal.status);
    }
    
    @Test
    public void noVehicleDetectedTest() throws Exception {
        SensorValue senVal = new SensorValue(-2);
        SensorValue senVal2 = new SensorValue(Double.POSITIVE_INFINITY);
        Assert.assertEquals(SensorStatus.NO_VEHICLE_DETECTED, senVal.status);
        Assert.assertEquals(SensorStatus.NO_VEHICLE_DETECTED, senVal2.status);
    }
    
    @Test
    public void sensorEquippedTest() throws Exception {
        SensorValue senVal = new SensorValue(123.456);
        Assert.assertEquals(SensorStatus.VEHICLE_DETECTED, senVal.status);
        Assert.assertEquals(123.456, senVal.distValue, 0);
    }
    
    @Test
    public void equalsTestIsEqual() throws Exception {
        SensorValue senVal = new SensorValue(135);
        SensorValue senVal2 = new SensorValue(135);
        Assert.assertEquals(true, senVal.equals(senVal2));
    }
    
    @Test
    public void equalsTestNotEqual() throws Exception {
        SensorValue senVal = new SensorValue(135);
        SensorValue senVal2 = new SensorValue(136);
        Assert.assertEquals(false, senVal.equals(senVal2));
    }

}
