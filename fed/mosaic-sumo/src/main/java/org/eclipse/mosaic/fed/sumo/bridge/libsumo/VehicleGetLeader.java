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
package org.eclipse.mosaic.fed.sumo.bridge.libsumo;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.LeadingVehicle;

import org.eclipse.sumo.libsumo.SWIGTYPE_p_std__pairT_std__string_double_t;
import org.eclipse.sumo.libsumo.Vehicle;
import org.slf4j.LoggerFactory;

public class VehicleGetLeader implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleGetLeader {

    public LeadingVehicle execute(Bridge bridge, String vehicle, double lookahead) {
        SWIGTYPE_p_std__pairT_std__string_double_t leader = Vehicle.getLeader(Bridge.VEHICLE_ID_TRANSFORMER.toExternalId(vehicle), lookahead);
        //TODO currently not implemented on libsumo side
        LoggerFactory.getLogger(this.getClass()).warn("Reading the leading vehicle is not implemented yet in libsumo.");
        return null;
//        return new LeadingVehicle(null, 0);
    }
}
