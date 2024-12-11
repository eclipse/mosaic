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

package org.eclipse.mosaic.rti;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UNITSTest {

    @Test
    public void testUnitsConstants() {
        double delta = 1e-10;

        assertEquals(1, UNITS.METER, delta);
        assertEquals(1000, UNITS.KILOMETER, delta);
        assertEquals(1609.344, UNITS.MILE, delta);

        assertEquals(13.88, 50 * UNITS.KMH, 0.01); // Kilometers per hour -> Meters per second
        assertEquals(15.65, 35 * UNITS.MPH, 0.01); // Miles per hour -> Meters per second
        assertEquals(48.28, 30 * UNITS.MPH / UNITS.KMH, 0.01); // Miles per hour -> Kilometers per hour
    }

}
