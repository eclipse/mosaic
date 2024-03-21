/*
 * Copyright (c) 2023 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.routing.graphhopper.util;

import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.weighting.TurnCostProvider;
import com.graphhopper.storage.TurnCostStorage;
import com.graphhopper.util.EdgeIterator;

public class OptionalTurnCostProvider implements TurnCostProvider {

    private final BooleanEncodedValue turnRestrictionsEnc;
    private final DecimalEncodedValue turnCostsEnc;
    private final TurnCostStorage turnCostStorage;

    private boolean turnCostsEnabled = true;

    public OptionalTurnCostProvider(VehicleEncoding encoding, TurnCostStorage turnCostStorage) {
        if (turnCostStorage == null) {
            throw new IllegalArgumentException("No storage set to calculate turn weight");
        }
        this.turnRestrictionsEnc = encoding.turnRestriction();
        this.turnCostsEnc = encoding.turnCost();
        this.turnCostStorage = turnCostStorage;
    }

    /**
     * Disables consideration of turn costs. Only turn restrictions are checked.
     */
    public TurnCostProvider disableTurnCosts() {
        this.turnCostsEnabled = false;
        return this;
    }

    @Override
    public double calcTurnWeight(int edgeFrom, int nodeVia, int edgeTo) {
        if (!EdgeIterator.Edge.isValid(edgeFrom) || !EdgeIterator.Edge.isValid(edgeTo)) {
            return 0;
        }
        if (turnRestrictionsEnc != null) {
            boolean restricted = turnCostStorage.get(turnRestrictionsEnc, edgeFrom, nodeVia, edgeTo);
            if (restricted) {
                return Double.POSITIVE_INFINITY;
            }
        }
        if (turnCostsEnc != null && turnCostsEnabled) {
            return turnCostStorage.get(turnCostsEnc, edgeFrom, nodeVia, edgeTo);
        }
        return 0;
    }

    @Override
    public long calcTurnMillis(int inEdge, int viaNode, int outEdge) {
        return (long) (1000 * calcTurnWeight(inEdge, viaNode, outEdge));
    }

    @Override
    public String toString() {
        return "mosaic_tcp";
    }
}
