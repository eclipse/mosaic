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

package org.eclipse.mosaic.lib.routing.graphhopper.util;

import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.lib.routing.graphhopper.junit.TestGraphRule;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TurnCostEncoder;
import com.graphhopper.storage.TurnCostExtension;
import org.junit.Rule;
import org.junit.Test;

public class TurnCostAnalyzerTest {

    private EncodingManager encManager = EncodingManager.create(new CarFlagEncoder(5, 5, 127));

    @Rule
    public TestGraphRule testGraph = new TestGraphRule(encManager);

    @Test
    public void turnCostsCalculation() {
        //run
        new TurnCostAnalyzer().createTurnCostsForCars(testGraph.getGraph(), encManager.getEncoder("CAR"));

        //assert
        assertEquals(4d, getTurnCosts(1, 0, 0), 0.4d); //4 seconds for 90deg right turn

        assertEquals(2d, getTurnCosts(2, 2, 6), 0.4d); //2 seconds for a slight 45deg right turn

        assertEquals(6d, getTurnCosts(0, 1, 4), 0.4d); //6 seconds for a hard 120deg right turn

        assertEquals(23d, getTurnCosts(0, 0, 1), 0.4d); //23 seconds for a 90deg left turn

    }

    private double getTurnCosts(int fromEdge, int viaNode, int toEdge) {
        TurnCostEncoder enc = (TurnCostEncoder) (encManager.getEncoder("CAR"));
        TurnCostExtension tcS = testGraph.getTurnCostStorage();
        long flags = tcS.getTurnCostFlags(fromEdge, viaNode, toEdge);
        if (enc.isTurnRestricted(flags)) {
            return Double.MAX_VALUE;
        }
        return enc.getTurnCost(flags);
    }
}
