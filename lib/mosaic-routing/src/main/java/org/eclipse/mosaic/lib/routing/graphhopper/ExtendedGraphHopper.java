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

import org.eclipse.mosaic.lib.routing.graphhopper.util.GraphhopperToDatabaseMapper;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of GraphHopper which is able to import map data from the MOSAIC scenario database.
 * Routing functionality is implemented {@link org.eclipse.mosaic.lib.routing.graphhopper.GraphHopperRouting}.
 */
class ExtendedGraphHopper extends GraphHopper {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected GraphLoader graphLoader;
    protected GraphhopperToDatabaseMapper mapper;
    protected VehicleEncodingManager encodingManager;

    private boolean fullyLoaded = false;

    ExtendedGraphHopper(VehicleEncodingManager encoding, GraphLoader graphLoader, GraphhopperToDatabaseMapper mapper) {
        this.graphLoader = graphLoader;
        this.mapper = mapper;
        this.encodingManager = encoding;
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
            logger.warn("Could not clean up routing graph, skipping. Routing might not work as expected!");
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

}
