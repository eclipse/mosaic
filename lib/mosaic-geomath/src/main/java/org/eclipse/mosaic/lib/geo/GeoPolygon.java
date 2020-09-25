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

package org.eclipse.mosaic.lib.geo;

import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.spatial.Ray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GeoPolygon implements Polygon<GeoPoint>, GeoArea {

    private static final long serialVersionUID = 1L;

    private final List<GeoPoint> vertices;
    private transient GeoRectangle boundingBox;

    public GeoPolygon(GeoPoint... coordinates) {
        this(Arrays.asList(coordinates));
    }

    /**
     * Construct a new {@link GeoPolygon}.
     *
     * @param coordinates A list of lists of {@link GeoPoint}s
     */
    public GeoPolygon(List<GeoPoint> coordinates) {
        final List<GeoPoint> verticesTmp = new ArrayList<>(coordinates);
        if (!verticesTmp.get(0).equals(verticesTmp.get(verticesTmp.size() - 1))) {
            verticesTmp.add(verticesTmp.get(0));
        }

        if (verticesTmp.size() < 3) {
            throw new IllegalArgumentException("The polygon must consists of two different vertices at least.");
        }
        vertices = Collections.unmodifiableList(verticesTmp);
    }

    private GeoRectangle calcBoundingBox(List<GeoPoint> vertices) {
        double maxLat = -90;
        double minLat = 90;
        double maxLon = -180;
        double minLon = 180;
        for (GeoPoint point : vertices) {
            maxLat = Math.max(maxLat, point.getLatitude());
            minLat = Math.min(minLat, point.getLatitude());
            maxLon = Math.max(maxLon, point.getLongitude());
            minLon = Math.min(minLon, point.getLongitude());
        }
        return new GeoRectangle(GeoPoint.latLon(maxLat, minLon), GeoPoint.latLon(minLat, maxLon));
    }

    public List<GeoPoint> getVertices() {
        return vertices;
    }

    @Override
    public Bounds<GeoPoint> getBounds() {
        if (boundingBox == null) {
            boundingBox = calcBoundingBox(vertices);
        }
        return boundingBox;
    }

    @Override
    public boolean contains(GeoPoint point) {
        final Ray rayFromPoint = new Ray();
        point.toVector3d(rayFromPoint.origin);
        rayFromPoint.origin.set(point.getLongitude(), 0, -point.getLatitude());
        rayFromPoint.direction.set(1, 0, 0);

        int intersections = 0;
        final Vector3d edgeStart = new Vector3d();
        final Vector3d edgeEnd = new Vector3d();
        GeoPoint prev = null;
        for (GeoPoint curr : getVertices()) {
            if (prev != null) {
                edgeStart.set(prev.getLongitude(), 0, -prev.getLatitude());
                edgeEnd.set(curr.getLongitude(), 0, -curr.getLatitude());
                boolean intersect = rayFromPoint.intersectsLineSegmentXZ(edgeStart, edgeEnd);
                intersections += intersect ? 1 : 0;
            }
            prev = curr;
        }
        return intersections % 2 != 0;
    }

    public CartesianPolygon toCartesian() {
        return new CartesianPolygon(
                getVertices().stream().map(GeoPoint::toCartesian).collect(Collectors.toList())
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeoPolygon other = (GeoPolygon) o;
        return this.getVertices().equals(other.getVertices());
    }

    @Override
    public int hashCode() {
        long longHash = 898912L;
        for (GeoPoint corner : getVertices()) {
            longHash = 31 * longHash + corner.hashCode();
        }
        return (int) (longHash ^ (longHash >>> 32));
    }

}
