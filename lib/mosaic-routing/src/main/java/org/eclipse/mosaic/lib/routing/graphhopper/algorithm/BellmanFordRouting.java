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

package org.eclipse.mosaic.lib.routing.graphhopper.algorithm;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.routing.AbstractRoutingAlgorithm;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.PathExtractor;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.PMap;

/**
 * Implementation of the Bellman-Ford algorithm which supports negative edge costs.
 * This algorithm visits all edges of the network several times, thus it gets very slow
 * on large networks. Be careful with this! :)
 */
public class BellmanFordRouting extends AbstractRoutingAlgorithm {

    private final IntObjectMap<SPTEntry> edgeEntries = new IntObjectHashMap<>();
    private final IntObjectMap<SPTEntry> nodeEntries = new IntObjectHashMap<>();

    /**
     * Creates a new {@link BellmanFordRouting} object based on the {@link AbstractRoutingAlgorithm}.
     *
     * @param graph     specifies the graph where this algorithm will run on.
     * @param weighting set the used weight calculation (e.g. fastest, shortest).
     */
    public BellmanFordRouting(Graph graph, Weighting weighting, PMap hints) {
        super(graph, weighting, TraversalMode.EDGE_BASED);
    }

    /**
     * Calculates a path between two nodes dependent on the edge costs.
     *
     * @param from The start node of the path.
     * @param to   The end node of the path
     * @return The ideal path.
     */
    @Override
    public Path calcPath(int from, int to) {
        edgeEntries.clear();
        nodeEntries.clear();
    
        determineAllEdges();
        createStartCondition(from);

        boolean weightsUpdated = false;
        for (int i = 0; i < graph.getNodes(); i++) {
            if (!(weightsUpdated = updateWeightsOfEdges())) {
                // if nothing has been updated in this iteration, we can skip all further iterations
                break;
            }
        }

        if (weightsUpdated) {
            //if weights have been updated in the last iteration, there's a cycle in the graph due to negative weights
            throw new IllegalStateException("There's a cycle with negative weights.");
        }

        SPTEntry toNode = nodeEntries.get(to);
        if (toNode != null) {
            return PathExtractor.extractPath(graph, weighting, toNode);
        }
        return null;
    }

    /**
     * Updates the weights of the edges. The intention is to have always the smallest weighting.
     *
     * @return <code>true</code> if the edge is updated, otherwise <code>false</code>.
     */
    private boolean updateWeightsOfEdges() {
        SPTEntry u, v, edge;
        double tmpWeight;
        EdgeIteratorState edgeIt;
        boolean weightsUpdated = false;

        for (IntObjectCursor<SPTEntry> cursor : edgeEntries) {
            edge = cursor.value;

            edgeIt = graph.getEdgeIteratorState(edge.edge, edge.adjNode);

            u = nodeEntries.get(edgeIt.getBaseNode());
            if (u == null) {
                // dead end, continue with next edge
                continue;
            }

            tmpWeight = GHUtility.calcWeightWithTurnWeight(weighting, edgeIt, false, u.edge) + u.weight;

            if (tmpWeight < edge.weight) {
                edge.weight = tmpWeight;
                edge.parent = u;

                v = nodeEntries.get(edgeIt.getAdjNode());
                if (tmpWeight < v.weight) {
                    nodeEntries.put(edgeIt.getAdjNode(), edge);
                }
                weightsUpdated = true;
            }
        }
        return weightsUpdated;
    }

    private void determineAllEdges() {
        final EdgeExplorer edgeExplorer = graph.createEdgeExplorer();

        for (int node = 0; node < graph.getNodes(); node++) {
            EdgeIterator edgeIt = edgeExplorer.setBaseNode(node);
            while (edgeIt.next()) {
                SPTEntry entry = new SPTEntry(edgeIt.getEdge(), edgeIt.getAdjNode(), Double.POSITIVE_INFINITY, null);
                int id = traversalMode.createTraversalId(edgeIt, false);
                edgeEntries.put(id, entry);
                nodeEntries.put(edgeIt.getAdjNode(), entry);
            }
        }
    }

    private void createStartCondition(int from) {
        nodeEntries.put(from, new SPTEntry(from, 0));
    }

    @Override
    public int getVisitedNodes() {
        return this.graph.getNodes();
    }
}
