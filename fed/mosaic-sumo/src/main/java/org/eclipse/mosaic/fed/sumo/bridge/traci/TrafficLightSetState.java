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

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.TraciVersion;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.Status;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandChangeTrafficLightState;
import org.eclipse.mosaic.rti.api.InternalFederateException;

public class TrafficLightSetState
        extends AbstractTraciCommand<Void>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.TrafficLightSetState {

    /**
     * Creates a {@link TrafficLightSetState} traci command.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Traffic_Lights_State.html">Traffic Lights State Change</a>
     */
    public TrafficLightSetState() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandChangeTrafficLightState.COMMAND)
                .variable(CommandChangeTrafficLightState.VAR_STATE)
                .writeStringParam()
                .writeStringParamWithType();
    }

    public void execute(Bridge bridge, String tlId, String phaseDefinition) throws CommandException, InternalFederateException {
        super.execute(bridge, tlId, phaseDefinition);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
