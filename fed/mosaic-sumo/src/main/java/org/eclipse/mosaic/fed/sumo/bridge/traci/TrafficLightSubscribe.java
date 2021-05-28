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

package org.eclipse.mosaic.fed.sumo.bridge.traci;

import static org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveTrafficLightValue.VAR_CURRENT_PHASE_INDEX;
import static org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveTrafficLightValue.VAR_CURRENT_PROGRAM;
import static org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveTrafficLightValue.VAR_CURRENT_STATE;
import static org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveTrafficLightValue.VAR_TIME_OF_NEXT_SWITCH;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.TraciVersion;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.Status;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandVariableSubscriptions;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import com.google.common.collect.Lists;

import java.util.Collection;

/**
 * This class represents the SUMO command which allows to subscribe the traffic light to the application.
 * For more information check https://sumo.dlr.de/docs/TraCI/Object_Variable_Subscription.html
 */
public class TrafficLightSubscribe
        extends AbstractTraciCommand<Void>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.TrafficLightSubscribe {

    /**
     * Default constructor with codes of main variables.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Traffic_Lights_Value_Retrieval.html">Traffic Lights Value Retrieval</a>
     */
    @SuppressWarnings("WeakerAccess")
    public TrafficLightSubscribe() {
        this(Lists.newArrayList(
                VAR_CURRENT_PROGRAM,
                VAR_CURRENT_PHASE_INDEX,
                VAR_TIME_OF_NEXT_SWITCH,
                VAR_CURRENT_STATE
        ));
    }

    /**
     * Creates a new {@link TrafficLightSubscribe} object.
     *
     * @param subscriptionCodes Codes of traffic light group variables needed to be received after subscription.
     */
    public TrafficLightSubscribe(Collection<Integer> subscriptionCodes) {
        super(TraciVersion.LOWEST);

        TraciCommandWriterBuilder write = write()
                .command(CommandVariableSubscriptions.COMMAND_SUBSCRIBE_TRAFFIC_LIGHT_VALUES)
                .writeDoubleParam() //start time
                .writeDoubleParam() //end time
                .writeStringParam() //Id of a traffic light group
                .writeByte(subscriptionCodes.size());

        for (Integer subscriptionCode : subscriptionCodes) {
            write.writeByte(subscriptionCode);
        }

        read()
                .expectByte(CommandVariableSubscriptions.RESPONSE_SUBSCRIBE_TRAFFIC_LIGHT_VALUES)
                .skipString()
                .expectByte(subscriptionCodes.size())
                .skipRemaining();

    }

    /**
     * This method executes the command with the given arguments in order to subscribe the traffic light group to the application.
     *
     * @param bridge Connection to SUMO.
     * @param trafficLightGroupId The id of the traffic light group.
     * @param startTime           The time to subscribe the traffic light group.
     * @param endTime             The end time of the subscription of the traffic light group in the application.
     * @throws CommandException     if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(Bridge bridge, String trafficLightGroupId, long startTime, long endTime) throws CommandException, InternalFederateException {
        super.execute(bridge, ((double) startTime) / TIME.SECOND, ((double) endTime) / TIME.SECOND, trafficLightGroupId);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }

}
