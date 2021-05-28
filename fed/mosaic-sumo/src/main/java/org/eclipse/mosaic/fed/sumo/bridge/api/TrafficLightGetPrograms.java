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
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.SumoTrafficLightLogic;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.List;

/**
 * Retrieves complete definitions of all traffic light programs. Does only work with SUMO >= 1.1.0. With
 * prior API versions an empty list is returned.
 */
public interface TrafficLightGetPrograms {
    /**
     * This method executes the command with the given arguments in order to get the complete definitions of all traffic light programs.
     *
     * @param bridge Connection to SUMO.
     * @param tlId   Id of the traffic light.
     * @return The traffic light programs.
     * @throws CommandException          if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The connection to SUMO is shut down.
     */
    List<SumoTrafficLightLogic> execute(Bridge bridge, String tlId) throws CommandException, InternalFederateException;
}
