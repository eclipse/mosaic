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

package org.eclipse.mosaic.fed.application.ambassador.simulation.electric;

import static org.eclipse.mosaic.lib.geo.GeoPoint.latLon;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.ambassador.SimulationKernelRule;
import org.eclipse.mosaic.fed.application.ambassador.simulation.electric.objects.ChargingStationObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.electric.providers.ChargingStationIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.navigation.CentralNavigationComponent;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.CentralPerceptionComponent;
import org.eclipse.mosaic.fed.application.config.CApplicationAmbassador;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.electricity.ChargingSpot;
import org.eclipse.mosaic.lib.objects.electricity.ChargingStationData;
import org.eclipse.mosaic.lib.objects.electricity.ChargingType;
import org.eclipse.mosaic.lib.transform.GeoProjection;
import org.eclipse.mosaic.lib.transform.Wgs84Projection;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ChargingStationIndexTest {
    GeoPoint position = latLon(52.5, 13.4);

    @Rule
    public SimulationKernelRule simulationKernel = new SimulationKernelRule(null, null,
            mock(CentralNavigationComponent.class), mock(CentralPerceptionComponent.class));

    @Before
    public void before() {
        SimulationKernel.SimulationKernel.setConfiguration(new CApplicationAmbassador());
        SimulationKernel.SimulationKernel.setClassLoader(ClassLoader.getSystemClassLoader());

        ChargingStationIndex chargingStationIndex = new ChargingStationIndex();
        SimulationKernel.SimulationKernel.setChargingStationIndex(chargingStationIndex);

        // init geo projection
        GeoProjection.initialize(new Wgs84Projection(position).failIfOutsideWorld());
    }

    private void registerChargingStations(int amount) {
        for (int i = 0; i < amount; i++) {
            SimulationKernel.SimulationKernel.getChargingStationIndex().addChargingStation("cs_" + i, position);
        }
    }

    private List<ChargingSpot> getChargingSpots(int amount, String cs_id) {
        List<ChargingSpot> spots = new ArrayList<>();
        // create ChargingSpots
        for (int i = 0; i < amount; i++) {
            String spot_id = "charging_spot" + cs_id + "_" + i;
            ChargingSpot spot = new ChargingSpot(spot_id, ChargingType.DC, 100.0, 100.0);
            spots.add(spot);
        }

        return spots;
    }

    @Test
    public void addChargingStation_ChargingStationIndex() {
        registerChargingStations(3);

        // add initial data to charging stations
        List<ChargingSpot> spots_0 = getChargingSpots(3, "cs_0");
        SimulationKernel.SimulationKernel.getChargingStationIndex().updateChargingStation(new ChargingStationData(0, "cs_0", position, spots_0));
        SimulationKernel.SimulationKernel.getChargingStationIndex().updateChargingStation(new ChargingStationData(0, "cs_1", position, new ArrayList<>()));
        SimulationKernel.SimulationKernel.getChargingStationIndex().updateChargingStation(new ChargingStationData(0, "cs_2", position, new ArrayList<>()));

        // verify that the charging stations were added
        GeoCircle searchArea = new GeoCircle(position, 10000);
        List<ChargingStationObject> stations = SimulationKernel.SimulationKernel.getChargingStationIndex()
                .getChargingStationsInCircle(searchArea);
        int numberOfStations = SimulationKernel.SimulationKernel.getChargingStationIndex().getNumberOfChargingStations();

        // assert added stations are present in tree
        assertEquals(numberOfStations, 3);
        assertEquals(stations.get(0).getChargingStationData().getName(), "cs_1");
        assertEquals(stations.get(1).getChargingStationData().getName(), "cs_2");
        assertEquals(stations.get(2).getChargingStationData().getName(), "cs_0");
    }

    @Test
    public void update_ChargingStationIndex() {
        registerChargingStations(1);

        // add initial data to charging stations
        List<ChargingSpot> spots_0 = getChargingSpots(3, "cs_0");
        SimulationKernel.SimulationKernel.getChargingStationIndex().updateChargingStation(new ChargingStationData(0, "cs_0", position, spots_0));

        // update state of charging spot of cs_0
        spots_0.get(0).setAvailable(false);
        SimulationKernel.SimulationKernel.getChargingStationIndex().updateChargingStation(new ChargingStationData(0, "cs_0", position, spots_0));

        // assert tree was updated
        GeoCircle searchArea = new GeoCircle(position, 10000);
        boolean isAvailable_cs0 = SimulationKernel.SimulationKernel.getChargingStationIndex().getChargingStationsInCircle(searchArea)
                .get(0).getChargingStationData().getChargingSpot(spots_0.get(0).getChargingSpotId()).isAvailable();
        assertEquals(isAvailable_cs0, false);
    }
}
