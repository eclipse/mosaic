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
 */

package org.eclipse.mosaic.lib.routing.graphhopper.algorithm;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;

import java.util.PriorityQueue;

/**
 * Calculates alternative routes next to the best route, by using bidirectional Dijkstra as routing
 * engine and 'camvit choice routing' afterwards to determine alternative routes.
 *
 * @see AbstractCamvitChoiceRouting
 * @see <a href="http://www.camvit.com/camvit-technical-english/Camvit-Choice-Routing-Explanation-english.pdf" />
 */
public class DijkstraCamvitChoiceRouting extends AbstractCamvitChoiceRouting {

    private int visitedToCount;
    private int visitedFromCount;

    /**
     * Creates a new {@link DijkstraCamvitChoiceRouting} object while using Dijkstra and camvit choice routing.
     *
     * @param g    Specifies the graph where this algorithm will run on.
     * @param type Set the used weight calculation.
     */
    public DijkstraCamvitChoiceRouting(Graph g, Weighting type) {
        super(g, type);
    }

    void fillEdges(SPTEntry curr, PriorityQueue<SPTEntry> prioQueue,
                   IntObjectMap<SPTEntry> shortestWeightMap, EdgeExplorer explorer, boolean reverse) {

        EdgeIterator iter = explorer.setBaseNode(curr.adjNode);
        while (iter.next()) {
            if (!accept(iter, curr)) {
                continue;
            }

            int traversalId = traversalMode.createTraversalId(iter, reverse);//createTraversalIdentifier(iter, reverse);
            double tmpWeight = weighting.calcWeight(iter, reverse, curr.edge) + curr.weight;

            if (Double.isInfinite(tmpWeight)) {
                continue;
            }

            SPTEntry de = shortestWeightMap.get(traversalId);
            if (de == null) {
                de = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
                de.parent = curr;
                shortestWeightMap.put(traversalId, de);
                prioQueue.add(de);
            } else if (de.weight > tmpWeight) {
                prioQueue.remove(de);
                de.edge = iter.getEdge();
                de.weight = tmpWeight;
                de.parent = curr;
                prioQueue.add(de);
            }
        }
    }

    /**
     * Creates the shortest path tree starting at the source node.
     *
     * @return <code>true</code> if creating the source tree has been finished, otherwise
     * <code>false</code>
     */
    protected boolean fillEdgesFrom(IntObjectMap<SPTEntry> weights, PriorityQueue<SPTEntry> heap) {
        if (currFrom != null) {
            if (finished(currFrom, to)) {
                return true;
            }

            fillEdges(currFrom, heap, weights, outEdgeExplorer, false);
            visitedFromCount++;
            if (heap.isEmpty()) {
                return true;
            }
            currFrom = heap.poll();

        } else if (currTo == null) {
            //creating the source tree is finished when target tree has been finished as well
            return true;
        }
        return false;
    }

    protected boolean finished(SPTEntry currEdge, int to) {
        return currEdge.adjNode == to;
    }

    /**
     * Creates the shortest path tree in reverse direction starting at the target node.
     *
     * @return <code>true</code> if creating the target tree has been finished, otherwise
     * <code>false</code>
     */
    protected boolean fillEdgesTo(IntObjectMap<SPTEntry> weights, PriorityQueue<SPTEntry> heap) {
        if (currTo != null) {
            if (finished(currTo, from)) {
                return true;
            }
            fillEdges(currTo, heap, weights, inEdgeExplorer, true);
            visitedToCount++;
            if (heap.isEmpty()) {
                return true;
            }
            currTo = heap.poll();
        } else if (currFrom == null) {
            //creating the target tree is finished when source tree has been finished as well
            return true;
        }
        return false;
    }

    @Override
    public int getVisitedNodes() {
        return visitedFromCount + visitedToCount;
    }

    @Override
    public String getName() {
        return "choicerouting";
    }

    @Override
    SPTEntry createEdge(int edgeId, int endNode, double distance) {
        return new SPTEntry(edgeId, endNode, distance);
    }

}
