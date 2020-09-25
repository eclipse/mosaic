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
import com.carrotsearch.hppc.cursors.IntCursor;
import com.graphhopper.routing.AbstractRoutingAlgorithm;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.EdgeIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Implementation of the 'Camvit Choice Routing' algorithm to calculate alternative routes next to
 * the best route. After building the source tree and target tree by the router on top of this
 * class, edges which are in both trees will be determined. Afterwards, all of those edges which are
 * connected to each other will be collected in a set of so called 'plateaus'. Those plateaus are
 * either good (many edges / highest weight) or bad (few edges / low weight). One plateau can be
 * used to calculate an alternative path from A to B going trough this plateau, by traversing from
 * the start of a plateau to A by using the target tree and by traversing from the end of the
 * plateau to B by using the source tree. By choosing the best plateau a good alternative route next
 * to the best route can be found.
 *
 * @see <a href="http://www.camvit.com/camvit-technical-english/Camvit-Choice-Routing-Explanation-english.pdf" />
 */
public abstract class AbstractCamvitChoiceRouting extends AbstractRoutingAlgorithm implements AlternativeRoutesRoutingAlgorithm {

    private double constraintMaxShare = 0.9d;
    private double constraintMaxStretch = 0.8d;
    private double constraintMaxUniformlyBoundedStretch = 0.7d;
    private double constraintThresholdLocalOptimality = 0.3d;
    private double constraintMinLocalOptimality = 0.1d;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected int from;
    protected int to;

    private IntObjectMap<SPTEntry> shortestWeightsFrom;
    private IntObjectMap<SPTEntry> shortestWeightsTo;

    private PriorityQueue<SPTEntry> heapFrom;
    private PriorityQueue<SPTEntry> heapTo;

    private boolean alreadyRun;
    SPTEntry currFrom;
    SPTEntry currTo;

    private PriorityQueue<Plateau> plateaus;
    private PriorityQueue<RelEdge> relevantEdgesQ;
    private HashSet<Integer> relevantEdgesIDs;

    private int requestAlternatives = 0;

    private List<Path> alternativePaths;

    AbstractCamvitChoiceRouting(Graph g, Weighting type) {
        super(g, type, TraversalMode.EDGE_BASED);
        initCollections(Math.max(20, graph.getNodes()));
    }

    /**
     * initializes all required collections and maps.
     *
     * @param nodes the number of nodes in the graph
     */
    private void initCollections(int nodes) {
        try {
            heapFrom = new PriorityQueue<>(nodes / 10);
            shortestWeightsFrom = new IntObjectHashMap<>(nodes / 10);

            heapTo = new PriorityQueue<>(nodes / 10);
            shortestWeightsTo = new IntObjectHashMap<>(nodes / 10);
        } catch (OutOfMemoryError e) {
            logger.error("Not sufficient memory", e);
            throw e;
        }
    }



    /**
     * Returns the identifier of an already traversed edge entry. In edge-based algorithms it is
     * usually the id of the edge, but it might be extended by a flag which signalizes the direction
     * of the edge (if algorithm is direction sensitive)-
     *
     * @return the identifier of an already traversed edge entry.
     */
    private int createTraversalIdentifier(SPTEntry iter, boolean reverse) {
        if (iter.parent != null) {
            return traversalMode.createTraversalId(iter.parent.adjNode, iter.adjNode, iter.edge, reverse);
        }
        return iter.edge;

    }


    /**
     * Determines, if the currently traversed edge should be continued with.
     *
     * @param edge     the edge which is currently being traversed.
     * @param currEdge the preceding edge.
     * @return <code>true</code> if this edge is acceptable to continue with.
     */
    protected boolean accept(EdgeIterator edge, SPTEntry currEdge) {
        return (currEdge.edge == EdgeIterator.NO_EDGE || edge.getEdge() != currEdge.edge) && super.accept(edge, currEdge.edge);
    }

