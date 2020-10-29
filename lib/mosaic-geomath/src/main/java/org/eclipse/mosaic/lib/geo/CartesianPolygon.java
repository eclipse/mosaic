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

import org.eclipse.mosaic.lib.math.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CartesianPolygon implements Polygon<CartesianPoint>, CartesianArea {

    private static final long serialVersionUID = 1L;

    private final List<CartesianPoint> vertices;
    private final CartesianRectangle boundingBox;

    public CartesianPolygon(CartesianPoint... coordinates) {
        this(Arrays.asList(coordinates));
    }

    /**
     * Construct a new {@link CartesianPolygon}.
     *
     * @param coordinates A list of lists of {@link CartesianPoint}s
     */
    public CartesianPolygon(List<CartesianPoint> coordinates) {
        final List<CartesianPoint> verticesTmp = new ArrayList<>(coordinates);
        if (!verticesTmp.get(0).equals(verticesTmp.get(verticesTmp.size() - 1))) {
            verticesTmp.add(verticesTmp.get(0));
        }

        if (verticesTmp.size() < 3) {
            throw new IllegalArgumentException("The polygon must consists of two different vertices at least.");
        }

        boundingBox = calcBoundingBox(verticesTmp);
        vertices = Collections.unmodifiableList(verticesTmp);
    }

    private CartesianRectangle calcBoundingBox(List<CartesianPoint> vertices) {
        double maxY = -Long.MAX_VALUE;
        double minY = Long.MAX_VALUE;
        double maxX = -Long.MAX_VALUE;
        double minX = Long.MAX_VALUE;
        for (CartesianPoint point : vertices) {
            maxY = Math.max(maxY, point.getY());
            minY = Math.min(minY, point.getY());
            maxX = Math.max(maxX, point.getX());
            minX = Math.min(minX, point.getX());
        }
        return new CartesianRectangle(CartesianPoint.xy(minX, maxY), CartesianPoint.xy(maxX, minY));
    }

    public List<CartesianPoint> getVertices() {
        return vertices;
    }

    @Override
    public Bounds<CartesianPoint> getBounds() {
        return boundingBox;
    }

    @Override
    public boolean contains(CartesianPoint point) {
        float[] verticesXValues = new float[vertices.size()];
        float[] verticesYValues = new float[vertices.size()];

        for (int i = 0; i < vertices.size(); i++) {
            CartesianPoint geoPoint = vertices.get(i);
            verticesXValues[i] = (float) geoPoint.getX();
            verticesYValues[i] = (float) geoPoint.getY();
        }

        return MathUtils.pnpoly(vertices.size(), verticesXValues, verticesYValues, (float) point.getX(), (float) point.getY());
    }


    public GeoPolygon toGeo() {
        return new GeoPolygon(
                getVertices().stream().map(CartesianPoint::toGeo).collect(Collectors.toList())
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
        CartesianPolygon other = (CartesianPolygon) o;
        return this.getVertices().equals(other.getVertices());
    }

    @Override
    public int hashCode() {
        long longHash = 898912L;
        for (CartesianPoint corner : getVertices()) {
            longHash = 31 * longHash + corner.hashCode();
        }
        return (int) (longHash ^ (longHash >>> 32));
    }

}
