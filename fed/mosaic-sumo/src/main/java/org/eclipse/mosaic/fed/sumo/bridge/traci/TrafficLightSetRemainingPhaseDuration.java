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

public class TrafficLightSetRemainingPhaseDuration
        extends AbstractTraciCommand<Void>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.TrafficLightSetRemainingPhaseDuration {

    /**
     * Creates a {@link TrafficLightSetRemainingPhaseDuration} traci command.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Traffic_Lights_State.html">Traffic Lights State Change</a>
     */
    @SuppressWarnings("WeakerAccess")
    public TrafficLightSetRemainingPhaseDuration() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandChangeTrafficLightState.COMMAND)
                .variable(CommandChangeTrafficLightState.VAR_PHASE_DURATION)
                .writeStringParam()
                .writeDoubleParamWithType();
    }

    public void execute(Bridge con, String tlId, double remainingDurationS) throws CommandException, InternalFederateException {
        super.execute(con, tlId, remainingDurationS);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
