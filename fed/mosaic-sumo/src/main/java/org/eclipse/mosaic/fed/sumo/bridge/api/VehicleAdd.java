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

public interface VehicleAdd {

    /**
     * This method executes the command with the given arguments in order to add a vehicle with its specific properties.
     *
     * @param bridge         Connection to SUMO.
     * @param vehicleId      Id of the vehicle.
     * @param routeId        Id of the route.
     * @param vehicleType    Type of the vehicle.
     * @param departLane     Lane of the departure. Possible values: [int] or random", "free", "allowed", "best", "first"
     * @param departPosition Position of the departure. Possible values: [int] or "random", "free", "base", "last", "random free"
     * @param departSpeed    Speed at departure. Possible Values: [double] or "max", "random"
     * @throws CommandException          If the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException If some serious error occurs during writing or reading. The connection to SUMO is shut down.
     */
    void execute(Bridge bridge, String vehicleId, String routeId, String vehicleType,
                 String departLane, String departPosition, String departSpeed) throws CommandException, InternalFederateException;
}
