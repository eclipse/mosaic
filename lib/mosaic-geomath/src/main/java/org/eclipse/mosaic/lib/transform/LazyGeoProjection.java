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

import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.MutableCartesianPoint;
import org.eclipse.mosaic.lib.geo.MutableGeoPoint;
import org.eclipse.mosaic.lib.geo.MutableUtmPoint;
import org.eclipse.mosaic.lib.geo.UtmPoint;
import org.eclipse.mosaic.lib.math.Vector3d;

public class LazyGeoProjection extends GeoProjection {

    private final ProjectionFactory projectionFactory;
    private GeoProjection inst = null;

    public LazyGeoProjection() {
        this(new Wgs84ProjectionFactory());
    }

    public LazyGeoProjection(ProjectionFactory projectionFactory) {
        this.projectionFactory = projectionFactory;
    }

    private GeoProjection getInst(GeoPoint pt) {
        if (inst == null) {
            inst = projectionFactory.initializeFromGeo(pt);
        }
        return inst;
    }

    private GeoProjection getInst(UtmPoint pt) {
        if (inst == null) {
            inst = projectionFactory.initializeFromUtm(pt);
        }
        return inst;
    }

    private GeoProjection getInst(CartesianPoint pt) {
        if (inst == null) {
            inst = projectionFactory.initializeFromCartesian(pt);
        }
        return inst;
    }

    private GeoProjection getInst(Vector3d pt) {
        if (inst == null) {
            inst = projectionFactory.initializeFromVector3d(pt);
        }
        return inst;
    }

    @Override
    public MutableUtmPoint geographicToUtm(GeoPoint geoPoint, MutableUtmPoint mutableUtmPoint) {
        return getInst(geoPoint).geographicToUtm(geoPoint, mutableUtmPoint);
    }

    @Override
    public MutableGeoPoint utmToGeographic(UtmPoint utmPoint, MutableGeoPoint mutableGeoPoint) {
        return getInst(utmPoint).utmToGeographic(utmPoint, mutableGeoPoint);
    }

    @Override
    public MutableCartesianPoint geographicToCartesian(GeoPoint geoPoint, MutableCartesianPoint mutableCartesianPoint) {
        return getInst(geoPoint).geographicToCartesian(geoPoint, mutableCartesianPoint);
    }

    @Override
    public MutableGeoPoint cartesianToGeographic(CartesianPoint cartesianPoint, MutableGeoPoint mutableGeoPoint) {
        return getInst(cartesianPoint).cartesianToGeographic(cartesianPoint, mutableGeoPoint);
    }

    @Override
    public Vector3d utmToVector(UtmPoint utmPoint, Vector3d vector3d) {
        return getInst(utmPoint).utmToVector(utmPoint, vector3d);
    }

    @Override
    public MutableUtmPoint vectorToUtm(Vector3d vector3d, MutableUtmPoint mutableUtmPoint) {
        return getInst(vector3d).vectorToUtm(vector3d, mutableUtmPoint);
    }

    @Override
    public Vector3d geographicToVector(GeoPoint geoPoint, Vector3d vector3d) {
        return getInst(geoPoint).geographicToVector(geoPoint, vector3d);
    }

    @Override
    public MutableGeoPoint vectorToGeographic(Vector3d vector3d, MutableGeoPoint mutableGeoPoint) {
        return getInst(vector3d).vectorToGeographic(vector3d, mutableGeoPoint);
    }

    public interface ProjectionFactory {
        GeoProjection initializeFromGeo(GeoPoint origin);

        GeoProjection initializeFromUtm(UtmPoint origin);

        GeoProjection initializeFromCartesian(CartesianPoint origin);

        GeoProjection initializeFromVector3d(Vector3d origin);
    }

    private static class Wgs84ProjectionFactory implements ProjectionFactory {
        @Override
        public GeoProjection initializeFromGeo(GeoPoint origin) {
            return new Wgs84Projection(origin);
        }

        @Override
        public GeoProjection initializeFromUtm(UtmPoint origin) {
            return new Wgs84Projection(origin);
        }

        @Override
        public GeoProjection initializeFromCartesian(CartesianPoint origin) {
            return failInitialization();
        }

        @Override
        public GeoProjection initializeFromVector3d(Vector3d origin) {
            return failInitialization();
        }

        private GeoProjection failInitialization() {
            throw new IllegalStateException("Cannot initialize LazyGeoProjection from a local coordinate! "
                    + "LazyGeoProjection was used to translate a local coordinate to global coordinate before "
                    + "a global origin was set.");
        }
    }

}
