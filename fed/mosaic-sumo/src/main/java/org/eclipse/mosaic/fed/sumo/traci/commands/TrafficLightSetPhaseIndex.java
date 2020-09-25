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

package org.eclipse.mosaic.fed.sumo.traci.commands;

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciCommand;
import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.TraciConnection;
import org.eclipse.mosaic.fed.sumo.traci.TraciVersion;
import org.eclipse.mosaic.fed.sumo.traci.complex.Status;
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandChangeTrafficLightState;
import org.eclipse.mosaic.rti.api.InternalFederateException;

public class TrafficLightSetPhaseIndex extends AbstractTraciCommand<Void> {

    /**
     * Creates a new {@link TrafficLightSetPhaseIndex} object.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Traffic_Lights_State.html">Traffic Lights State Change</a>
     */
    public TrafficLightSetPhaseIndex() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandChangeTrafficLightState.COMMAND)
                .variable(CommandChangeTrafficLightState.VAR_PHASE_INDEX)
                .writeStringParam()
                .writeIntParamWithType();
    }

    public void execute(TraciConnection con, String tlId, int phaseIndex) throws TraciCommandException, InternalFederateException {
        super.execute(con, tlId, phaseIndex);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
