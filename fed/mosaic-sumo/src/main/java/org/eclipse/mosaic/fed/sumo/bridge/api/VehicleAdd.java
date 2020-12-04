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

package org.eclipse.mosaic.fed.sumo.bridge.api;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.rti.api.InternalFederateException;

public interface VehicleAdd {
    /**
     * This method executes the command with the given arguments in order to add a vehicle with its specific properties.
     *
     * @param con            Connection to Traci.
     * @param vehicleId      Id of the vehicle.
     * @param routeId        Id of the route.
     * @param vehicleType    Type of the vehicle.
     * @param departLane     Lane of the departure. Possible values: [int] or random", "free", "allowed", "best", "first"
     * @param departPosition Position of the departure. Possible values: [int] or "random", "free", "base", "last", "random free"
     * @param departSpeed    Speed at departure. Possible Values: [double] or "max", "random"
     * @param line           Line for public transport.
     * @param personCapacity The amount of persons a vehicle can hold.
     * @param personNumber   The amount of people in the vehicle.
     * @throws CommandException     If the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException If some serious error occurs during writing or reading. The TraCI connection is shut down.
     */

    void execute(Bridge con, String vehicleId, String routeId, String vehicleType,
                 String departLane, String departPosition, String departSpeed,
                 String line, int personCapacity, int personNumber) throws CommandException, InternalFederateException;

    /**
     * This method executes the command with the given arguments in order to add a vehicle with its specific properties.
     * This overload of {@link #execute} uses some default values
     *
     * @param con            Connection to TraCI.
     * @param vehicleId      Id of the vehicle.
     * @param routeId        Id of the route.
     * @param vehicleType    Type of the vehicle.
     * @param departLane     Lane of the departure. Possible values: [int] or random", "free", "allowed", "best", "first"
     * @param departPosition Position of the departure. Possible values: [int] or "random", "free", "base", "last", "random free"
     * @param departSpeed    Speed at departure. Possible Values: [double] or "max", "random"
     * @throws CommandException     If the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException If some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    void execute(Bridge con, String vehicleId, String routeId, String vehicleType,
                 String departLane, String departPosition, String departSpeed) throws CommandException, InternalFederateException;
}
