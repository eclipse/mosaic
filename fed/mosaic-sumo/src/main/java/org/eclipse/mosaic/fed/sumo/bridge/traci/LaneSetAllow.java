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

import static org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandLanePropertyChange.VAR_ALLOWED;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.TraciVersion;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.Status;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandVariableSubscriptions;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.List;

/**
 * This class represents the SUMO command which sets the allowed classes to a specific lane.
 */
public class LaneSetAllow
        extends AbstractTraciCommand<Void>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.LaneSetAllow {

    /**
     * Creates a new {@link LaneSetAllow} traci command, which will
     * allow all given vehicle classes on the given lane-id once executed.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Lane_State.html">Lane State Change</a>
     */
    public LaneSetAllow() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandVariableSubscriptions.COMMAND_CHANGE_LANE_STATE)
                .variable(VAR_ALLOWED)
                .writeStringParam() // Lane id
                .writeStringListParamWithType(); // List of allowed vClasses with the type written beforehand
    }

    /**
     * This method executes the command with the given arguments and sets the allowed vehicle classes vClasses.
     *
     * @param bridge          Connection to SUMO.
     * @param laneId          Id of the lane.
     * @param allowedVClasses Vehicle classes to allow.
     * @throws CommandException          if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(Bridge bridge, String laneId, List<String> allowedVClasses) throws CommandException, InternalFederateException {
        super.execute(bridge, laneId, allowedVClasses);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
