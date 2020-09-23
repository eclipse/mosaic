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

package org.eclipse.mosaic.lib.routing.graphhopper.extended;

import org.eclipse.mosaic.lib.routing.graphhopper.GraphLoader;
import org.eclipse.mosaic.lib.routing.graphhopper.algorithm.AlternativeRoutesRoutingAlgorithm;
import org.eclipse.mosaic.lib.routing.graphhopper.util.GraphhopperToDatabaseMapper;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.util.BikeFlagEncoder;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.FootFlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GHDirectory;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.storage.TurnCostExtension;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.shapes.GHPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * An extension of GraphHopper which is able to import map data from the MOSAIC scenario database.
 */
public class ExtendedGraphHopper extends GraphHopper {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected GraphLoader graphLoader;
    protected TurnCostExtension tcStorage;
    protected GraphhopperToDatabaseMapper mapper;

    public ExtendedGraphHopper(GraphLoader graphLoader, GraphhopperToDatabaseMapper mapper) {
        this.graphLoader = graphLoader;
        this.mapper = mapper;

        setCHEnabled(false);
        setEncodingManager(EncodingManager.create(
                new CarFlagEncoder(5, 5, 127),
                new BikeFlagEncoder(),
                new FootFlagEncoder())
        );
    }

    @Override
    public GraphHopper importOrLoad() {
        GHDirectory dir = new RAMDirectory();

        tcStorage = new TurnCostExtension() {
            // is required since we need to store the way type of edges
            public boolean isRequireEdgeField() {
                return true;
            }

            public int getDefaultEdgeFieldValue() {
                return 0;
            }
        };

        GraphHopperStorage graph = new GraphHopperStorage(dir, getEncodingManager(), true, tcStorage);

        setGraphHopperStorage(graph);
        graph.setSegmentSize(-1);

        importDB(getGraphHopperLocation());

        return this;
    }

    private void importDB(String ghLocation) {
        if (getGraphHopperStorage() == null) {
            throw new IllegalStateException("Load or init graph before import database");
        }

        if (mapper == null) {
            throw new IllegalStateException("A mapper is required for importing database");
        }

        logger.info("start creating graph from database");

        graphLoader.initialize(getGraphHopperStorage(), getEncodingManager(), mapper);
        graphLoader.loadGraph();

        postProcessing();
        try {
            cleanUp();
        } catch (Exception e) {
            logger.warn("Could not clean up routing graph, skipping. Routing might not work as expected!");
        }
        // optimize();
        flush();
    }

    @Override
    public GHResponse route(GHRequest request) {
        if (request instanceof ExtendedGHRequest) {
            return route((ExtendedGHRequest) request);
        }
        throw new UnsupportedOperationException("ExtendedGHRequest is required");
    }

    private ExtendedGHResponse route(ExtendedGHRequest request) {
        if (getGraphHopperStorage() == null) {
            throw new IllegalStateException("no graph has yet been initialized");
        }

        ExtendedGHResponse rsp = new ExtendedGHResponse();

        if (!getEncodingManager().hasEncoder(request.getVehicle())) {
            rsp.addError(
                    new IllegalArgumentException("Vehicle " + request.getVehicle() + " unsupported. Supported are: " + getEncodingManager())
            );
            return rsp;
        }

        FlagEncoder encoder = getEncodingManager().getEncoder(request.getVehicle());

        GHPoint fromPoint = Iterables.getFirst(request.getPoints(), null);
        GHPoint toPoint = Iterables.getLast(request.getPoints(), null);

        EdgeFilter edgeFilterFrom = createEdgeFilter(fromPoint, encoder);
        EdgeFilter edgeFilterTo = createEdgeFilter(toPoint, encoder);

        if (fromPoint == null || toPoint == null) {
            rsp.addError(new IllegalArgumentException("Not enough points given"));
            return rsp;
        }

        StopWatch sw = new StopWatch().start();

        final QueryResult fromRes = getLocationIndex().findClosest(fromPoint.lat, fromPoint.lon, edgeFilterFrom);
        final QueryResult toRes = getLocationIndex().findClosest(toPoint.lat, toPoint.lon, edgeFilterTo);

        if (fromRes.getClosestNode() < 0 || toRes.getClosestNode() < 0) {
            rsp.addError(new IllegalArgumentException("Request location(s) can not be snapped to the network"));
            return rsp;
        }

        QueryGraph queryGraph = new QueryGraph(getGraphHopperStorage());
        queryGraph.lookup(Lists.newArrayList(fromRes, toRes));

        String debug = "graphLookup:" + sw.stop().getSeconds() + "s";

        sw = new StopWatch().start();
        Weighting weighting = request.getWeightingInstance(queryGraph);

        RoutingAlgorithm algorithm = request.getAlgorithmFactory().createAlgorithm(queryGraph, weighting);

        if (algorithm instanceof AlternativeRoutesRoutingAlgorithm && request.getAlternatives() > 0) {
            ((AlternativeRoutesRoutingAlgorithm) algorithm).setRequestAlternatives(request.getAlternatives());
        }

        debug += ", algoInit:" + sw.stop().getSeconds() + "s";
        sw = new StopWatch().start();

        Path bestPath = algorithm.calcPath(fromRes.getClosestNode(), toRes.getClosestNode());
        debug += ", " + algorithm.getName() + "-routing:" + sw.stop().getSeconds() + "s, " + bestPath.getDebugInfo();

        final List<Path> paths = new ArrayList<Path>();
        paths.add(bestPath);
        if (algorithm instanceof AlternativeRoutesRoutingAlgorithm) {
            paths.addAll(((AlternativeRoutesRoutingAlgorithm) algorithm).getAlternativePaths());
        }

        ExtendedGHResponse currentResponse = null;
        int i = 1;
        for (Path path : paths) {
            if (currentResponse == null) {
                // the first path is always the requested shortest path
                currentResponse = rsp;
            } else {
                // if more than one route returned, we add a new response
                currentResponse = new ExtendedGHResponse();
                rsp.addRouteResponse(currentResponse);
                debug = "alternative path: " + (i++) + ", " + path.getDebugInfo();
            }
            currentResponse.setPath(path).addDebugInfo(debug);
        }
        return rsp;
    }



    private EdgeFilter createEdgeFilter(final GHPoint point, FlagEncoder encoder) {
        if (point instanceof ExtendedGHPoint) {
            return edgeState -> edgeState.getEdge() == ((ExtendedGHPoint) point).getEdgeId();
        }
        return DefaultEdgeFilter.allEdges(encoder);
    }

}
