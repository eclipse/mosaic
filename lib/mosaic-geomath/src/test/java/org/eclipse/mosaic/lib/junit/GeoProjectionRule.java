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

package org.eclipse.mosaic.lib.junit;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.UtmPoint;
import org.eclipse.mosaic.lib.transform.GeoProjection;
import org.eclipse.mosaic.lib.transform.Wgs84Projection;

import org.junit.rules.ExternalResource;

import java.lang.reflect.Field;

public class GeoProjectionRule extends ExternalResource {

    private GeoPoint geoOrigin;
    private UtmPoint utmOrigin;

    public GeoProjectionRule(GeoPoint geoOrigin) {
        this.geoOrigin = geoOrigin;
    }

    public GeoProjectionRule(UtmPoint utmOrigin) {
        this.utmOrigin = utmOrigin;
    }

    @Override
    protected void before() throws Throwable {
        if (utmOrigin != null) {
            GeoProjection.initialize(new Wgs84Projection(utmOrigin).failIfOutsideWorld());
        } else {
            GeoProjection.initialize(new Wgs84Projection(geoOrigin).failIfOutsideWorld());
        }
    }

    @Override
    protected void after() {
        try {
            Field singletonField = GeoProjection.class.getDeclaredField("instance");
            singletonField.setAccessible(true);
            singletonField.set(GeoProjection.class, null);
        } catch (Exception e) {
            throw new AssertionError("Could not reset GeoProjection singleton", e);
        }
    }
}