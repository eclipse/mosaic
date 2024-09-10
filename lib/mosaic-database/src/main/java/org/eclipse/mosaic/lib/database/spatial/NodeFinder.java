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
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.spatial.KdTree;
import org.eclipse.mosaic.lib.spatial.SpatialItemAdapter;
import org.eclipse.mosaic.lib.spatial.SpatialTreeTraverser;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.util.List;

/**
 * A spatial index which searches for the closest node to a specified geo location.
 */
public class NodeFinder {

    private final KdTree<NodeWrapper> nodeIndex;
    private final SpatialTreeTraverser.Nearest<NodeWrapper> nodeSearch;

    /**
     * Constructs a new edgeFinder object with the specified database.
     *
     * @param database Database which contains all nodes.
     */
    public NodeFinder(Database database) {
        List<NodeWrapper> items = database.getNodes().stream()
                .map(NodeWrapper::new).toList();

        nodeIndex = new KdTree<>(new SpatialItemAdapter.PointAdapter<>(), items);
        nodeSearch = new SpatialTreeTraverser.Nearest<>();
    }

    /**
     * Searches for the closest {@link Node} to the geo location.
     *
     * @return Closest {@link Node} to the given location.
     */
    public Node findClosestNode(GeoPoint location) {
        synchronized (nodeSearch) {
            nodeSearch.setup(location.toVector3d());
            nodeSearch.traverse(nodeIndex);

            NodeWrapper result = nodeSearch.getNearest();
            if (result == null) {
                return null;
            }
            return result.node;
        }
    }

    @SuppressWarnings(value = "SE_BAD_FIELD", justification = "The EdgeWrapper won't be serialized.")
    private final static class NodeWrapper extends Vector3d {

        private final Node node;

        private NodeWrapper(Node node) {
            super(node.getPosition().toVector3d());
            this.node = node;
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o)
                    && o instanceof NodeWrapper
                    && ((NodeWrapper) o).node == node;
        }

        @Override
        public int hashCode() {
            return super.hashCode() * 31 + node.hashCode();
        }
    }

}
