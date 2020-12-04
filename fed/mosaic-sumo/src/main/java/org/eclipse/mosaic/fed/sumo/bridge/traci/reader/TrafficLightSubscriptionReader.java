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

import org.eclipse.mosaic.fed.sumo.bridge.api.complex.TrafficLightSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveTrafficLightValue;
import org.eclipse.mosaic.rti.TIME;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts a TraCI response to a subscribed traffic light into a traffic light subscription result.
 **/
public class TrafficLightSubscriptionReader extends AbstractSubscriptionTraciReader<TrafficLightSubscriptionResult> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    TrafficLightSubscriptionResult createSubscriptionResult(String id) {
        TrafficLightSubscriptionResult result = new TrafficLightSubscriptionResult();
        result.id = id; // id of a traffic light group
        return result;
    }

    /**
     * This method enables to handle the subscription variables of a traffic light group.
     *
     * @param result   The result of the lane area.
     * @param varId    The Id of the variable.
     * @param varValue The value of the variable.
     */
    protected void handleSubscriptionVariable(TrafficLightSubscriptionResult result, int varId, Object varValue) {
        switch (varId) {
            case CommandRetrieveTrafficLightValue.VAR_CURRENT_PROGRAM:
                result.currentProgramId = (String) varValue;
                break;
            case CommandRetrieveTrafficLightValue.VAR_CURRENT_PHASE_INDEX:
                result.currentPhaseIndex = (int) varValue;
                break;
            case CommandRetrieveTrafficLightValue.VAR_TIME_OF_NEXT_SWITCH:
                result.assumedNextPhaseSwitchTime = (long) ((double) varValue * TIME.SECOND); //s -> ns
                break;
            case CommandRetrieveTrafficLightValue.VAR_CURRENT_STATE:
                result.currentStateEncoded = (String) varValue;
                break;
            default:
                log.warn("Unknown subscription variable {}. Skipping.", String.format("%02X ", varId));
        }
    }

}
