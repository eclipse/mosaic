/*
 * Copyright (c) 2023 Fraunhofer FOKUS and others. All rights reserved.
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
 * This class reads the speed factor value for specific a vehicle type.
 */
public interface VehicleTypeGetSpeedFactor {

    /**
     * This method executes the command with the given arguments in order to get the speed factor value of a specific vehicle type.
     *
     * @param bridge        Connection to SUMO.
     * @param vehicleTypeId Id of the vehicle type.
     * @return the value of the parameter, or {@code null} if not present
     * @throws CommandException          if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The connection to SUMO is shut down.
     */
    double execute(Bridge bridge, String vehicleTypeId) throws CommandException, InternalFederateException;
}
