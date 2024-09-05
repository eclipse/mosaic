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

package org.eclipse.mosaic.lib.database.spatial;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.math.MathUtils;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.math.VectorUtils;
import org.eclipse.mosaic.lib.spatial.KdTree;
import org.eclipse.mosaic.lib.spatial.SpatialItemAdapter;
import org.eclipse.mosaic.lib.spatial.SpatialTreeTraverser;

import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.util.ArrayList;
import java.util.List;

/**
 * Edge Finder searches for the closest edge to a specified geo location.
 */
public class EdgeFinder {

    /**
     * In order to handle cases were adjacent edges use the same FROM- and TO-Nodes,
     * we return the two nearest edges, which allows more concrete handling in later applications.
     */
    private static final int K_EDGES = 2;

    private final KdTree<EdgeWrapper> edgeIndex;
    private final SpatialTreeTraverser.KNearest<EdgeWrapper> edgeSearch;

    /**
     * Constructs a new edgeFinder object with the specified database.
     *
     * @param database Database which contains all connections.
     */
    public EdgeFinder(Database database) {
        List<EdgeWrapper> items = new ArrayList<>();

        for (Connection con : database.getConnections()) {
            for (int i = 0; i < con.getNodes().size() - 1; i++) {
                Node from = con.getNodes().get(i);
                Node to = con.getNodes().get(i + 1);
                items.add(new EdgeWrapper(new Edge(con, from, to)));
            }
        }
        edgeIndex = new KdTree<>(new SpatialItemAdapter.EdgeAdapter<>(), items);
        edgeSearch = new org.eclipse.mosaic.lib.database.spatial.Edge.KNearest<>();
    }

    /**
     * Searches for the closest edge given a location and a heading.
     * If two adjacent edges overlap, the heading will be used as a similarity measure.
     *
     * @param location the location to find the closest edge to
     * @param heading  used as a measure of similarity
     * @return the closest edge to the given location considering the heading
     */
    public Edge findClosestEdge(GeoPoint location, double heading) {
        List<EdgeWrapper> result = findKNearestEdgeWrappers(location, K_EDGES);
        if (result == null || result.isEmpty()) {
            return null;
        }
        if (result.size() == 1) {
            return result.get(0).edge;
        }
        Edge bestMatch = null;
        for (EdgeWrapper contestant : result) {
            if (bestMatch == null
                    || MathUtils.angleDif(getHeadingOfEdge(bestMatch), heading)
                    > MathUtils.angleDif(getHeadingOfEdge(contestant.edge), heading)) {
                bestMatch = contestant.edge;
            } else {
                getHeadingOfEdge(bestMatch);
            }
        }
        return bestMatch;
    }

    private double getHeadingOfEdge(Edge bestMatch) {
        return VectorUtils.getHeadingFromDirection(bestMatch.getNextNode().getPosition().toVector3d()
                .subtract(bestMatch.getPreviousNode().getPosition().toVector3d(), new Vector3d())
        );
    }

    /**
     * Searches for the two closest edges to the geo location.
     * The number of searched edges can be configured using {@link #K_EDGES}.
     *
     * @param location the location to find the closest edge to
     * @return The two closest edges to the given location.
     */
    public List<Edge> findClosestEdges(GeoPoint location) {
        List<EdgeWrapper> result = findKNearestEdgeWrappers(location, K_EDGES);
        if (result == null || result.isEmpty()) {
            return null;
        }
        if (result.size() == 1) {
            return Lists.newArrayList(result.get(0).edge);
        }
        Edge edge0 = result.get(0).edge;
        Edge edge1 = result.get(1).edge;
        // check if roads are adjacent
        if (edge0.getPreviousNode() == edge1.getNextNode() && edge0.getNextNode() == edge1.getPreviousNode()
                && edge0.getConnection().getNodes().size() == edge1.getConnection().getNodes().size()) {
            final Vector3d locationVector = location.toVector3d();
            final Vector3d origin0 = edge0.getPreviousNode().getPosition().toVector3d();
            final Vector3d direction0 = edge0.getNextNode().getPosition().toVector3d().subtract(origin0, new Vector3d());
            final Vector3d origin1 = edge1.getPreviousNode().getPosition().toVector3d();
            final Vector3d direction1 = edge1.getNextNode().getPosition().toVector3d().subtract(origin1, new Vector3d());
            List<Edge> resultEdges = new ArrayList<>();
            if (!VectorUtils.isLeftOfLine(locationVector, origin0, direction0)) {
                // if location is right of first connection, return first one
                resultEdges.add(edge0);
            }
            if (!VectorUtils.isLeftOfLine(locationVector, origin1, direction1)) {
                // if location is right of second connection, return second one
                resultEdges.add(edge1);
            }
            return resultEdges;
        } else {
            return Lists.newArrayList(edge0);
        }

    }

    private List<EdgeWrapper> findKNearestEdgeWrappers(GeoPoint location, int k) {
        synchronized (edgeSearch) {
            Vector3d locationVector = location.toVector3d();
            edgeSearch.setup(locationVector, K_EDGES);
            edgeSearch.traverse(edgeIndex);

            List<EdgeWrapper> result = edgeSearch.getKNearest();
            if (result == null || result.isEmpty()) {
                return null;
            }
            if (result.size() == 1) {
                return Lists.newArrayList(result.get(0));
            }
            return result;
        }
    }

    @SuppressWarnings(value = "SE_BAD_FIELD", justification = "The EdgeWrapper won't be serialized.")
    private final static class EdgeWrapper extends org.eclipse.mosaic.lib.spatial.Edge<Vector3d> {

        private final Edge edge;

        public EdgeWrapper(Edge edge) {
            super(
                    edge.getPreviousNode().getPosition().toVector3d(),
                    edge.getNextNode().getPosition().toVector3d()
            );
            this.edge = edge;
        }

        public Edge getEdge() {
            return edge;
        }
    }

}
