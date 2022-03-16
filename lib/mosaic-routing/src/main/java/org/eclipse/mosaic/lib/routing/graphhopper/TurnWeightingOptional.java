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

import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.TurnCostExtension;

/**
 * Extension of TurnWeighting which allows to disable
 * the consideration of turn costs while still considering turn restrictions.
 */
class TurnWeightingOptional extends TurnWeighting {

    private final boolean enableTurnCosts;
    private final TurnCostExtension tcExt;
    private final double restrictedCosts;

    /**
     * Creates a {@link TurnWeighting} extension which disables or enables the consideration of turn costs, but always
     * considers turn restrictions.
     *
     * @param superWeighting  the basic weighting implementation for road weighting
     * @param turnCostExt     the turn cost extension of the graph
     * @param enableTurnCosts if <code>true</code>, turn costs and restrictions are considered,
     *                        if <code>false</code>, only turn restrictions are considered
     * @param restrictedCosts the costs to apply if a turn is restricted (use Double.POSITIVE_INFINITY to forbid turn)
     */
    public TurnWeightingOptional(Weighting superWeighting, TurnCostExtension turnCostExt, boolean enableTurnCosts, double restrictedCosts) {
        super(superWeighting, turnCostExt);
        this.tcExt = turnCostExt;
        this.restrictedCosts = restrictedCosts;
        this.enableTurnCosts = enableTurnCosts;
    }

    @Override
    public double calcTurnWeight(int edgeFrom, int nodeVia, int edgeTo) {
        long turnFlags = tcExt.getTurnCostFlags(edgeFrom, nodeVia, edgeTo);
        if (getFlagEncoder().isTurnRestricted(turnFlags)) {
            return restrictedCosts;
        }

        if (enableTurnCosts) {
            return getFlagEncoder().getTurnCost(turnFlags);
        }
        return 0;
    }

}