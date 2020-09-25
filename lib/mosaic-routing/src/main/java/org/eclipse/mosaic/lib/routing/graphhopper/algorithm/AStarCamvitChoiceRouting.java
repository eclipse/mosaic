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

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.routing.weighting.BeelineWeightApproximator;
import com.graphhopper.routing.weighting.ConsistentWeightApproximator;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.Helper;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.util.PriorityQueue;

/**
 * Calculates alternative routes next to the best route, by using bidirectional AStar as routing
 * engine and 'camvit choice routing' afterwards to determine alternative routes.
 *
 * @see AbstractCamvitChoiceRouting
 * @see <a href="http://www.camvit.com/camvit-technical-english/Camvit-Choice-Routing-Explanation-english.pdf" />
 */
public class AStarCamvitChoiceRouting extends AbstractCamvitChoiceRouting {

    private final ConsistentWeightApproximator weightApprox;

    private int visitedToCount;
    private int visitedFromCount;

    /**
     * Creates a new {@link AStarCamvitChoiceRouting} object with a graph and the weighting of it.
     *
     * @param g         The graph to check for suitability as alternative route.
     * @param weighting The weighting of the given graph.
     */
    public AStarCamvitChoiceRouting(Graph g, Weighting weighting) {
        super(g, weighting);
        BeelineWeightApproximator defaultApprox = new BeelineWeightApproximator(nodeAccess, weighting);
        defaultApprox.setDistanceCalc(Helper.DIST_PLANE);
        weightApprox = new ConsistentWeightApproximator(defaultApprox);
    }

    @Override
    public AbstractCamvitChoiceRouting initFrom(int from) {

        super.initFrom(from);

        weightApprox.setFrom(from);

        if (currTo != null) {
            currFrom.weight += weightApprox.approximate(currFrom.adjNode, false);
            currTo.weight += weightApprox.approximate(currTo.adjNode, true);
        }
        return this;
    }

    @Override
    public AbstractCamvitChoiceRouting initTo(int to) {

        super.initTo(to);

        weightApprox.setTo(to);

        if (currFrom != null) {
            currFrom.weight += weightApprox.approximate(currFrom.adjNode, false);
            currTo.weight += weightApprox.approximate(currTo.adjNode, true);
        }

        return this;
    }

    private void fillEdges(SPTEntry curr, PriorityQueue<SPTEntry> prioQueueOpenSet,
                           IntObjectMap<SPTEntry> shortestWeightMap, EdgeExplorer explorer, boolean reverse) {

        int currNode = curr.adjNode;
        EdgeIterator iter = explorer.setBaseNode(currNode);
        while (iter.next()) {
            if (!accept(iter, curr)) {
                continue;
            }

            int traversalId = traversalMode.createTraversalId(iter, reverse);
            double alreadyVisitedWeight = weighting.calcWeight(iter, reverse, curr.edge) + curr.weight;

            if (Double.isInfinite(alreadyVisitedWeight)) {
                continue;
            }

            AStarSPTEntry de = (AStarSPTEntry) shortestWeightMap.get(traversalId);
            if (de == null || de.weight > alreadyVisitedWeight) {

                double currWeightToGoal = weightApprox.approximate(iter.getAdjNode(), reverse);
                double estimationFullDist = alreadyVisitedWeight + currWeightToGoal;
                if (de == null) {
                    de = new AStarSPTEntry(iter.getEdge(), iter.getAdjNode(), alreadyVisitedWeight, estimationFullDist);
                    shortestWeightMap.put(traversalId, de);
                } else {
                    assert (de.weight > estimationFullDist) : "Inconsistent distance estimate";
                    prioQueueOpenSet.remove(de);
                    de.edge = iter.getEdge();
                    de.weight = alreadyVisitedWeight;
                    de.distanceEstimation = estimationFullDist;
                }

                de.parent = curr;
                prioQueueOpenSet.add(de);
            }
        }
    }

    /**
     * Creates the shortest path tree starting at the origin node.
     *
     * @return <code>true</code> if creating the source tree has been finished, otherwise <code>false</code>.
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
        return "choiceroutingAstar";
    }

    @Override
    SPTEntry createEdge(int edgeId, int endNode, double distance) {
        return new AStarSPTEntry(edgeId, endNode, distance, 0);
    }


    /**
     * extension of the {@link SPTEntry} which uses
     * the distanceEstimation of AStar to sort existing
     * {@link SPTEntry}s instead of the actual weight.
     */
    @SuppressWarnings(
            value="EQ_COMPARETO_USE_OBJECT_EQUALS",
            justification = "Seems to be OK here, as extended class does not implement equals/hashCode anyhow. "
    )
    private static class AStarSPTEntry extends SPTEntry {

        private double distanceEstimation;

        private AStarSPTEntry(int edgeId, int node, double weightForHeap, double distanceEstimation) {
            super(edgeId, node, weightForHeap);
            // round makes distance smaller => heuristic should underestimate the distance!
            this.distanceEstimation = (float) distanceEstimation;
        }


        public int compareTo(SPTEntry o) {
            if (o instanceof AStarSPTEntry) {
                return Double.compare(this.distanceEstimation, ((AStarSPTEntry) o).distanceEstimation);
            } else {
                return super.compareTo(o);
            }
        }
    }
}
