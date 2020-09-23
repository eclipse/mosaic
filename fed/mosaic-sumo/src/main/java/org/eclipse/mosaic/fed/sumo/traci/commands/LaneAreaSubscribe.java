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

package org.eclipse.mosaic.fed.sumo.traci.commands;

import static org.eclipse.mosaic.fed.sumo.traci.constants.CommandRetrieveLaneAreaState.VAR_LAST_STEP_HALTING_VEHICLE_NUMBER;
import static org.eclipse.mosaic.fed.sumo.traci.constants.CommandRetrieveLaneAreaState.VAR_LAST_STEP_MEAN_SPEED;
import static org.eclipse.mosaic.fed.sumo.traci.constants.CommandRetrieveLaneAreaState.VAR_LAST_STEP_VEHICLE_IDS;
import static org.eclipse.mosaic.fed.sumo.traci.constants.CommandRetrieveLaneAreaState.VAR_LAST_STEP_VEHICLE_NUMBER;
import static org.eclipse.mosaic.fed.sumo.traci.constants.CommandRetrieveLaneAreaState.VAR_LENGTH;

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciCommand;
import org.eclipse.mosaic.fed.sumo.traci.CommandRegister;
import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.TraciConnection;
import org.eclipse.mosaic.fed.sumo.traci.TraciVersion;
import org.eclipse.mosaic.fed.sumo.traci.complex.Status;
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandVariableSubscriptions;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.Arrays;
import java.util.Collection;

public class LaneAreaSubscribe extends AbstractTraciCommand<Void> {

    /**
     * Default constructor used in TraciSimulationFacade.
     * Called by {@link CommandRegister#getOrCreate(java.lang.Class)}.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Object_Variable_Subscription.html">Subscriptions</a>
     */
    @SuppressWarnings("WeakerAccess")
    public LaneAreaSubscribe() {
        this(Arrays.asList(
                VAR_LENGTH,
                VAR_LAST_STEP_VEHICLE_NUMBER,
                VAR_LAST_STEP_MEAN_SPEED,
                VAR_LAST_STEP_HALTING_VEHICLE_NUMBER,
                VAR_LAST_STEP_VEHICLE_IDS
        ));
    }

    /**
     * Creates a new {@link LaneAreaSubscribe} object.
     * Access needs to be public, because command is called using Reflection.
     *
     * @param subscriptionCodes variables to subscribe to
     */
    @SuppressWarnings("WeakerAccess")
    public LaneAreaSubscribe(Collection<Integer> subscriptionCodes) {
        super(TraciVersion.LOWEST);

        TraciCommandWriterBuilder write = write()
                .command(CommandVariableSubscriptions.COMMAND_SUBSCRIBE_LANE_AREA_VALUES)
                .writeDoubleParam() // start time
                .writeDoubleParam() // end time
                .writeStringParam() // Object laneId
                .writeByte(subscriptionCodes.size());

        for (Integer subscriptionCode : subscriptionCodes) {
            write.writeByte(subscriptionCode);
        }

        read()
                .expectByte(CommandVariableSubscriptions.RESPONSE_SUBSCRIBE_LANE_AREA_VALUES)
                .skipString()
                .expectByte(subscriptionCodes.size())
                .skipRemaining();
    }

    /**
     * This method executes the command with the given arguments.
     *
     * @param traciCon   Connection to Traci.
     * @param laneAreaId Id of the lane area.
     * @param startTime  start time to subscribe.
     * @param endTime    end time of subscribe.
     * @throws TraciCommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(TraciConnection traciCon, String laneAreaId, long startTime, long endTime) throws TraciCommandException, InternalFederateException {
        super.execute(traciCon, ((double) startTime) / TIME.SECOND, ((double) endTime) / TIME.SECOND, laneAreaId);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
