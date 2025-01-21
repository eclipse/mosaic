/*
 * Copyright (c) 2025 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.application.ambassador.simulation.electric.providers;

import org.eclipse.mosaic.fed.application.ambassador.simulation.electric.objects.ChargingStationObject;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.electricity.ChargingStationData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ChargingStationIndex {

    /**
     * Stores {@link ChargingStationObject}s for fast removal and position update.
     */
    final Map<String, ChargingStationObject> indexedChargingStations = new HashMap<>();

    /**
     * Method called to initialize index after configuration has been read.
     */
    public abstract void initialize();

    /**
     * Perform action before an update of the {@link ChargingStationIndex} takes place.
     */
    public abstract void onChargingStationUpdate();

    public abstract int getNumberOfChargingStations();

    public abstract List<ChargingStationObject> getChargingStationsInCircle(GeoCircle circle);

    public void addChargingStation(String id, GeoPoint position) {
        indexedChargingStations.computeIfAbsent(id, ChargingStationObject::new)
                .setPosition(position.toCartesian());
    }

    public void updateChargingStation(ChargingStationData chargingStationData) {
        onChargingStationUpdate();
        indexedChargingStations.get(chargingStationData.getName())
                .setChargingStationData(chargingStationData);
    }

}
