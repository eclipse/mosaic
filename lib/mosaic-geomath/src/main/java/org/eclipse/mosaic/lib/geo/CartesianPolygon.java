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
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.spatial.Edge;

import java.util.*;
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

    /**
     *
     * @param edge1A
     * @param edge1B
     * @param edge2A
     * @param edge2B
     * @return
     */
    public boolean isIntersectingEdge(Vector3d edge1A, Vector3d edge1B, Vector3d edge2A, Vector3d edge2B) {
        Vector3d r = new Vector3d(edge1A.x - edge1B.x, 0, edge1A.z - edge1B.z);
        Vector3d s = new Vector3d(edge2A.x - edge2B.x, 0, edge2A.z - edge2B.z);
        Vector3d diff = edge2A.subtract(edge1A);
        double crossprod1 = r.x * s.y - r.y * s.x;
        double crossprod2 = diff.x * r.y - diff.y * r.x;

        if (crossprod1 == 0 && crossprod2 == 0) {
            // Vectors of edges are collinear, check for mutual line segment
            return edge1A.x >= edge2A.x && edge1A.x < edge2B.x || edge1B.x >= edge2A.x && edge1B.x < edge2B.x ||
                    edge2A.x >= edge1A.x && edge2A.x < edge1B.x || edge2B.x >= edge1A.x && edge2B.x < edge1B.x;
        } else if (crossprod1 != 0) {
            // Vectors of edges are neither parallel nor collinear, check for intersection
            double u = crossprod2 / crossprod1;
            double t = (diff.x * s.y - diff.y * s.x) / crossprod1;
            return (u >= 0 && u <= 1 && t >= 0 && t <= 1) || (u <= 0 && u >= -1 && t <= 0 && t >= -1);
        }
        return false;
    }


    /**
     * Returns true if there is a collision with another polygon.
     *
     * @param polygon The other polygon
     * @return true if the polygons collide
     */
    public boolean isIntersectingPolygon(CartesianPolygon polygon) {
        // Test if bounding boxes intersect
        CartesianRectangle rectA = polygon.boundingBox;
        CartesianRectangle rectB = this.boundingBox;
        if ((rectA.getA().getX() > rectB.getB().getX() || rectA.getB().getX() < rectB.getA().getX()
                || rectA.getA().getY() < rectB.getB().getY() || rectA.getB().getY() > rectA.getA().getY())){
            return false;
        }
        // Test if any polygon is completely contained within the other polygon
        if (contains(polygon.getVertices().get(0)) || polygon.contains(vertices.get(0))) {
            return true;
        }
        // Test if any edges of the polygons intersect
        for (int vIdx1 = 1; vIdx1 < vertices.size() ; vIdx1++) {
            Vector3d edgeP1A = vertices.get(vIdx1 - 1).toVector3d();
            Vector3d edgeP1B = vertices.get(vIdx1).toVector3d();
            for (int vIdx2 = 1; vIdx2 < polygon.getVertices().size() ; vIdx2++) {
                Vector3d edgeP2A = polygon.getVertices().get(vIdx2 - 1).toVector3d();
                Vector3d edgeP2B = polygon.getVertices().get(vIdx2).toVector3d();
                if (isIntersectingEdge(edgeP1A, edgeP1B, edgeP2A, edgeP2B)) {
                    return true;
                }
            }
        }
        return false;
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
