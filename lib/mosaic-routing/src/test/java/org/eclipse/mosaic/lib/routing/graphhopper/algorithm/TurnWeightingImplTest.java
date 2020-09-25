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

import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.lib.routing.graphhopper.junit.TestGraphRule;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TurnCostEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import org.junit.Rule;
import org.junit.Test;

public class TurnWeightingImplTest {

    private EncodingManager encManager = EncodingManager.create(new CarFlagEncoder(5, 5, 127));

    @Rule
    public TestGraphRule testGraph = new TestGraphRule(encManager);

    @Test
    public void fastest_noTurnCosts() {
        FlagEncoder enc = encManager.getEncoder("CAR");

        TurnWeighting w = new TurnWeighting(new FastestWeighting(enc), testGraph.getTurnCostStorage());

        EdgeExplorer expl = testGraph.getGraph().createEdgeExplorer();
        EdgeIterator it = expl.setBaseNode(0);

        double distance = it.getDistance();

        double weight = w.calcWeight(it, false, 0);
        double turnWeight = w.calcTurnWeight(1, 0, 0);

        assertEquals(distance / enc.getMaxSpeed(), weight, 0.1d);
        assertEquals(0, turnWeight, 0.1d);
    }

    @Test
    public void shortest_noTurnCosts() {
        FlagEncoder enc = encManager.getEncoder("CAR");

        TurnWeighting w = new TurnWeighting(new ShortestWeighting(enc), testGraph.getTurnCostStorage());

        EdgeExplorer expl = testGraph.getGraph().createEdgeExplorer();
        EdgeIterator it = expl.setBaseNode(0);

        double distance = it.getDistance();

        double weight = w.calcWeight(it, false, 0);
        double turnWeight = w.calcTurnWeight(1, 0, 0);

        assertEquals(distance, weight, 0.1d);
        assertEquals(0, turnWeight, 0.1d);
    }


    @Test
    public void shortest_turnCosts() {
        FlagEncoder enc = encManager.getEncoder("CAR");

        testGraph.getTurnCostStorage().addTurnInfo(1, 0, 0, ((TurnCostEncoder) enc).getTurnFlags(false, 10));

        TurnWeighting w = new TurnWeighting(new ShortestWeighting(enc), testGraph.getTurnCostStorage());

        EdgeExplorer expl = testGraph.getGraph().createEdgeExplorer();
        EdgeIterator it = expl.setBaseNode(0);

        double distance = it.getDistance();

        double weight = w.calcWeight(it, false, 0);
        double turnWeight = w.calcTurnWeight(1, 0, 0);

        assertEquals(distance, weight, 0.1d);
        assertEquals(10, turnWeight, 0.1d);
    }

    @Test
    public void fastest_turnCosts() {
        FlagEncoder enc = encManager.getEncoder("CAR");

        testGraph.getTurnCostStorage().addTurnInfo(1, 0, 0, ((TurnCostEncoder) enc).getTurnFlags(false, 10));

        TurnWeighting w = new TurnWeighting(new FastestWeighting(enc), testGraph.getTurnCostStorage());

        EdgeExplorer expl = testGraph.getGraph().createEdgeExplorer();
        EdgeIterator it = expl.setBaseNode(0);

        double distance = it.getDistance();

        double weight = w.calcWeight(it, false, 0);
        double turnWeight = w.calcTurnWeight(1, 0, 0);

        assertEquals(distance / enc.getMaxSpeed(), weight, 0.1d);
        assertEquals(10, turnWeight, 0.1d);
    }

    @Test
    public void shortest_turnRestriction() {
        FlagEncoder enc = encManager.getEncoder("CAR");

        testGraph.getTurnCostStorage().addTurnInfo(1, 0, 0, ((TurnCostEncoder) enc).getTurnFlags(true, 0));

        TurnWeighting w = new TurnWeighting(new ShortestWeighting(enc), testGraph.getTurnCostStorage());

        EdgeExplorer expl = testGraph.getGraph().createEdgeExplorer();
        EdgeIterator it = expl.setBaseNode(0);

        double distance = it.getDistance();

        double weight = w.calcWeight(it, false, 0);
        double turnWeight = w.calcTurnWeight(1, 0, 0);

        assertEquals(distance, weight, 0.1d);
        assertEquals(Double.POSITIVE_INFINITY, turnWeight, 0.1d);
    }

    @Test
    public void fastest_turnRestriction() {
        FlagEncoder enc = encManager.getEncoder("CAR");

        testGraph.getTurnCostStorage().addTurnInfo(1, 0, 0, ((TurnCostEncoder) enc).getTurnFlags(true, 0));

        TurnWeighting w = new TurnWeighting(new FastestWeighting(enc), testGraph.getTurnCostStorage());

        EdgeExplorer expl = testGraph.getGraph().createEdgeExplorer();
        EdgeIterator it = expl.setBaseNode(0);

        double distance = it.getDistance();

        double weight = w.calcWeight(it, false, 0);
        double turnWeight = w.calcTurnWeight(1, 0, 0);

        assertEquals(distance / enc.getMaxSpeed(), weight, 0.1d);
        assertEquals(Double.POSITIVE_INFINITY, turnWeight, 0.1d);
    }

}
