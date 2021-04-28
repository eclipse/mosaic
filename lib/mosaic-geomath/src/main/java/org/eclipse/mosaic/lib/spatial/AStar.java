/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.spatial;

import org.eclipse.mosaic.lib.geo.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Generic implementation of a node-based A* algorithm for routing across {@link Point} based road networks. In order to use
 * this, the routing network has to be presented as a list of network nodes implementing {@link AStar.Node}. The
 * routing behavior can be influenced by the costs returned by the node implementation.
 */
public class AStar<T extends AStar.Node<T, P>, P extends Point<P>> {

    private final PriorityQueue<CostNode> openList = new PriorityQueue<>();
    private final Map<Node<T, P>, CostNode> openListNodes = new HashMap<>();
    private final Set<Node<T, P>> closedList = new HashSet<>();

    public List<T> route(Node<T, P> from, Node<T, P> to) {
        CostNode destinationNode = null;
        closedList.clear();
        openList.clear();
        openListNodes.clear();
        boolean routeFound = false;

        openList.add(new CostNode(from, 0));
        // loop until destination is found or no candidates are left in open list
        while (!openList.isEmpty()) {
            // get node with smallest f from open list (and remove it from open list)
            CostNode current = openList.poll();
            openListNodes.remove(current.node);
            if (current.node == to) {
                // route found
                routeFound = true;
                destinationNode = current;
                break;
            }

            // add succeeding nodes to open list
            expandNode(current, to);

            // current node is examined
            closedList.add(current.node);
        }

        if (routeFound) {
            List<T> route = new ArrayList<>();
            CostNode routeNode = destinationNode;
            route.add(routeNode.node.getSelf());
            while (routeNode.predecessor != null) {
                routeNode = routeNode.predecessor;
                route.add(routeNode.node.getSelf());
            }
            Collections.reverse(route);
            return route;
        } else {
            return null;
        }
    }

    private void expandNode(CostNode node, Node<T, P> dest) {
        for (Node<T, P> nextNode : node.node.getNexts()) {
            if (closedList.contains(nextNode)) {
                // node is already fully evaluated - skip it
                continue;
            }

            double temporaryCosts = node.costs + node.node.getCost(nextNode);
            double estimatedCostsToDestination = temporaryCosts + node.node.estimateCost(dest);

            // check if next node is already in open list
            CostNode nextCostNode = openListNodes.get(nextNode);
            if (nextCostNode == null) {
                // node is not yet in open list - add it
                nextCostNode = new CostNode(nextNode, estimatedCostsToDestination);
                nextCostNode.costs = temporaryCosts;
                nextCostNode.predecessor = node;
                openList.add(nextCostNode);
                openListNodes.put(nextNode, nextCostNode);

            } else if (temporaryCosts < nextCostNode.costs) {
                // next node is already in open list but this way is cheaper
                // remove and re-add node with updated weight to maintain queue order
                openList.remove(nextCostNode);
                nextCostNode.estimate = estimatedCostsToDestination;
                nextCostNode.costs = temporaryCosts;
                nextCostNode.predecessor = node;
                openList.add(nextCostNode);
            }
        }
    }

    private class CostNode implements Comparable<CostNode> {
        /**
         * cost of the cheapest path from start to node
         */
        private double costs;
        /**
         * best guess of total cost of path from start to destination if it goes through node
         */
        private double estimate;

        private final Node<T, P> node;
        private CostNode predecessor;

        private CostNode(Node<T, P> node, double estimatedCostsToDestination) {
            this.node = node;
            this.estimate = estimatedCostsToDestination;
            this.costs = 0;
        }

        @Override
        public int compareTo(CostNode o) {
            return Double.compare(estimate, o.estimate);
        }
    }

    /**
     * A* routing network node.
     */
    public interface Node<T extends Node<T, P>, P extends Point<P>> {

        T getSelf();

        P getPosition();

        /**
         * Returns a list of network nodes directly reachable from this node.
         */
        List<? extends Node<T, P>> getNexts();

        /**
         * Returns the exact cost between adjacent network nodes.
         * <p>
         * Costs are typically based on distance: traveling from one node to another is cheap if the nodes are
         * very close and expensive if they are far apart. However there might be other factors influencing the
         * cost: e.g. travel speed, road capacity, etc.
         */
        default double getCost(Node<T, P> nextNode) {
            return getPosition().distanceTo(nextNode.getPosition());
        }

        /**
         * Returns the estimated cost in order to reach the final destination node from this node.
         * <p>
         * The estimated cost must be lower or equal to the exact cost. If the returned value is higher than the final
         * exact cost would be this node might be excluded from the routing graph before the exact cost is known
         * and a potentially more expensive route might win.
         * <p>
         * Typically, the cost is estimated by using the straight line distance between this node and the destination
         * node. However, care must be taken if exact costs between nodes do not only depend on distance but consider
         * other factors (travel speed, ...) as well.
         */
        default double estimateCost(Node<T, P> destinationNode) {
            return getPosition().distanceTo(destinationNode.getPosition());
        }
    }
}