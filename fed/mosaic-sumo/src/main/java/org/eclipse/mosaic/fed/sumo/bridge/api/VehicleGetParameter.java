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

/**
 * This class reads a parameter value for specific a vehicle.
 */
public interface VehicleGetParameter {
    /**
     * This method executes the command with the given arguments in order to get the value of a specific parameter of the given vehicle.
     *
     * @param bridge        Connection to SUMO.
     * @param vehicleId     Id of the vehicle.
     * @param parameterName the name of the parameter to read
     * @return the value of the parameter, or {@code null} if not present
     * @throws CommandException          if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The connection to SUMO is shut down.
     */
    String execute(Bridge bridge, String vehicleId, String parameterName) throws CommandException, InternalFederateException;
}
