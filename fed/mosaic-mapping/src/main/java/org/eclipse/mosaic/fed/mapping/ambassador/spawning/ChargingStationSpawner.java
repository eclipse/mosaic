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

import static org.apache.commons.lang3.Validate.notNull;

import org.eclipse.mosaic.lib.util.NameGenerator;
import org.eclipse.mosaic.fed.mapping.ambassador.SpawningFramework;
import org.eclipse.mosaic.fed.mapping.config.units.CChargingStation;
import org.eclipse.mosaic.fed.mapping.config.units.CChargingStation.CChargingSpot;
import org.eclipse.mosaic.interactions.mapping.ChargingStationRegistration;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.electricity.ChargingSpot;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for configuring Charging Stations to be added to the simulation.
 */
public class ChargingStationSpawner extends UnitSpawner implements Spawner {

    private static final Logger LOG = LoggerFactory.getLogger(ChargingStationSpawner.class);

    /**
     * The position of the ChargingStation defined by a {@link GeoPoint}.
     */
    private final GeoPoint position;
    /**
     * The operator of the ChargingStation (e.g. RWE, Vattenfall, etc.)
     */
    private String operator;
    /**
     * Access restrictions, e.g. open to all or restricted to some communities,
     * free of access or paying access (mandatory).
     */
    private String access;
    /**
     * A list of all configurations of ChargingSpots belonging to the ChargingStation.
     */
    private List<CChargingSpot> chargingSpotConfigurations;

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
        this.operator = chargingStationConfiguration.operator;
        this.access = chargingStationConfiguration.access;

        this.chargingSpotConfigurations = ObjectUtils.defaultIfNull(chargingStationConfiguration.chargingSpots, new ArrayList<>());
    }

    /**
     * Called by the {@link SpawningFramework} used to initialize the
     * Road Side Units for the simulation.
     *
     * @param spawningFramework the framework handling the spawning
     * @throws InternalFederateException thrown if {@link ChargingStationRegistration} couldn't be handled by rti
     */
    @Override
    public void init(SpawningFramework spawningFramework) throws InternalFederateException {
        String name = NameGenerator.getChargingStationName();
        List<ChargingSpot> chargingSpots = new ArrayList<>();
        int id = 0;
        for (CChargingSpot chargingSpot : chargingSpotConfigurations) {
            if (chargingSpot.id == null) {
                chargingSpot.id = id++;
            }
            chargingSpots.add(new ChargingSpot(
                    name + "_" + chargingSpot.id,
                    notNull(chargingSpot.type, "No type set for charging spot with id " + chargingSpot.id),
                    notNull(chargingSpot.parkingPlaces, "No parkingPlaces set for charging spot with id " + chargingSpot.id)
            ));
        }
        ChargingStationRegistration chargingStationRegistration = new ChargingStationRegistration(0, name, group, getAppList(),
                position, operator, access, chargingSpots);
        try {
            LOG.info("Creating Charging Station " + this.toString());
            spawningFramework.getRti().triggerInteraction(chargingStationRegistration);
        } catch (IllegalValueException e) {
            LOG.error("Exception while sending ChargingStationRegistration interaction in ChargingStationSpawner.init()");
            throw new InternalFederateException(
                    "Exception while sending Interaction in ChargingStationSpawner.init()", e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("@position: ")
                .append(position)
                .append(", operator: ")
                .append(operator)
                .append(", access: ")
                .append(access)
                .append(", charging spots: ");

        String delimiter = "";
        for (CChargingSpot chargingSpotConfiguration : chargingSpotConfigurations) {
            sb.append(delimiter)
                    .append("[id: ")
                    .append(chargingSpotConfiguration.id)
                    .append(", type: ")
                    .append(chargingSpotConfiguration.type)
                    .append(", parking places: ")
                    .append(chargingSpotConfiguration.parkingPlaces)
                    .append("]");
            delimiter = ", ";
        }

        sb.append("] with apps: ");

        delimiter = "";
        for (String application : getAppList()) {
            sb.append(delimiter).append(application);
            delimiter = ", ";
        }

        return sb.toString();
    }
}
