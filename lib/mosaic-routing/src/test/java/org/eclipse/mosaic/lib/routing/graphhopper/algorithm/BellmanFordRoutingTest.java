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

import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.lib.routing.graphhopper.junit.TestGraphRule;
import org.eclipse.mosaic.lib.routing.graphhopper.util.GHListHelper;

import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.BikeFlagEncoder;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.FootFlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.TurnCostExtension;
import org.junit.Rule;
import org.junit.Test;

public class BellmanFordRoutingTest {

    private EncodingManager encManager = EncodingManager.create(
            new CarFlagEncoder(5, 5, 127),
            new BikeFlagEncoder(),
            new FootFlagEncoder()
    );

    @Rule
    public TestGraphRule testGraph = new TestGraphRule(encManager);

    @Test
    public void calculateFastestPath() {
        GraphHopperStorage g = testGraph.getGraph();
        FlagEncoder e = encManager.getEncoder("CAR");
        Weighting w = new FastestWeighting(e);


        //run
        Path p = new BellmanFordRouting(g, w).calcPath(0, 10);

        //assert shortest
        assertEquals(GHListHelper.createTList(0, 1, 5, 10), p.calcNodes());
        assertEquals(5000, p.getDistance(), 0.1);
    }

    @Test
    public void calculateFastestPath_turnCosts() {
        GraphHopperStorage g = testGraph.getGraph();
        FlagEncoder e = encManager.getEncoder("CAR");
        Weighting w = new TurnWeighting(new FastestWeighting(e), (TurnCostExtension) g.getExtension());

        //add expensive turn at (0-1)->(1,5)
        testGraph.getTurnCostStorage().addTurnInfo(0, 1, 3, e.getTurnFlags(false, 124));

        //run
        Path p = new BellmanFordRouting(g, w).calcPath(0, 10);

        //assert shortest
        assertEquals(GHListHelper.createTList(0, 4, 7, 9, 10), p.calcNodes());
        assertEquals(5500, p.getDistance(), 0.1);
    }

    @Test
    public void calculateShortestPath() {
        GraphHopperStorage g = testGraph.getGraph();
        FlagEncoder e = encManager.getEncoder("CAR");
        Weighting w = new ShortestWeighting(e);

        //run
        Path p = new BellmanFordRouting(g, w).calcPath(0, 10);

        //assert shortest
        assertEquals(GHListHelper.createTList(0, 1, 5, 10), p.calcNodes());
        assertEquals(5000, p.getDistance(), 0.1);
    }


}