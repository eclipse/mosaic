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

        return pnpoly(vertices.size(), verticesXValues, verticesYValues, (float) point.getX(), (float) point.getY());
    }

    /** Point Inclusion in Polygon Test.
     * Defines if a points lies within a polygon
     * @param nvert Number of vertices in the polygon. Whether to repeat the first vertex at the end is discussed below.
     * @param vertx Array containing the x-coordinates of the polygon's vertices.
     * @param verty Array containing the y-coordinates of the polygon's vertices.
     * @param testx X-coordinate of the test point.
     * @param testy Y-coordinate of the test point.
     * @return true if point lies within the polygon, false otherwise
     */
    private boolean pnpoly(int nvert, float[] vertx, float[] verty, float testx, float testy) {
        float minXValue = getMinValue(vertx);
        float minYValue = getMinValue(verty);

        boolean c = false;
        for (int i = 0, j = nvert - 1; i < nvert; j = i++) {
            boolean conditionXAxis;
            boolean conditionYAxis;

            if (testx <= minXValue) {
                conditionXAxis = testx < (vertx[j] - vertx[i]) * (testy - verty[i]) / (verty[j] - verty[i]) + vertx[i];
            } else {
                conditionXAxis = testx <= (vertx[j] - vertx[i]) * (testy - verty[i]) / (verty[j] - verty[i]) + vertx[i];
            }

            if (testy <= minYValue) {
                conditionYAxis = verty[i] > testy != verty[j] > testy;
            } else {
                conditionYAxis = verty[i] >= testy != verty[j] >= testy;
            }

            if (conditionYAxis && conditionXAxis) {
                c = !c;
            }
        }
        return c;
    }

    private float getMinValue(float[] numbers) {
        float minValue = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] < minValue) {
                minValue = numbers[i];
            }
        }
        return minValue;
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