    /**
     * Initializes the routing process by announcing the source node.
     *
     * @param from the source node
     */
    public AbstractCamvitChoiceRouting initFrom(int from) {
        this.from = from;
        currFrom = createEdge(EdgeIterator.NO_EDGE, from, 0);
        return this;
    }

    /**
     * Creates the first initial edge.
     *
     * @return an instance of CRAStarEdge
     */
    abstract SPTEntry createEdge(int edgeId, int endNode, double distance);

    /**
     * Initializes the routing process by announcing the target node.
     *
     * @param to the target node.
     */
    public AbstractCamvitChoiceRouting initTo(int to) {
        this.to = to;
        currTo = createEdge(EdgeIterator.NO_EDGE, to, 0);
        return this;
    }

    @Override
    public void setRequestAlternatives(int alternatives) {
        this.requestAlternatives = alternatives;
    }

    /**
     * Calculates the best and alternative paths between the specified nodes.
     *
     * @param from Start of the path.
     * @param to   End of the path.
     * @return The calculated path.
     */
    @Override
    public Path calcPath(int from, int to) {
        if (alreadyRun) {
            throw new IllegalStateException("Create a new instance per call");
        }
        alreadyRun = true;

        alternativePaths = new ArrayList<Path>();

        logger.debug("calc " + (requestAlternatives + 1) + " paths from " + from + " to " + to);

        if (from == to) {
            return new Path(graph, weighting);
        }

        initFrom(from);
        initTo(to);

        boolean finished = false;
        while (!finished) {
            finished = fillEdgesFrom(shortestWeightsFrom, heapFrom) & fillEdgesTo(shortestWeightsTo,
                    heapTo);
        }

        PlateauPath path = extractPath();

        if (requestAlternatives > 0) {
            // determine edges which has been traversed in both source and target
            // tree
            determineRelevantEdges(path.getWeight());

            // determine edges which are connected with each other
            determinePlateaus(path.getWeight());

            // extracts alternative paths
            calculateAlternativePaths(path);
        }

        return path;
    }

    /**
     * Returns a list of alternative paths (not including the optimal path).
     *
     * @return The alternative paths, is empty if no alternative paths have been requested.
     */
    @Override
    public List<Path> getAlternativePaths() {
        return alternativePaths;
    }

    public PlateauPath extractPath() {
        PlateauPath path = (PlateauPath) new PlateauPath(graph, weighting).setSPTEntry(currFrom).extract();
        path.setWeight(currFrom.weight);
        return path;
    }

    /**
     * Executes one step of traversal in forward direction starting at the source node.
     *
     * @return <code>true</code> if creating the source tree has been finished, otherwise
     * <code>false</code>
     */
    abstract boolean fillEdgesFrom(IntObjectMap<SPTEntry> weights, PriorityQueue<SPTEntry> heap);

    /**
     * Executes one step of traversal in backward direction starting at the target node.
     *
     * @return <code>true</code> if creating the target tree has been finished, otherwise
     * <code>false</code>
     */
    abstract boolean fillEdgesTo(IntObjectMap<SPTEntry> weights, PriorityQueue<SPTEntry> heap);

    /**
     * @return <code>true</code>, if both path searches reached their final node.
     */
    abstract boolean finished(SPTEntry currEdge, int to);

    @Override
    protected boolean finished() {
        throw new UnsupportedOperationException("Not required");
    }

