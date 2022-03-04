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

package org.eclipse.mosaic.fed.sumo.bridge.libsumo;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.eclipse.sumo.libsumo.Vehicle;

public class VehicleSetRemove implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetRemove {

    public void execute(Bridge bridge, String vehicleId, Reason reason) throws CommandException, InternalFederateException {
        Vehicle.remove(Bridge.VEHICLE_ID_TRANSFORMER.toExternalId(vehicleId), (char) reason.reasonByte);
    }

}
