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

package org.eclipse.mosaic.lib.routing.graphhopper;

import org.eclipse.mosaic.lib.routing.RoutingCostFunction;
import org.eclipse.mosaic.lib.routing.graphhopper.util.GraphhopperToDatabaseMapper;
import org.eclipse.mosaic.lib.routing.graphhopper.util.TurnCostsProvider;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.WeightingFactory;
import com.graphhopper.routing.ev.Subnetwork;
import com.graphhopper.routing.subnetwork.PrepareRoutingSubnetworks;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * An extension of GraphHopper which is able to import map data from the MOSAIC scenario database.
 * Routing functionality is implemented {@link org.eclipse.mosaic.lib.routing.graphhopper.GraphHopperRouting}.
 */
class ExtendedGraphHopper extends GraphHopper {

    static final String WEIGHTING_TURN_COSTS = "weighting.turnCosts";
    static final String WEIGHTING_COST_FUNCTION = "weighting.costFunction";
    static final String WEIGHTING_GRAPH_MAPPER = "weighting.graphMapper";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected GraphLoader graphLoader;
    protected GraphhopperToDatabaseMapper mapper;
    protected VehicleEncodingManager encodingManager;

    private boolean fullyLoaded = false;

    ExtendedGraphHopper(VehicleEncodingManager encoding, GraphLoader graphLoader, GraphhopperToDatabaseMapper mapper) {
        this.graphLoader = graphLoader;
        this.mapper = mapper;
        this.encodingManager = encoding;

        setProfiles(encoding.getAllProfiles());
    }

    @Override
    public EncodingManager getEncodingManager() {
        return encodingManager.getEncodingManager();
    }

    @Override
    public GraphHopper importOrLoad() {
        fullyLoaded = false;

        setBaseGraph(new BaseGraph
                .Builder(getEncodingManager())
                .setDir(new RAMDirectory())
                .set3D(false)
                .withTurnCosts(getEncodingManager().needsTurnCostsSupport())
                .setSegmentSize(-1)
                .build()
        );

        importDB(getGraphHopperLocation());
        fullyLoaded = true;
        return this;
    }

    @Override
    protected WeightingFactory createWeightingFactory() {
        return (profile, hints, b) -> {
            final VehicleEncoding vehicleEncoding = encodingManager.getVehicleEncoding(profile.getVehicle());

            final TurnCostsProvider turnCostProvider = new TurnCostsProvider(vehicleEncoding, getBaseGraph().getTurnCostStorage());
            if (!hints.getBool(WEIGHTING_TURN_COSTS, false)) {
                turnCostProvider.disableTurnCosts();
            }
            final GraphhopperToDatabaseMapper graphMapper = hints.getObject(WEIGHTING_GRAPH_MAPPER, null);
            final RoutingCostFunction costFunction = hints.getObject(WEIGHTING_COST_FUNCTION, RoutingCostFunction.Default);
            return new GraphHopperWeighting(vehicleEncoding, encodingManager.wayType(), turnCostProvider, graphMapper)
                    .setRoutingCostFunction(costFunction);
        };
    }

    private void importDB(String ignore) {
        if (getBaseGraph() == null) {
            throw new IllegalStateException("Load or init graph before import database");
        }

        if (mapper == null) {
            throw new IllegalStateException("A mapper is required for importing database");
        }

        logger.info("start creating graph from database");

        graphLoader.initialize(getBaseGraph(), encodingManager, mapper);
        graphLoader.loadGraph();

        postProcessing(false);
        try {
            cleanUp();
        } catch (Exception e) {
            logger.warn("Could not clean up routing graph, skipping. Routing might not work as expected!", e);
        }
        getBaseGraph().flush();
    }

    @Override
    public boolean getFullyLoaded() {
        return fullyLoaded;
    }

    @Override
    public GHResponse route(GHRequest request) {
        throw new UnsupportedOperationException("Routing Logic is implemented in GraphHopperRouting.");
    }

    /* the following code has been copied and adjusted from original GraphHopper repository.
    * This was necessary, since `encodingManager` in `GraphHopper` is private, cannot be set from outside, and is used directly
    * in `buildSubnetworkRemovalJobs`. */
    @Override
    protected void cleanUp() {
        PrepareRoutingSubnetworks preparation = new PrepareRoutingSubnetworks(getBaseGraph(), buildSubnetworkRemovalJobs());
        preparation.setMinNetworkSize(200);
        preparation.setThreads(1);
        preparation.doWork();
        logger.info("nodes: " + Helper.nf(getBaseGraph().getNodes()) + ", edges: " + Helper.nf(getBaseGraph().getEdges()));
    }

    private List<PrepareRoutingSubnetworks.PrepareJob> buildSubnetworkRemovalJobs() {
        List<PrepareRoutingSubnetworks.PrepareJob> jobs = new ArrayList<>();
        for (Profile profile : getProfiles()) {
            Weighting weighting = createWeighting(profile, new PMap());
            // here we use `getEncodingManager()` instead of `encodingManager`, making this code work
            jobs.add(new PrepareRoutingSubnetworks.PrepareJob(getEncodingManager().getBooleanEncodedValue(Subnetwork.key(profile.getName())), weighting));
        }
        return jobs;
    }

}
