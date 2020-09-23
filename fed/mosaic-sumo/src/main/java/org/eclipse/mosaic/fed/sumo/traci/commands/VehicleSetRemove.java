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

public class VehicleSetRemove extends AbstractTraciCommand<Void> {

    /**
     * Creates a new {@link VehicleSetRemove} object.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Vehicle_State.html">Vehicle State Change</a>
     */
    public VehicleSetRemove() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandChangeVehicleValue.COMMAND)
                .variable(CommandChangeVehicleValue.VAR_REMOVE)
                .writeVehicleIdParam()
                .writeByteParamWithType(); // reason
    }

    /**
     * This method executes the command with the given arguments in order to set the remove type.
     *
     * @param traciCon  Connection to Traci.
     * @param vehicleId The Id of the vehicle.
     * @param reason    The reason for the remove the vehicle from the simulation.
     * @throws TraciCommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(TraciConnection traciCon, String vehicleId, Reason reason) throws TraciCommandException, InternalFederateException {
        super.execute(traciCon, vehicleId, reason.reasonByte);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }

    /**
     * Enum class that represents the different remove type.
     */
    public enum Reason {
        TELEPORT(0),
        PARKING(1),
        ARRIVED(2),
        VAPORIZED(3),
        TELEPORT_ARRIVED(4);

        private final int reasonByte;

        Reason(int reasonByte) {
            this.reasonByte = reasonByte;
        }
    }
}
