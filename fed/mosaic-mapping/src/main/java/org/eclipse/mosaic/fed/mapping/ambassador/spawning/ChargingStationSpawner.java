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

package org.eclipse.mosaic.fed.mapping.ambassador.spawning;

import org.eclipse.mosaic.fed.mapping.ambassador.SpawningFramework;
import org.eclipse.mosaic.fed.mapping.config.units.CChargingStation;
import org.eclipse.mosaic.fed.mapping.config.units.CChargingStation.CChargingSpot;
import org.eclipse.mosaic.interactions.mapping.ChargingStationRegistration;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.UnitNameGenerator;
import org.eclipse.mosaic.lib.objects.electricity.ChargingSpot;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for configuring Charging Stations to be added to the simulation.
 */
public class ChargingStationSpawner extends UnitSpawner implements StationaryUnitSpawner {

    private static final Logger LOG = LoggerFactory.getLogger(ChargingStationSpawner.class);

    /**
     * The position of the ChargingStation defined by a {@link GeoPoint}.
     */
    private final GeoPoint position;

    /**
     * A list of all configurations of ChargingSpots belonging to the ChargingStation.
     */
    private final List<CChargingSpot> chargingSpotConfigurations;

    /**
     * Constructor for {@link ChargingStationSpawner}.
     * Calls super constructor and sets additional parameters inferred from
     * json configuration.
     *
     * @param chargingStationConfiguration {@link CChargingStation} created from json configuration
     */
    public ChargingStationSpawner(CChargingStation chargingStationConfiguration) {
        super(chargingStationConfiguration.applications, chargingStationConfiguration.name, chargingStationConfiguration.group);
        this.position = chargingStationConfiguration.position;

        this.chargingSpotConfigurations = ObjectUtils.defaultIfNull(chargingStationConfiguration.chargingSpots, new ArrayList<>());
    }

    /**
     * Called by the {@link SpawningFramework} used to initialize the
     * Charging Stations for the simulation.
     *
     * @param spawningFramework the framework handling the spawning
     * @throws InternalFederateException thrown if {@link ChargingStationRegistration} couldn't be handled by rti
     */
    @Override
    public void init(SpawningFramework spawningFramework) throws InternalFederateException {
        String chargingStationName = UnitNameGenerator.nextChargingStationName();
        List<ChargingSpot> chargingSpots = new ArrayList<>();
        int id = 0;
        for (CChargingSpot chargingSpotConfig : chargingSpotConfigurations) {
            chargingSpots.add(new ChargingSpot(
                    chargingStationName + "_" + id, chargingSpotConfig.chargingType,
                    chargingSpotConfig.maxVoltage, chargingSpotConfig.maxCurrent
            ));
            id++;
        }
        ChargingStationRegistration chargingStationRegistration =
                new ChargingStationRegistration(0, chargingStationName, group, getApplications(), position, chargingSpots);
        try {
            LOG.info("Creating Charging Station: {}", this);
            spawningFramework.getRti().triggerInteraction(chargingStationRegistration);
        } catch (IllegalValueException e) {
            LOG.error("Exception while sending ChargingStationRegistration interaction in ChargingStationSpawner.init()");
            throw new InternalFederateException(
                    "Exception while sending Interaction in ChargingStationSpawner.init()", e);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("position", position)
                .append("chargingSpots",
                        chargingSpotConfigurations.stream()
                                .map(chargingSpot ->
                                        "chargingSpot(" + chargingSpotConfigurations.indexOf(chargingSpot) + ")=" + chargingSpot.toString())
                                .toList()
                )
                .build();
    }
}