    /**
     * Collects all edges which have been traversed by both forward and backward path searches. All
     * edges receive a weighting depending on their distances towards the target and source node.
     * Furthermore, edges which are far away from the best path are ignored to speed up the search.
     *
     * @param optimalWeight the weight of the optimal path.
     */
    private void determineRelevantEdges(double optimalWeight) {
        relevantEdgesQ = new PriorityQueue<>(Math.max(1, shortestWeightsFrom.size() / 10),
                Comparator.comparingDouble(o -> o.rating)
        );

        relevantEdgesIDs = new HashSet<>();

        double maxWeight = optimalWeight * (1.0d + constraintMaxStretch);

        for (IntCursor key : shortestWeightsFrom.keys()) {
            SPTEntry edge = shortestWeightsFrom.get(key.value);
            if (edge.parent != null) {
                SPTEntry edgeOfTargetTree = shortestWeightsTo.get(key.value);
                if (edgeOfTargetTree != null && edgeOfTargetTree.edge == edge.edge && edge.adjNode != edgeOfTargetTree.adjNode) {
                    //create a new relevant edge if it has been found in both search trees
                    //the rating is composed by the distances of the edge towards source and target

                    RelEdge relEdge = new RelEdge();
                    relEdge.fwd = edge;
                    relEdge.bwd = edgeOfTargetTree;
                    relEdge.rating = (edgeOfTargetTree.parent != null)
                            ? edge.weight + edgeOfTargetTree.parent.weight
                            : edgeOfTargetTree.weight + edge.parent.weight;

                    if (relEdge.rating < maxWeight && relEdge.fwd.weight < Double.MAX_VALUE
                            && relEdge.bwd.weight < Double.MAX_VALUE) {
                        relevantEdgesQ.add(relEdge);
                        relevantEdgesIDs.add(key.value);
                    }
                }
            }
        }
    }

    /**
     * The set of relevant edges is used to build plateaus. A plateau consists of a sequence of
     * relevant edges. If a plateau has been built, all its edges are removed out of the set of
     * relevant edges. The relevant edge with the lowest rating is used to build the next plateau.
     *
     * @param optimalWeight the weight of the optimal path.
     */
    private void determinePlateaus(double optimalWeight) {
        plateaus = new PriorityQueue<Plateau>(Math.max(1, relevantEdgesQ.size() / 10),
                new Comparator<Plateau>() {
                    @Override
                    public int compare(Plateau o1, Plateau o2) {
                        // sub paths with equal rating but highest weight on top
                        int r = Double.compare(o1.rating(), o2.rating());
                        if (r == 0) {
                            return Double.compare(o1.weight(), o2.weight());
                        } else {
                            return r;
                        }
                    }
                });

        double bestRating = Double.MAX_VALUE;

        while (!relevantEdgesQ.isEmpty()) {
            RelEdge relEdge = relevantEdgesQ.poll();

            bestRating = Math.min(relEdge.rating, bestRating);

            // if edge pointers of current relevant edge is already a member of
            // a plateau, reject this relevant edge
            if (!relevantEdgesIDs.contains(createTraversalIdentifier(relEdge.fwd, false))
                    && !relevantEdgesIDs.contains(createTraversalIdentifier(relEdge.bwd, true))) {
                continue;
            }

            // if rating is twice expensive than best rating, then cancel search
            // for plateaus
            if (relEdge.rating > bestRating * 2) {
                relevantEdgesQ.clear();
                break;
            }

            // create new plateau
            Plateau plateau = new Plateau();
            plateau.start = relEdge.fwd;
            plateau.startRev = relEdge.bwd;
            plateau.targetRev = relEdge.bwd;

            // the distance of relevant edge from target
            double distance = plateau.startRev.weight;

            // traverse backward until we found the start of the plateaus, that
            // is as long the parent edge is relevant
            SPTEntry tmp = plateau.start.parent;
            while (tmp != null && tmp.parent != null
                    && relevantEdgesIDs.remove(createTraversalIdentifier(tmp, false))
            ) {
                distance += tmp.weight - tmp.parent.weight;
                // build new edge entry for the backward pointer startRev
                SPTEntry entry = new SPTEntry(tmp.edge, tmp.parent.adjNode, distance);
                entry.parent = plateau.startRev;
                plateau.startRev = entry;
                plateau.start = tmp;
                tmp = tmp.parent;
            }

            // traverse forward until we found the end of the plateaus
            tmp = plateau.targetRev.parent;
            while (tmp != null && tmp.parent != null
                    && relevantEdgesIDs.remove(createTraversalIdentifier(tmp, true))
            ) {
                plateau.targetRev = tmp;
                tmp = tmp.parent;
            }

            // the local optimality = weight of plateau / weight of optimal path
            plateau.localOptimality = (plateau.startRev.weight - plateau.targetRev.parent.weight) / optimalWeight;

            // if plateau consists of only one edge or is not locally optimal,
            // then reject it
            if (plateau.start.edge != plateau.targetRev.edge && plateau.localOptimality >= constraintMinLocalOptimality) {
                plateaus.add(plateau);
            }

            // mark the relevant edge as used
            relevantEdgesIDs.remove(createTraversalIdentifier(relEdge.fwd, false));
            relevantEdgesIDs.remove(createTraversalIdentifier(relEdge.bwd, true));
        }

        // we don't need those anymore
        relevantEdgesIDs.clear();
    }

