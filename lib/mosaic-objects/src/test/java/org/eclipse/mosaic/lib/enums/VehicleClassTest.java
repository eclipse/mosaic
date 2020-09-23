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

package org.eclipse.mosaic.lib.enums;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class VehicleClassTest {

    @Test
    public void fromId() {
        for (VehicleClass vehicleClass : VehicleClass.values()) {
            assertEquals(vehicleClass, VehicleClass.fromId(vehicleClass.id));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromId_nonExisting() {
        VehicleClass.fromId(100);
    }

}