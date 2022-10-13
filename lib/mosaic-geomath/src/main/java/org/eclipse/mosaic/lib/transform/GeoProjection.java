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

/**
 * Provides methods to transform geographic coordinates from and to cartesian coordinates.
 */
public abstract class GeoProjection {

    private static volatile GeoProjection instance;

    public static GeoProjection getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Coordinate Transformation has not been initialized.");
        }
        return instance;
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    public static void initialize(final GeoProjection geoProjection) {
        if (instance != null) {
            throw new IllegalStateException("Coordinate Transformation has already been initialized.");
        }
        GeoProjection.instance = geoProjection;
    }

    private volatile GeoCalculator geoCalculator = null;

    public GeoProjection setGeoCalculator(GeoCalculator geoCalculator) {
        if (this.geoCalculator != null) {
            throw new IllegalStateException("GeoCalculator has already been defined.");
        }
        this.geoCalculator = geoCalculator;
        return this;
    }

    public final GeoCalculator getGeoCalculator() {
        if (geoCalculator == null) {
            synchronized (this) {
                if (geoCalculator == null) {
                    setGeoCalculator(new SimpleGeoCalculator());
                }
            }
        }
        return geoCalculator;
    }

    /**
     * Converts a geographic WGS84 coordinate depicted by a {@link GeoPoint} into UTM coordinates.
     *
     * @param geographic geographic WGS84 coordinates
     * @param result     the {@link MutableUtmPoint} to which the result is written to
     * @return the result object with converted UTM coordinates
     */
    public abstract MutableUtmPoint geographicToUtm(final GeoPoint geographic, final MutableUtmPoint result);

    /**
     * Converts a geographic WGS84 coordinate depicted by a {@link GeoPoint} into UTM coordinates.
     *
     * @param geographic geographic WGS84 coordinates
     * @return the result object with converted UTM coordinates
     */
    public UtmPoint geographicToUtm(final GeoPoint geographic) {
        return geographicToUtm(geographic, new MutableUtmPoint());
    }

    /**
     * Converts a cartesian world coordinate depicted by a {@link UtmPoint} to geographic WGS84 coordinates.
     *
     * @param utm    cartesian UTM coordinates
     * @param result the {@link MutableGeoPoint} to which the result is written to
     * @return the result object with converted WGS84 coordinates
     */
    public abstract MutableGeoPoint utmToGeographic(final UtmPoint utm, final MutableGeoPoint result);

    /**
     * Converts a cartesian world coordinate depicted by a {@link UtmPoint} to geographic WGS84 coordinates.
     *
     * @param utm cartesian UTM coordinates
     * @return the result object with converted WGS84 coordinates
     */
    public GeoPoint utmToGeographic(final UtmPoint utm) {
        return utmToGeographic(utm, new MutableGeoPoint(0d, 0d, 0d));
    }

    /**
     * Converts a geographic WGS84 coordinate depicted by a {@link GeoPoint} into local coordinates coordinates.
     *
     * @param geographic geographic WGS84 coordinates
     * @param result     the {@link MutableUtmPoint} to which the result is written to
     * @return the result object with converted local coordinates
     */
    public abstract MutableCartesianPoint geographicToCartesian(final GeoPoint geographic, final MutableCartesianPoint result);

    /**
     * Converts a geographic WGS84 coordinate depicted by a {@link GeoPoint} into local coordinates coordinates.
     *
     * @param geographic geographic WGS84 coordinates
     * @return the result object with converted local coordinates
     */
    public CartesianPoint geographicToCartesian(final GeoPoint geographic) {
        return geographicToCartesian(geographic, new MutableCartesianPoint(0, 0, 0));
    }

    /**
     * Converts a local coordinate depicted by a {@link CartesianPoint} to geographic WGS84 coordinates.
     *
     * @param cartesian the local coordinate
     * @param result    the {@link MutableGeoPoint} to which the result is written to
     * @return the result object with converted WGS84 coordinates
     */
    public abstract MutableGeoPoint cartesianToGeographic(final CartesianPoint cartesian, final MutableGeoPoint result);

    /**
     * Converts a local coordinate depicted by a {@link CartesianPoint} to geographic WGS84 coordinates.
     *
     * @param cartesian the local coordinate
     * @return the result object with converted WGS84 coordinates
     */
    public GeoPoint cartesianToGeographic(final CartesianPoint cartesian) {
        return cartesianToGeographic(cartesian, new MutableGeoPoint(0d, 0d, 0d));
    }

    /**
     * Converts a cartesian world coordinate depicted by a {@link UtmPoint} into local coordinates coordinates.
     *
     * @param utm    the cartesian world UTM coordinate.
     * @param result the {@link MutableUtmPoint} to which the result is written to
     * @return the result object with converted local coordinates
     */
    public abstract Vector3d utmToVector(final UtmPoint utm, final Vector3d result);

    /**
     * Converts a cartesian world coordinate depicted by a {@link UtmPoint} into local coordinates coordinates.
     *
     * @param utm the cartesian world UTM coordinate.
     * @return the result object with converted local coordinates
     */
    public Vector3d utmToVector(final UtmPoint utm) {
        return utmToVector(utm, new Vector3d());
    }

    /**
     * Converts a local coordinate depicted by a {@link Vector3d} into UTM coordinates.
     *
     * @param vector the local coordinate
     * @param result the {@link MutableUtmPoint} to which the result is written to
     * @return the result object with converted UTM coordinates
     */
    public abstract MutableUtmPoint vectorToUtm(final Vector3d vector, final MutableUtmPoint result);

    /**
     * Converts a local coordinate depicted by a {@link Vector3d} into UTM coordinates.
     *
     * @param vector the local coordinate
     * @return the result object with converted UTM coordinates
     */
    public UtmPoint vectorToUtm(final Vector3d vector) {
        return vectorToUtm(vector, new MutableUtmPoint());
    }

    /**
     * Converts a geographic WGS84 coordinate depicted by a {@link GeoPoint} into local coordinates coordinates.
     *
     * @param geographic geographic WGS84 coordinates
     * @param result     the {@link MutableUtmPoint} to which the result is written to
     * @return the result object with converted local coordinates
     */
    public abstract Vector3d geographicToVector(final GeoPoint geographic, final Vector3d result);

    /**
     * Converts a geographic WGS84 coordinate depicted by a {@link GeoPoint} into local coordinates coordinates.
     *
     * @param geographic geographic WGS84 coordinates
     * @return the result object with converted local coordinates
     */
    public Vector3d geographicToVector(final GeoPoint geographic) {
        return geographicToVector(geographic, new Vector3d());
    }

    /**
     * Converts a local coordinate depicted by a {@link Vector3d} to geographic WGS84 coordinates.
     *
     * @param vector the local coordinate
     * @param result the {@link MutableGeoPoint} to which the result is written to
     * @return the result object with converted WGS84 coordinates
     */
    public abstract MutableGeoPoint vectorToGeographic(final Vector3d vector, final MutableGeoPoint result);

    /**
     * Converts a local coordinate depicted by a {@link Vector3d} to geographic WGS84 coordinates.
     *
     * @param vector the local coordinate
     * @return the result object with converted WGS84 coordinates
     */
    public GeoPoint vectorToGeographic(final Vector3d vector) {
        return vectorToGeographic(vector, new MutableGeoPoint(0d, 0d, 0d));
    }


}

