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

package org.eclipse.mosaic.fed.sumo.bridge.libsumo;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.rti.api.InternalFederateException;

/**
 * This class represents the traci command which allows to subscribe the traffic light to the application.
 * For more information check https://sumo.dlr.de/docs/TraCI/Object_Variable_Subscription.html
 */
public class TrafficLightSubscribe implements org.eclipse.mosaic.fed.sumo.bridge.api.TrafficLightSubscribe {
    /**
     * This method executes the command with the given arguments in order to subscribe the traffic light group to the application.
     *
     * @param traciCon            Connection to Traci.
     * @param trafficLightGroupId The id of the traffic light group.
     * @param startTime           The time to subscribe the traffic light group.
     * @param endTime             The end time of the subscription of the traffic light group in the application.
     * @throws CommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(Bridge traciCon, String trafficLightGroupId, long startTime, long endTime) {
        SimulationSimulateStep.TRAFFIC_LIGHT_SUBSCRIPTIONS.add(trafficLightGroupId);
    }
}
