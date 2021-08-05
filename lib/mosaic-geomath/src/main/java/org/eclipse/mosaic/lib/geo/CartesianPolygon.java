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

    private static class ArrayIndexComparator implements Comparator<Integer>
    {
        private final List<CartesianPoint> verticeList;

        public ArrayIndexComparator(List<CartesianPoint> verticeList)
        {
            this.verticeList = verticeList;
        }

        public List<Integer> createIndexArray()
        {
            List<Integer> indexes = new ArrayList<>();
            for (int i = 0; i < verticeList.size(); i++)
            {
                indexes.add(i);
            }
            return indexes;
        }

        @Override
        public int compare(Integer index1, Integer index2)
        {
            return Double.compare(verticeList.get(index1).getX(), verticeList.get(index2).getX());
        }
    }
    public boolean sweepLineIntersection(List<Edge<Vector3d>> edgeList, Edge<Vector3d> edge) {
        for (Edge<Vector3d> e : edgeList) {
            if (edge.isIntersectingEdge(e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if there is an intersection with another polygon.
     * The intersection detection is implemented with a sweep line algorithm.
     *
     * @param polygon The other polygon
     * @return true if the polygons intersect
     */
    public boolean isIntersectingPolygon(CartesianPolygon polygon) {
        // Test if bounding boxes intersect
        CartesianRectangle rectA = polygon.boundingBox;
        CartesianRectangle rectB = calcBoundingBox(getVertices());
        if ((rectA.getA().getX() > rectB.getB().getX() || rectB.getB().getX() < rectB.getA().getX()
                || rectA.getA().getY() < rectB.getB().getY() || rectA.getB().getY() > rectA.getA().getY())){
            return false;
        }
        // Test if any polygon is completely contained in the other polygon
        if (contains(polygon.getVertices().get(0))) {
            return true;
        }
        if (polygon.contains(getVertices().get(0))) {
            return true;
        }

        // Test if any edges intersect (sweep-line algorithm)
        ArrayIndexComparator comparatorP1 = new ArrayIndexComparator(getVertices());
        List<Integer> indexesP1 = comparatorP1.createIndexArray();
        ArrayIndexComparator comparatorP2 = new ArrayIndexComparator(getVertices());
        List<Integer> indexesP2 = comparatorP2.createIndexArray();
        Iterator<Integer> iteratorP1 = indexesP1.iterator();
        Iterator<Integer> iteratorP2 = indexesP2.iterator();

        int verticeIndexP1;
        int verticeIndexP2;
        int previousIndex;
        int nextIndex;

        Vector3d vertice1;
        Vector3d vertice2;

        List<Edge<Vector3d>> sweepLineStatusP1 = new ArrayList<>();
        List<Edge<Vector3d>> sweepLineStatusP2 = new ArrayList<>();

        verticeIndexP1 = iteratorP1.next();
        verticeIndexP2 = iteratorP2.next();

        boolean hasNextP1 = true;
        boolean hasNextP2 = true;

        while (hasNextP1 || hasNextP2) {
            if (vertices.get(verticeIndexP1).getX() < polygon.getVertices().get(verticeIndexP2).getX() && hasNextP1) {
                // update sweepLineStatusP1
                vertice1 = getVertices().get(verticeIndexP1).toVector3d();
                if (verticeIndexP1 == 0){
                    previousIndex = getVertices().size() - 1;
                } else {
                    previousIndex = verticeIndexP1 - 1;
                }
                vertice2 = getVertices().get(previousIndex).toVector3d();
                if(vertice2.x >= vertice1.x) {
                    // If vertice is at the riht side of the sweep line add edge to sweepLineStatusP1
                    sweepLineStatusP1.add(new Edge<>(vertice1, vertice2));
                    if (sweepLineIntersection(sweepLineStatusP2, new Edge<>(vertice1, vertice2))) {
                        return true;
                    }
                } else {
                    // If vertice is at the left side of the sweep line remove edge from sweepLineStatusP1
                    sweepLineStatusP1.remove(new Edge<>(vertice2, vertice1));
                }
                if (verticeIndexP1 == vertices.size() - 1){
                    nextIndex = 0;
                } else {
                    nextIndex = verticeIndexP1 + 1;
                }
                vertice2 = vertices.get(nextIndex).toVector3d();
                if(vertice2.x >= vertice1.x) {
                    // If vertice is at the right side of the sweep line add edge to sweepLineStatusP1
                    sweepLineStatusP1.add(new Edge<>(vertice1, vertice2));
                    if (sweepLineIntersection(sweepLineStatusP2, new Edge<>(vertice1, vertice2))) {
                        return true;
                    }
                } else {
                    // If vertice is at the left side of the sweep line remove edge from sweepLineStatusP1
                    sweepLineStatusP1.remove(new Edge<>(vertice2, vertice1));
                }
                // Get next vertex in x direction if possible
                if (iteratorP1.hasNext()){
                    verticeIndexP1 = iteratorP1.next();
                } else {
                    hasNextP1 = false;
                }
            } else if (hasNextP2) {
                // Update sweepLineStatusP1
                vertice1 = polygon.getVertices().get(verticeIndexP2).toVector3d();
                // Check edge between current vertice and previous vertice
                if (verticeIndexP2 == 0){
                    previousIndex = polygon.getVertices().size() - 1;
                } else {
                    previousIndex = verticeIndexP2 - 1;
                }
                vertice2 = polygon.getVertices().get(previousIndex).toVector3d();
                if(vertice2.x >= vertice1.x) {
                    // If vertice is at the right side of the sweep line add edge to sweepLineStatusP1
                    sweepLineStatusP2.add(new Edge<>(vertice1, vertice2));
                    if (sweepLineIntersection(sweepLineStatusP1, new Edge<>(vertice1, vertice2))) {
                        return true;
                    }
                } else {
                    // If vertice is at the left side of the sweep line remove edge from sweepLineStatusP1
                    sweepLineStatusP2.remove(new Edge<>(vertice2, vertice1));
                }
                // Check edge between current vertice and next vertice
                if (verticeIndexP2 == polygon.getVertices().size() - 1){
                    nextIndex = 0;
                } else {
                    nextIndex = verticeIndexP2 + 1;
                }
                vertice2 = polygon.getVertices().get(nextIndex).toVector3d();
                if(vertice2.x >= vertice1.x) {
                    // If vertice is at the right side of the sweep line add edge to sweepLineStatusP1
                    sweepLineStatusP2.add(new Edge<>(vertice1, vertice2));
                    if (sweepLineIntersection(sweepLineStatusP1, new Edge<>(vertice1, vertice2))) {
                        return true;
                    }
                } else {
                    // If vertice is at the left side of the sweep line remove edge from sweepLineStatusP1
                    sweepLineStatusP2.remove(new Edge<>(vertice2, vertice1));
                }
                // Get next vertex in x direction if possible
                if (iteratorP2.hasNext()){
                    verticeIndexP2 = iteratorP2.next();
                } else {
                    hasNextP2 = false;
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
