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

package org.eclipse.mosaic.fed.sumo.bridge.libsumo;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.eclipse.sumo.libsumo.Vehicle;

/**
 * This class represents the SUMO command which allows to filter a previously created context subscription to
 * collect all vehicles surrounding a specific vehicle.
 */
public class VehicleSubscriptionSetFieldOfVision
        implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSubscriptionSetFieldOfVision {

    public void execute(Bridge bridge, double openingAngle) throws CommandException, InternalFederateException {
        Vehicle.addSubscriptionFilterFieldOfVision(openingAngle);
    }

}
