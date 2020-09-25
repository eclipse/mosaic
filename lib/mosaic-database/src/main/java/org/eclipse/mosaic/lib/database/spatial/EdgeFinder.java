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
import org.eclipse.mosaic.lib.database.route.Edge;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.spatial.KdTree;
import org.eclipse.mosaic.lib.spatial.SpatialItemAdapter;
import org.eclipse.mosaic.lib.spatial.SpatialTreeTraverser;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.util.ArrayList;
import java.util.List;

/**
 * Edge Finder searches for the closest edge to a specified geo location.
 */
public class EdgeFinder {

    private final KdTree<EdgeWrapper> edgeIndex;
    private final SpatialTreeTraverser.Nearest<EdgeWrapper> edgeSearch;

    /**
     * Constructs a new edgeFinder object with the specified database.
     *
     * @param database Database which contains all connections.
     */
    public EdgeFinder(Database database) {
        List<EdgeWrapper> items =  new ArrayList<>();

        for (Connection con : database.getConnections()) {
            for (int i = 0; i < con.getNodes().size() - 1; i++) {
                Node from = con.getNodes().get(i);
                Node to = con.getNodes().get(i + 1);

                String id;
                if (database.getImportOrigin().equals(Database.IMPORT_ORIGIN_SUMO)) {
                    id = con.getId();
                } else {
                    id = con.getId() + "_" + from.getId();
                }
                items.add(new EdgeWrapper(new Edge(con, id, from, to)));
            }
        }
        edgeIndex = new KdTree<>(new SpatialItemAdapter.EdgeAdapter<>(), items);
        edgeSearch = new SpatialTreeTraverser.Nearest<>();
    }

    /**
     * Searches for the closest edge to the geo location.
     *
     * @return Closest edge to the given location.
     */
    public Edge findClosestEdge(GeoPoint location) {
        synchronized (edgeSearch) {
            edgeSearch.setup(location.toVector3d());
            edgeSearch.traverse(edgeIndex);

            EdgeWrapper result = edgeSearch.getNearest();
            if (result == null) {
                return null;
            }
            return result.edge;
        }
    }

    @SuppressWarnings(value = "SE_BAD_FIELD", justification = "The EdgeWrapper won't be serialized.")
    private final static class EdgeWrapper extends org.eclipse.mosaic.lib.spatial.Edge<Vector3d> {

        private final Edge edge;

        public EdgeWrapper(Edge edge) {
            super(
                    edge.getFromNode().getPosition().toVector3d(),
                    edge.getToNode().getPosition().toVector3d()
            );
            this.edge = edge;
        }

        public Edge getEdge() {
            return edge;
        }
    }

}
