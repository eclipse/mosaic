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
import org.eclipse.mosaic.lib.enums.VehicleStopMode;
import org.eclipse.mosaic.lib.objects.vehicle.StoppingPlace;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.eclipse.sumo.libsumo.TraCINextStopData;
import org.eclipse.sumo.libsumo.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class VehicleGetStops implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleGetStops {
    @Override
    public List<StoppingPlace> execute(Bridge bridge, String vehicleId, int maxStops) throws CommandException, InternalFederateException {
        List<StoppingPlace> nextStops = new ArrayList<>();
        for (TraCINextStopData stopData : Vehicle.getStops(vehicleId, maxStops)) {
            StoppingPlace stoppingPlace = new StoppingPlace.Builder()
                    .laneId(stopData.getLane()).endPos(stopData.getEndPos()).stoppingPlaceId(stopData.getStoppingPlaceID())
                    .stopFlags(VehicleStopMode.fromSumoInt(stopData.getStopFlags())).stopDuration(stopData.getDuration())
                    .stoppedUntil(stopData.getUntil()).startPos(stopData.getStartPos())
                    .build();
            nextStops.add(stoppingPlace);
        }
        return nextStops;
    }
}
