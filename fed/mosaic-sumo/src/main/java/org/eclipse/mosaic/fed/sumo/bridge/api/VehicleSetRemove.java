/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.sumo.bridge.api;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.rti.api.InternalFederateException;

public interface VehicleSetRemove {
    /**
     * This method executes the command with the given arguments in order to set the remove type.
     *
     * @param bridge    Connection to SUMO.
     * @param vehicleId The Id of the vehicle.
     * @param reason    The reason for the remove the vehicle from the simulation.
     * @throws CommandException          if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The connection to SUMO is shut down.
     */
    void execute(Bridge bridge, String vehicleId, Reason reason) throws CommandException, InternalFederateException;

    /**
     * Enum class that represents the different remove type.
     */
    public enum Reason {
        TELEPORT(0),
        PARKING(1),
        ARRIVED(2),
        VAPORIZED(3),
        TELEPORT_ARRIVED(4);

        public final int reasonByte;

        Reason(int reasonByte) {
            this.reasonByte = reasonByte;
        }
    }
}
