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

public abstract class AbstractVehicleSetSingleDoubleValue extends AbstractTraciCommand<Void> {

    /**
     * Creates a new {@link AbstractVehicleSetSingleDoubleValue}.
     *
     * @param version  The version of the traci.
     * @param variable Variable to set. Use variables defined in CommandRegisters
     */
    protected AbstractVehicleSetSingleDoubleValue(TraciVersion version, int variable) {
        super(version);
        write()
                .command(CommandChangeVehicleValue.COMMAND)
                .variable(variable)
                .writeVehicleIdParam()
                .writeDoubleParamWithType();
    }

    /**
     * Call this method to execute the command with the given arguments.
     *
     * @param traciCon  to connect to Traci.
     * @param vehicleId Id of the vehicle.
     * @param value     The value the variable should be set to
     * @throws TraciCommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(TraciConnection traciCon, String vehicleId, double value) throws TraciCommandException, InternalFederateException {
        super.execute(traciCon, vehicleId, value);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
