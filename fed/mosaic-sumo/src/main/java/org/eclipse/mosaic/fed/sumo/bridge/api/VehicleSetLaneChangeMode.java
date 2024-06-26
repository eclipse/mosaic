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
 * This class represents the SUMO command which allows to set the lane-change-mode as following.
 * - Strategic (change lanes to continue the route)
 * - Cooperative (change in order to allow others to change)
 * - Speed gain (the other lane allows for faster driving)
 * - Obligation to drive on the right
 */
public interface VehicleSetLaneChangeMode {

    void execute(Bridge bridge, String vehicleId, int value) throws CommandException, InternalFederateException;
}