    /**
     * Out of the plateaus, path are generated.
     *
     * @param optimalPath the optimal path
     */
    private void calculateAlternativePaths(PlateauPath optimalPath) {
        alternativePaths.clear();

        if (requestAlternatives == 0) {
            return;
        }

        if (plateaus != null) {
            // the first subPath is also the best path we've already found, so
            // we refuse it as an alternative
            plateaus.poll();
        }

        // add alternative paths as long as their weight is not greater than
        // weight of original path + drift
        PlateauPath nextPath = createPathFromPlateau(optimalPath);
        while (nextPath != null) {

            // logger.debug("Found next path: weight "+nextPath.getWeight()+"");
            alternativePaths.add(nextPath);
            // continue with next path, if we not yet have the desired number of
            // paths (maxPath < 0 means as many as possible)
            nextPath = (alternativePaths.size() < requestAlternatives || requestAlternatives < 0) ? createPathFromPlateau(
                    optimalPath) : null;
        }
    }

    private PlateauPath createPathFromPlateau(PlateauPath optimalPath) {
        if (plateaus.isEmpty()) {
            return null;
        }

        // get plateau with highest weight (the next best sub path)
        final Plateau plateau = plateaus.poll();

        // weight of path
        final double pathWeight = plateau.startRev.weight + plateau.start.weight;

        // weight of plateau
        final double plateauWeight = plateau.startRev.weight - plateau.targetRev.parent.weight;

        double ubs = 0;

        // if local optimality is below a certain threshold, the uniformly
        // bounded stretch will be calculated (quite expensive procedure)
        if (plateau.localOptimality < constraintThresholdLocalOptimality) {
            double maxWeight = plateauWeight + (optimalPath.getWeight() - plateauWeight) / 2;

            while (maxWeight > plateauWeight * 1.5d) {
                ubs = Math.max(ubs, calculateUniformleyBoundedStretch(plateau, maxWeight));
                maxWeight = plateauWeight + (maxWeight - plateauWeight) / 2;

                //if UBS exceeds the max value, the path will be rejected
                if (ubs > constraintMaxUniformlyBoundedStretch) {
                    return createPathFromPlateau(optimalPath);
                }
            }
        }

        final String debugInfo = String.format(Locale.ENGLISH,
                "%s, ubs: %.2f, lo: %.2f, weight_optimal: %.2f, weight_plateau: %.2f",
                createPlateauString(plateau), ubs, plateau.localOptimality, optimalPath.getWeight(),
                plateauWeight);
        final PlateauPath path = new PlateauPath(graph, weighting) {
            public String getDebugInfo() {
                return debugInfo;
            }
        };

        // target node
        final int to = optimalPath.getSptEntry().adjNode;

        // create path by traversing downwards until target node
        SPTEntry targetEdge = plateau.start;
        SPTEntry currentRevOfPlateau = plateau.startRev.parent;
        while (currentRevOfPlateau != null) {
            int endNode = (currentRevOfPlateau.parent != null) ? currentRevOfPlateau.parent.adjNode : to;
            SPTEntry newEntry = new SPTEntry(currentRevOfPlateau.edge, endNode, pathWeight - currentRevOfPlateau.weight);
            newEntry.parent = targetEdge;
            targetEdge = newEntry;
            currentRevOfPlateau = currentRevOfPlateau.parent;
            if (endNode == to) {
                break;
            }
        }
        path.setWeight(targetEdge.weight);
        path.setSPTEntry(targetEdge).extract();

        if (filter(optimalPath, path)) {
            return createPathFromPlateau(optimalPath);
        } else {
            return path;
        }
    }

