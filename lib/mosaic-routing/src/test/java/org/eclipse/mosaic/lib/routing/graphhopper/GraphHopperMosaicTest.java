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

package org.eclipse.mosaic.lib.routing.graphhopper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.routing.graphhopper.algorithm.BellmanFordRouting;
import org.eclipse.mosaic.lib.routing.graphhopper.algorithm.DijkstraCamvitChoiceRouting;
import org.eclipse.mosaic.lib.routing.graphhopper.algorithm.RoutingAlgorithmFactory;
import org.eclipse.mosaic.lib.routing.graphhopper.extended.ExtendedGHRequest;
import org.eclipse.mosaic.lib.routing.graphhopper.extended.ExtendedGHRequest.WeightingFactory;
import org.eclipse.mosaic.lib.routing.graphhopper.extended.ExtendedGHResponse;
import org.eclipse.mosaic.lib.routing.graphhopper.extended.ExtendedGraphHopper;
import org.eclipse.mosaic.lib.routing.graphhopper.junit.TestGraphRule;
import org.eclipse.mosaic.lib.routing.graphhopper.util.GHListHelper;
import org.eclipse.mosaic.lib.routing.graphhopper.util.GraphhopperToDatabaseMapper;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.util.TurnCostEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.TurnCostExtension;
import com.graphhopper.util.shapes.GHPoint;
import org.junit.Test;

import java.util.List;

public class GraphHopperMosaicTest {

    @Test
    public void importFromTestGraph() {
        ExtendedGraphHopper gh = new ExtendedGraphHopper(new TestGraphRule(null), new GraphhopperToDatabaseMapper());
        gh.importOrLoad();

        assertFalse("Contraction Hierarchies should be disabled", gh.isCHEnabled());
        assertTrue("Graph is not from instance GraphHopperStorage", gh.getGraphHopperStorage() instanceof GraphHopperStorage);

        assertTrue("The default CAR encoder should implement TurnCostEncoder",
                gh.getEncodingManager().getEncoder("CAR") instanceof TurnCostEncoder);

        GraphExtension extStorage = gh.getGraphHopperStorage().getExtension();
        assertTrue("Extended storage of graph is not from instance TurnCostStorage", extStorage instanceof TurnCostExtension);
        assertTrue("Extended storage must offer an additional edge field", extStorage.isRequireEdgeField());

        assertEquals("Not all nodes have been imported.", 14, gh.getGraphHopperStorage().getNodes());

    }

    @Test(expected = IllegalStateException.class)
    public void extendedGHRequestRequiredForRouting() {
        ExtendedGraphHopper gh = new ExtendedGraphHopper(null, null);
        gh.route(new GHRequest((GHPoint) null, (GHPoint) null));
    }

    @Test
    public void routeRequest_alternativeRoutesWithDijkstra() {
        //setup
        final ExtendedGraphHopper gh = new ExtendedGraphHopper(new TestGraphRule(null), new GraphhopperToDatabaseMapper());
        gh.importOrLoad();

        ExtendedGHRequest request = new ExtendedGHRequest(new GHPoint(0, 0), new GHPoint(0.03, 0.03));
        request.setWeightingFactory(new WeightingFactory() {

            @Override
            public Weighting create(Graph graph) {
                return new FastestWeighting(gh.getEncodingManager().getEncoder("CAR"));
            }
        });
        request.setRoutingAlgorithmFactory(new RoutingAlgorithmFactory() {

            @Override
            public RoutingAlgorithm createAlgorithm(Graph graph, Weighting weighting) {
                return new DijkstraCamvitChoiceRouting(graph, weighting);
            }
        });
        request.setVehicle("CAR");
        request.setAlternatives(2);

        //run
        GHResponse response = gh.route(request);

        //assert
        assertTrue("response should be from instance ExtendedGHResponse", response instanceof ExtendedGHResponse);
        assertTrue(response.getErrors().isEmpty());

        ExtendedGHResponse extResponse = (ExtendedGHResponse) response;
        Path p = extResponse.getPath();
        assertNotNull(p);
        assertEquals(GHListHelper.createTList(0, 1, 5, 10), p.calcNodes());
        assertEquals(5000, p.getDistance(), 0.1);
        assertEquals(300000, p.getTime());

        List<ExtendedGHResponse> alternatives = extResponse.getAdditionalRoutes();
        assertNotNull(alternatives);
        assertEquals(2, alternatives.size());

        p = alternatives.get(0).getPath();
        assertNotNull(p);
        assertEquals(GHListHelper.createTList(0, 4, 7, 9, 10), p.calcNodes());
        assertEquals(5500, p.getDistance(), 0.1);
        assertEquals(330000, p.getTime());

        p = alternatives.get(1).getPath();
        assertNotNull(p);
        assertEquals(GHListHelper.createTList(0, 1, 2, 6, 10), p.calcNodes());
        assertEquals(5600, p.getDistance(), 0.1);
        assertEquals(336000, p.getTime());

    }

    @Test
    public void routeRequest_fastestWithBellmanFord() {
        //setup
        final ExtendedGraphHopper gh = new ExtendedGraphHopper(new TestGraphRule(null), new GraphhopperToDatabaseMapper());
        gh.importOrLoad();

        ExtendedGHRequest request = new ExtendedGHRequest(new GHPoint(0, 0), new GHPoint(0.03, 0.03));
        request.setWeightingFactory((graph) -> new FastestWeighting(gh.getEncodingManager().getEncoder("CAR")));
        request.setRoutingAlgorithmFactory(BellmanFordRouting::new);
        request.setVehicle("CAR");

        //run
        GHResponse response = gh.route(request);

        //assert
        assertTrue("response should be from instance ExtendedGHResponse", response instanceof ExtendedGHResponse);
        assertTrue(response.getErrors().isEmpty());

        ExtendedGHResponse extResponse = (ExtendedGHResponse) response;
        Path p = extResponse.getPath();
        assertNotNull(p);
        assertEquals(GHListHelper.createTList(0, 1, 5, 10), p.calcNodes());
        assertEquals(5000, p.getDistance(), 0.1);
        assertEquals(300000, p.getTime());
    }

}
