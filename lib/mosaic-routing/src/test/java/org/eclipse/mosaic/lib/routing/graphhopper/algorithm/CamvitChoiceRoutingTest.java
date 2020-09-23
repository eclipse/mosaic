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
import com.graphhopper.routing.util.TurnCostEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class CamvitChoiceRoutingTest {
    /**
     * Run tests with both implementations: Djikstra and A*
     */
    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][]{
                {(TestAlgorithmPreparation) DijkstraCamvitChoiceRouting::new},
                {(TestAlgorithmPreparation) AStarCamvitChoiceRouting::new}}
        );
    }

    private TestAlgorithmPreparation algoPreparation;

    private EncodingManager encManager = EncodingManager.create(
            new CarFlagEncoder(5, 5, 127),
            new BikeFlagEncoder(),
            new FootFlagEncoder()
    );

    @Rule
    public TestGraphRule testGraph = new TestGraphRule(encManager);

    public CamvitChoiceRoutingTest(TestAlgorithmPreparation algoPreparation) {
        this.algoPreparation = algoPreparation;
    }

    /**
     * Calculates two alternatives paths using the test graph
     */
    @Test
    public void calculateAlternativePaths_withTurnCost() {
        GraphHopperStorage g = testGraph.getGraph();
        FlagEncoder e = encManager.getEncoder("CAR");
        TurnWeighting w = new TurnWeighting(new FastestWeighting(e), testGraph.getTurnCostStorage());

        //2 seconds turn costs for turn  (0-1)->(1-2)
        testGraph.getTurnCostStorage().addTurnInfo(0, 1, 2, ((TurnCostEncoder) e).getTurnFlags(false, 124));

        //run
        AlternativeRoutesRoutingAlgorithm algo = algoPreparation.createAlgo(g, w);

        algo.setRequestAlternatives(2);

        Path p = algo.calcPath(0, 10);

        //assert shortest
        assertEquals(GHListHelper.createTList(0, 1, 5, 10), p.calcNodes());
        assertEquals(5000, p.getDistance(), 0.1);

        List<Path> paths = algo.getAlternativePaths();
        assertEquals(2, paths.size());

        p = paths.get(0);
        assertEquals(GHListHelper.createTList(0, 4, 7, 9, 10), p.calcNodes());
        assertEquals(5500, p.getDistance(), 0.1);

        p = paths.get(1);
        assertEquals(GHListHelper.createTList(0, 1, 5, 6, 10), p.calcNodes());
        assertEquals(6000, p.getDistance(), 0.1);
    }

    /**
     * Calculates two alternatives paths using the test graph
     * considering turn costs
     */
    @Test
    public void calculateAlternativePaths() {
        Graph g = testGraph.getGraph();
        FlagEncoder e = encManager.getEncoder("CAR");
        Weighting w = new FastestWeighting(e);

        AlternativeRoutesRoutingAlgorithm algo = algoPreparation.createAlgo(g, w);

        algo.setRequestAlternatives(2);

        Path p = algo.calcPath(0, 10);

        //assert shortest
        assertEquals(GHListHelper.createTList(0, 1, 5, 10), p.calcNodes());
        assertEquals(5000, p.getDistance(), 0.1);

        List<Path> paths = algo.getAlternativePaths();
        assertEquals(2, paths.size());

        p = paths.get(0);
        assertEquals(GHListHelper.createTList(0, 4, 7, 9, 10), p.calcNodes());
        assertEquals(5500, p.getDistance(), 0.1);

        p = paths.get(1);
        assertEquals(GHListHelper.createTList(0, 1, 2, 6, 10), p.calcNodes());
        assertEquals(5600, p.getDistance(), 0.1);
    }

    /**
     * Calculates only the best path using ChoiceRouting
     */
    @Test
    public void calculateBestPath() {
        Graph g = testGraph.getGraph();
        FlagEncoder e = encManager.getEncoder("CAR");
        Weighting w = new FastestWeighting(e);

        AlternativeRoutesRoutingAlgorithm algo = algoPreparation.createAlgo(g, w);

        algo.setRequestAlternatives(0);

        Path p = algo.calcPath(0, 10);

        //assert shortest
        assertEquals(GHListHelper.createTList(0, 1, 5, 10), p.calcNodes());
        assertEquals(5000, p.getDistance(), 0.1);

        List<Path> paths = algo.getAlternativePaths();
        assertEquals(0, paths.size());
    }

    interface TestAlgorithmPreparation {
        AlternativeRoutesRoutingAlgorithm createAlgo(Graph g, Weighting w);
    }
}
