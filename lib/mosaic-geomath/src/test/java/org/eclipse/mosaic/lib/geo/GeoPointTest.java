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

package org.eclipse.mosaic.lib.geo;

import org.junit.Test;

public class GeoPointTest {

    @Test(expected = IllegalArgumentException.class)
    public void invalidLatitude() {
        GeoPoint.latLon(-91, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidLongitude() {
        GeoPoint.latLon(-182, 0, 0);
    }

    @Test
    public void validCoordinates() {
        GeoPoint.latLon(52, 13, 0);
    }

}
