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

import org.eclipse.mosaic.lib.util.NameGenerator;
import org.eclipse.mosaic.fed.mapping.ambassador.SpawningFramework;
import org.eclipse.mosaic.fed.mapping.config.units.CRoadSideUnit;
import org.eclipse.mosaic.interactions.mapping.RsuRegistration;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for configuring Road Side Units to be added to the simulation.
 */
public class RoadSideUnitSpawner extends UnitSpawner implements Spawner {

    private static final Logger LOG = LoggerFactory.getLogger(RoadSideUnitSpawner.class);

    /**
     * The position of the ChargingStation defined by a {@link GeoPoint}.
     */
    private GeoPoint position;

    /**
     * Constructor for {@link RoadSideUnitSpawner}.
     * Calls super constructor and sets additional parameters inferred from
     * json configuration.
     *
     * @param roadSideUnitConfiguration {@link CRoadSideUnit} created from json configuration
     */
    public RoadSideUnitSpawner(CRoadSideUnit roadSideUnitConfiguration) {
        super(roadSideUnitConfiguration.applications, roadSideUnitConfiguration.name, roadSideUnitConfiguration.group);
        this.position = roadSideUnitConfiguration.position;
    }

    /**
     * Called by the {@link SpawningFramework} used to initialize the
     * Charging Stations and their Charging Spots.
     *
     * @param spawningFramework the framework handling the spawning
     * @throws InternalFederateException thrown if {@link RsuRegistration} couldn't be handled by rti
     */
    public void init(SpawningFramework spawningFramework) throws InternalFederateException {
        String name = NameGenerator.getRsuName();
        RsuRegistration interaction = new RsuRegistration(0, name, group, getAppList(), this.position);
        try {
            LOG.info("Creating RSU " + this.toString());
            spawningFramework.getRti().triggerInteraction(interaction);
        } catch (IllegalValueException e) {
            LOG.error("Exception while sending Interaction in RoadSideUnitSpawner.init()");
            throw new InternalFederateException("Exception while sending Interaction in RoadSideUnitSpawner.init()", e);
        }
    }

    @Override
    public String toString() {
        return "@" + position + " with apps: " + StringUtils.join(applications, ",");
    }
}
