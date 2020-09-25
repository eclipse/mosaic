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

package org.eclipse.mosaic.fed.sumo.traci.reader;

import org.eclipse.mosaic.fed.sumo.traci.complex.InductionLoopSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.traci.complex.InductionLoopVehicleData;
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandRetrieveInductionLoopState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class InductionLoopSubscriptionTraciReader extends AbstractSubscriptionTraciReader<InductionLoopSubscriptionResult> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Creates a new {@link InductionLoopSubscriptionTraciReader} object.
     */
    @SuppressWarnings("WeakerAccess")
    public InductionLoopSubscriptionTraciReader() {
        getTypeBasedTraciReader().registerCompoundReader(new ListTraciReader<>(new InductionLoopVehicleDataTraciReader(), true));
    }

    @Override
    InductionLoopSubscriptionResult createSubscriptionResult(String id) {
        InductionLoopSubscriptionResult result = new InductionLoopSubscriptionResult();
        result.id = id;
        return result;
    }

    /**
     * This method enables to handle the subscription variable of the induction loop.
     *
     * @param result   The result of the induction loop.
     * @param varId    The Id of the variable.
     * @param varValue The value of the variable.
     */
    protected void handleSubscriptionVariable(InductionLoopSubscriptionResult result, int varId, Object varValue) {
        switch (varId) {
            case CommandRetrieveInductionLoopState.VAR_LAST_STEP_MEAN_SPEED:
                result.meanSpeed = (double) varValue;
                break;
            case CommandRetrieveInductionLoopState.VAR_LAST_STEP_MEAN_VEHICLE_LENGTH:
                result.meanVehicleLength = (double) varValue;
                break;
            case CommandRetrieveInductionLoopState.VAR_LAST_STEP_VEHICLE_DATA:
                result.vehiclesOnInductionLoop = (List<InductionLoopVehicleData>) varValue;
                break;
            default:
                log.warn("Unknown subscription variable {}. Skipping.", String.format("%02X ", varId));
        }
    }
}