    /**
     * Applies several filters on a newly generated path. If one filter returns true, the new path
     * will be rejected. Following filters are applied:<ul><li>
     * Route Share</li><li>
     * Route Stretch</li><li>
     * Contains loop</li></ul>
     */
    private boolean filter(PlateauPath optimalPath, PlateauPath newPath) {
        /*
         * it is sufficient to check similarity against the best path only,
         * because plateaus derive only from the best path.
         */
        if (checkSimilarity(optimalPath, newPath) > constraintMaxShare) {
            return true;
        }

        // filter route, if it's more than 1+STRETCH_MAX times longer (distance)
        // than the shortest (distance) route
        if (newPath.getDistance() > optimalPath.getDistance() * (1 + constraintMaxStretch)) {
            return true;
        }

        if (containsLoop(newPath)) {
            return true;
        }

        return false;
    }

    /**
     * This method checks whether a loop between the new and the previous founded paths exist.
     *
     * @param newPath The new path to check for loop.
     * @return True if it contains a loop.
     */
    private boolean containsLoop(PlateauPath newPath) {
        Set<Integer> visitedEdges = new HashSet<>();

        // first edge of already found path
        SPTEntry edge = newPath.getSptEntry();

        while (edge.parent.edge != EdgeIterator.NO_EDGE) {
            if (!visitedEdges.add(createTraversalIdentifier(edge, false))) {
                return true;
            }
            edge = edge.parent;
        }

        return false;
    }

    /**
     * This method checks the similarity between an existing and new path.
     *
     * @param existingPath The existing path to check for similarity with a new path.
     * @param newPath      The new path to check for similarity with an existing path.
     * @return A factor, which indicating the similarity.
     */
    private double checkSimilarity(PlateauPath existingPath, PlateauPath newPath) {

        Set<Integer> edgesOfOriginal = new HashSet<Integer>();
        // first edge of already found path
        SPTEntry edge1 = existingPath.getSptEntry();
        // first edge of new path
        SPTEntry edge2 = newPath.getSptEntry();

        while (edge1.parent.edge != EdgeIterator.NO_EDGE) {
            edgesOfOriginal.add(edge1.edge);
            edge1 = edge1.parent;
        }

        double sharedWeight = 0.0d;
        while (edge2.parent.edge != EdgeIterator.NO_EDGE) {
            if (edgesOfOriginal.remove(edge2.edge)) {
                sharedWeight += Math.abs(edge2.weight - edge2.parent.weight);
            }
            edge2 = edge2.parent;
        }

        return sharedWeight / existingPath.getWeight();
    }

    /**
     * Calculates the value for uniformly bounded stretch (UBS) for a plateau. UBS describes the
     * stretch of the full detour. Since a shortest path search is required this procedure is quite
     * expensive.
     *
     * @return the stretch of the detour (0 means no stretch)
     */
    private double calculateUniformleyBoundedStretch(Plateau plateau, double maxWeight) {

        // the weight of the plateau
        double weightDetour = plateau.startRev.weight - plateau.targetRev.parent.weight;

        // the start and end edge of the plateau (pointers)
        SPTEntry start = plateau.start;
        SPTEntry end = plateau.targetRev;

        while (start.parent.edge != EdgeIterator.NO_EDGE || end.parent.edge != EdgeIterator.NO_EDGE) {

            // move the start point towards the source and add its weight
            if (start.parent.edge != EdgeIterator.NO_EDGE) {
                weightDetour += (start.weight - start.parent.weight);
                start = start.parent;
            }

            // move the end point towards the target and add its weight
            if (end.parent.edge != EdgeIterator.NO_EDGE) {
                weightDetour += (end.weight - end.parent.weight);
                end = end.parent;
            }

            if (weightDetour >= maxWeight) {
                double weightShortest = calculateWeightOfShortestSubPath(start, end);
                return weightDetour / weightShortest - 1;
            }
        }
        return 0;
    }

