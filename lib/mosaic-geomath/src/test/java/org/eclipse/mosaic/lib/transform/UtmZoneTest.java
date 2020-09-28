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

package org.eclipse.mosaic.lib.transform;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.eclipse.mosaic.lib.geo.UtmZone;

import org.junit.Test;

public class UtmZoneTest {

    @Test
    public void zoneCache() {
        UtmZone zoneA = UtmZone.from("33n");
        UtmZone zoneB = UtmZone.from("33n");
        UtmZone zoneC = UtmZone.from(33, 'n');
        UtmZone zoneD = UtmZone.from("34n");

        assertSame(zoneA, zoneB);
        assertSame(zoneA, zoneC);
        assertNotSame(zoneA, zoneD);
    }

}