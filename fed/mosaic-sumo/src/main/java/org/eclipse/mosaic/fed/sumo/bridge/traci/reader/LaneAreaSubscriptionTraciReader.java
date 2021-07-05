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

package org.eclipse.mosaic.fed.sumo.bridge.traci.reader;

import org.eclipse.mosaic.fed.sumo.bridge.api.complex.LaneAreaSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveLaneAreaState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LaneAreaSubscriptionTraciReader extends AbstractSubscriptionTraciReader<LaneAreaSubscriptionResult> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    LaneAreaSubscriptionResult createSubscriptionResult(String id) {
        LaneAreaSubscriptionResult result = new LaneAreaSubscriptionResult();
        result.id = id;
        return result;
    }

    /**
     * This method enables to handle the subscription variable of the LaneArea.
     *
     * @param result   The result of the lane area.
     * @param varId    The Id of the variable.
     * @param varValue The value of the variable.
     */
    protected void handleSubscriptionVariable(LaneAreaSubscriptionResult result, int varId, Object varValue) {
        switch (varId) {
            case CommandRetrieveLaneAreaState.VAR_LENGTH:
                result.length = (double) varValue;
                break;
            case CommandRetrieveLaneAreaState.VAR_LAST_STEP_VEHICLE_NUMBER:
                result.vehicleCount = (int) varValue;
                break;
            case CommandRetrieveLaneAreaState.VAR_LAST_STEP_MEAN_SPEED:
                result.meanSpeed = (double) varValue;
                break;
            case CommandRetrieveLaneAreaState.VAR_LAST_STEP_HALTING_VEHICLE_NUMBER:
                result.haltingVehicles = (int) varValue;
                break;
            case CommandRetrieveLaneAreaState.VAR_LAST_STEP_VEHICLE_IDS:
                result.vehicles = (List<String>) varValue;
                break;
            default:
                log.warn("Unknown subscription variable {}. Skipping.", String.format("%02X ", varId));
        }
    }
}