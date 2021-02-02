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
import org.eclipse.mosaic.fed.mapping.config.units.CTrafficManagementCenter;
import org.eclipse.mosaic.interactions.mapping.TmcRegistration;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for configuring Traffic Management Centers to be added to the simulation.
 */
public class TrafficManagementCenterSpawner extends ServerSpawner {

    private static final Logger LOG = LoggerFactory.getLogger(TrafficManagementCenterSpawner.class);

    private final List<String> inductionLoopDetectors;
    private final List<String> laneAreaDetectors;

    /**
     * Constructor for {@link TrafficManagementCenterSpawner}.
     * Calls super constructor and sets additional parameters inferred from
     * json configuration.
     *
     * @param trafficManagementCenterConfiguration {@link CTrafficManagementCenter} created from json configuration
     */
    public TrafficManagementCenterSpawner(CTrafficManagementCenter trafficManagementCenterConfiguration) {
        super(
                trafficManagementCenterConfiguration.applications,
                trafficManagementCenterConfiguration.name,
                trafficManagementCenterConfiguration.group
        );
        this.inductionLoopDetectors = trafficManagementCenterConfiguration.inductionLoops;
        this.laneAreaDetectors = trafficManagementCenterConfiguration.laneAreaDetectors;
    }

    /**
     * Called by the {@link SpawningFramework} used to initialize the
     * Traffic Management Centers for the simulation.
     *
     * @param spawningFramework the framework handling the spawning
     * @throws InternalFederateException if {@link TmcRegistration} couldn't be handled by rti
     */
    public void init(SpawningFramework spawningFramework) throws InternalFederateException {
        String name = NameGenerator.getTmcName();
        TmcRegistration interaction = new TmcRegistration(0, name, group, getAppList(), getInductionLoopList(), getLaneAreaList());
        try {
            LOG.info("Creating TMC " + this.toString());
            spawningFramework.getRti().triggerInteraction(interaction);
        } catch (IllegalValueException e) {
            LOG.error("Exception while sending Interaction in TrafficManagementCenterSpawner.init()");
            throw new InternalFederateException("Exception while sending Interaction in TrafficManagementCenterSpawner.init()", e);
        }
    }

    /**
     * If the {@link #inductionLoopDetectors}-list is null this will return an empty list, otherwise
     * a copy of the list is returned.
     *
     * @return the induction loop detector list
     */
    private List<String> getInductionLoopList() {
        return inductionLoopDetectors == null ? new ArrayList<>() : new ArrayList<>(inductionLoopDetectors);
    }

    /**
     * If the {@link #laneAreaDetectors}-list is null this will return an empty list, otherwise
     * a copy of the list is returned.
     *
     * @return the lane area detector list
     */
    private List<String> getLaneAreaList() {
        return laneAreaDetectors == null ? new ArrayList<>() : new ArrayList<>(laneAreaDetectors);
    }
}