    private double calculateWeightOfShortestSubPath(SPTEntry start, SPTEntry end) {
        from = start.adjNode;
        to = end.adjNode;
        initCollections(Math.max(20, graph.getNodes()));
        initFrom(from);
        initTo(to);
        boolean finished = false;
        while (!finished) {
            finished = fillEdgesFrom(shortestWeightsFrom, heapFrom);
        }
        return currFrom.weight;
    }

    /**
     * Creates a {@link String} for the {@link Plateau} with start and end points.
     *
     * @param plateau The {@link Plateau} for which to create the string.
     * @return The created {@link Plateau} string.
     */
    private String createPlateauString(Plateau plateau) {
        StringBuilder plateauString = new StringBuilder();
        plateauString.append("plateau: ");
        SPTEntry tmpEdge = plateau.startRev;
        while (tmpEdge.parent.edge != EdgeIterator.NO_EDGE && tmpEdge.edge != plateau.targetRev.edge) {
            plateauString.append(tmpEdge.edge);
            plateauString.append("|");
            if (tmpEdge.parent.edge == plateau.targetRev.edge) {
                plateauString.append(tmpEdge.parent.edge);
            }
            tmpEdge = tmpEdge.parent;
        }
        return plateauString.toString();
    }

    /**
     * Represents a relevant edge. A relevant edge has been traversed by both path searches.
     */
    public static class RelEdge {

        /**
         * EdgeEntry whose parent points along the shortest path towards the source node. Its weight
         * is the distance from this edge to the source node.
         */
        SPTEntry fwd;

        /**
         * EdgeEntry whose parent points along the shortest path towards the target node. Its weight
         * is the distance from this edge to the target node.
         */
        SPTEntry bwd;

        /**
         * A rating of the relevant edge which is composed of the sum of the costs of both edge
         * entries.
         */
        double rating;
    }

    /**
     * A plateau represents a sequence of edges which have been traversed by both path searches.
     */
    public static class Plateau {

        /**
         * Marks the start of the plateau and points towards the source of the path. By following
         * its parents the shortest path from the start of the plateau towards the source node can
         * be determined.
         */
        SPTEntry start;

        /**
         * Marks the start of the plateau and points towards the target of the path. By following
         * its parents the path along the plateau towards the target can be determined.
         */
        SPTEntry startRev;

        /**
         * Marks the end of the plateau and points towards the target of the path. By following its
         * parents the shortest path from the end of the plateau towards the target node can be
         * determined.
         */
        SPTEntry targetRev;

        /**
         * Stores the local optimality of the plateau which has previously been calculated.
         */
        double localOptimality;

        /**
         * Calculates the weight of the resulting path of the plateau.
         */
        public double weight() {
            if (start.parent == null) {
                return startRev.weight;
            } else {
                return start.parent.weight + startRev.weight;
            }
        }

        /**
         * Calculates the rating of the plateau. The farther away from source or target the higher
         * the rating.
         */
        public double rating() {
            double costsAtStartOfPlateau = (start.parent == null) ? 0 : start.parent.weight;
            double costsAtEndOfPlateau = (targetRev.parent == null) ? 0 : targetRev.parent.weight;

            return costsAtStartOfPlateau + costsAtEndOfPlateau;

        }
    }

    public final void setUbsMax(double ubsMax) {
        this.constraintMaxUniformlyBoundedStretch = ubsMax;
    }

    public final void setRouteShareMax(double shareMax) {
        this.constraintMaxShare = shareMax;
    }

    public final void setRouteStretchMax(double stretchMax) {
        this.constraintMaxStretch = stretchMax;
    }

    public final void setLocalOptimalityMin(double loMin) {
        this.constraintMinLocalOptimality = loMin;
    }

    public final void setUbsThreshold(double ubsThreshold) {
        this.constraintThresholdLocalOptimality = ubsThreshold;
    }


}
