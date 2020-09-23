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

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciCommand;
import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.TraciConnection;
import org.eclipse.mosaic.fed.sumo.traci.TraciVersion;
import org.eclipse.mosaic.fed.sumo.traci.complex.Status;
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandChangeVehicleValue;
import org.eclipse.mosaic.rti.api.InternalFederateException;

/**
 * Class to set set a single integer value via a traci command.
 */
public abstract class AbstractVehicleSetSingleIntegerValue extends AbstractTraciCommand<Void> {

    /**
     * Creates a {@link AbstractVehicleSetSingleIntegerValue} traci command.
     * Will write the single int-variable to traci.
     * Access needs to be public, because command is called using Reflection.
     *
     * @param version  used traci version
     * @param variable variable to set
     */
    protected AbstractVehicleSetSingleIntegerValue(TraciVersion version, int variable) {
        super(version);

        write()
                .command(CommandChangeVehicleValue.COMMAND)
                .variable(variable)
                .writeVehicleIdParam()
                .writeIntParamWithType();
    }

    public void execute(TraciConnection traciCon, String vehicleId, int value) throws TraciCommandException, InternalFederateException {
        super.execute(traciCon, vehicleId, value);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
