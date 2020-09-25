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

package org.eclipse.mosaic.lib.database.road;

import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.lib.geo.GeoPoint;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RoundaboutTest {

    @Test
    public void testSimpleGetter() {
        // adding node reference
        List<Node> nodeRefs = new ArrayList<>();
        nodeRefs.add(new Node("0", GeoPoint.lonLat(0, 0)));
        nodeRefs.add(new Node("1", GeoPoint.lonLat(0, 0)));
        nodeRefs.add(new Node("2", GeoPoint.lonLat(0, 0)));
        nodeRefs.add(new Node("3", GeoPoint.lonLat(0, 0)));
        // constructor & getter
        Roundabout r1 = new Roundabout("roundabout_0", nodeRefs);
        assertEquals("Wrong roundabout id", "roundabout_0", r1.getId());

        assertEquals("Wrong node reference lists size", 4, r1.getNodes().size());
        Roundabout r2 = new Roundabout("roundabout_1", nodeRefs);
        assertEquals("Wrong node reference lists size", 4, r2.getNodes().size());
    }
}
