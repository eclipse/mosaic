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
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.LeadingVehicle;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import javax.annotation.Nullable;

/**
 * This class represents the SUMO command which allows to get the leading vehicle.
 */
public interface VehicleGetLeader {

    /**
     * This method executes the command with the given arguments in order to get the leading vehicle.
     *
     * @param bridge    Connection to SUMO.
     * @param vehicle   Id of the vehicle.
     * @param lookahead look ahead.
     * @return Id of the leading vehicle.
     * @throws CommandException          if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The connection to SUMO is shut down.
     */
    @Nullable
    LeadingVehicle execute(Bridge bridge, String vehicle, double lookahead) throws CommandException, InternalFederateException;
}
