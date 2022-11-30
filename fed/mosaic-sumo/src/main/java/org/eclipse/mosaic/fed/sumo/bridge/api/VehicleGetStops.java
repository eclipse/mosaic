/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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
import org.eclipse.mosaic.lib.objects.vehicle.StoppingPlace;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.List;

public interface VehicleGetStops {
    /**
     * This method returns a list of {@link StoppingPlace} representing the next stops a vehicle will take.
     *
     * @param bridge    Connection to SUMO.
     * @param vehicleId Id of the vehicle.
     * @param maxStops  the maximal amount of stops returned
     * @return a list of {@link StoppingPlace} for the next stop, or {@code null} if not present
     * @throws CommandException          if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The connection to SUMO is shut down.
     */
    List<StoppingPlace> execute(Bridge bridge, String vehicleId, int maxStops) throws CommandException, InternalFederateException;
}
