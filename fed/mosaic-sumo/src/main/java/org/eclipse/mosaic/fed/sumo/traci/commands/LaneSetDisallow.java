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

import static org.eclipse.mosaic.fed.sumo.traci.constants.CommandLanePropertyChange.VAR_DISALLOWED;

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciCommand;
import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.TraciConnection;
import org.eclipse.mosaic.fed.sumo.traci.TraciVersion;
import org.eclipse.mosaic.fed.sumo.traci.complex.Status;
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandVariableSubscriptions;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.List;

/**
 * This class represents the traci command which sets the disallowed classes to a specific lane.
 */
public class LaneSetDisallow extends AbstractTraciCommand<Void> {

    /**
     * Creates a new {@link LaneSetDisallow} traci command, which will
     * disallow all given vehicle classes on the given lane-id once executed.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Lane_State.html">Lane State Change</a>
     */
    public LaneSetDisallow() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandVariableSubscriptions.COMMAND_CHANGE_LANE_STATE)
                .variable(VAR_DISALLOWED)
                .writeStringParam() // Lane id
                .writeStringListParamWithType(); // List of disallowed vClasses with the type written beforehand
    }

    /**
     * This method executes the command with the given arguments and sets the disallowed classes.
     *
     * @param traciCon           Connection to Traci.
     * @param laneId             Id of the lane.
     * @param disallowedVClasses Vehicle classes to disallow.
     * @throws TraciCommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(TraciConnection traciCon, String laneId, List<String> disallowedVClasses) throws TraciCommandException, InternalFederateException {
        super.execute(traciCon, laneId, disallowedVClasses);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
