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
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.math.VectorUtils;
import org.eclipse.mosaic.lib.spatial.KdTree;
import org.eclipse.mosaic.lib.spatial.SpatialItemAdapter;
import org.eclipse.mosaic.lib.spatial.SpatialTreeTraverser;

import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
     * Searches for the closest edge to the geo location.
     *
     * @return Closest edge to the given location.
     */
    public List<Edge> findClosestEdge(GeoPoint location) {
        synchronized (edgeSearch) {
            Vector3d locationVector = location.toVector3d();
            edgeSearch.setup(locationVector, K_EDGES);
            edgeSearch.traverse(edgeIndex);

            List<EdgeWrapper> result = edgeSearch.getKNearest();
            if (result == null || result.isEmpty()) {
                return null;
            }

            EdgeWrapper edgeWrapper0 = result.get(0);
            EdgeWrapper edgeWrapper1 = result.get(1);
            Connection connection0 = edgeWrapper0.edge.getConnection();
            Connection connection1 = edgeWrapper1.edge.getConnection();
            // check if roads are adjacent
            if (connection0.getFrom() == connection1.getTo() && connection0.getTo() == connection1.getFrom()
                    && connection0.getNodes().size() == connection1.getNodes().size()) {
                Vector3d origin0 = connection0.getFrom().getPosition().toVector3d();
                Vector3d direction0 = connection0.getTo().getPosition().toVector3d().subtract(origin0, new Vector3d());
                Vector3d origin1 = connection1.getFrom().getPosition().toVector3d();
                Vector3d direction1 = connection1.getTo().getPosition().toVector3d().subtract(origin1, new Vector3d());
                if (!VectorUtils.isLeftOfLine(locationVector, origin0, direction0)) {
                    // if location is right of first connection, return first one
                    return Lists.newArrayList(edgeWrapper0.edge);
                } else if (!VectorUtils.isLeftOfLine(locationVector, origin1, direction1)) {
                    // if location is right of second connection, return second one
                    return Lists.newArrayList(edgeWrapper1.edge);
                } else {
                    // TODO: this should probably be the first if-clause and the dot product should be checked with a fuzzy-equals to zero
                    return result.stream().map(wrapper -> wrapper.edge).collect(Collectors.toList());
                }
            } else {
                return Lists.newArrayList(edgeWrapper0.edge);
            }
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
