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
import org.eclipse.mosaic.lib.objects.traffic.SumoTraciResult;
import org.eclipse.mosaic.rti.api.InternalFederateException;

/**
 * Implementation for a byte array message from the simulation.
 */
public interface SimulationTraciRequest {

    SumoTraciResult execute(Bridge bridge, String messageId, byte[] messageContent) throws CommandException, InternalFederateException;
}
