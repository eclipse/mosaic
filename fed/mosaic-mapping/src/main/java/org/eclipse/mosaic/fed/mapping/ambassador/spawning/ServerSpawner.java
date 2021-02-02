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
import org.eclipse.mosaic.fed.mapping.config.units.CServer;
import org.eclipse.mosaic.interactions.mapping.ServerRegistration;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Class responsible for configuring Servers to be added to the simulation.
 */
public class ServerSpawner extends UnitSpawner implements Spawner {

    private static final Logger LOG = LoggerFactory.getLogger(ServerSpawner.class);

    /**
     * Constructor for {@link ServerSpawner} using configuration
     * to construct spawning object.
     *
     * @param serverConfiguration server configuration from json.
     */
    public ServerSpawner(CServer serverConfiguration) {
        super(
                serverConfiguration.applications,
                serverConfiguration.name,
                serverConfiguration.group
        );
    }

    /**
     * Constructor for {@link ServerSpawner} using configuration
     * to construct spawning object used by specialized servers (e.g. TMCs
     *
     * @param applications list of applications
     * @param name         name of the unit
     * @param group        group of the unit
     */
    public ServerSpawner(List<String> applications, String name, String group) {
        super(applications, name, group);
    }

    /**
     * Called by the {@link SpawningFramework}, used to initialize the servers for the simulation.
     *
     * @param spawningFramework the framework handling the spawning
     * @throws InternalFederateException if {@link ServerRegistration} couldn't be handled by rti
     */
    @Override
    public void init(SpawningFramework spawningFramework) throws InternalFederateException {
        String name = NameGenerator.getServerName();
        ServerRegistration interaction = new ServerRegistration(0, name, group, getAppList());
        try {
            LOG.info("Creating Server " + this.toString());
            spawningFramework.getRti().triggerInteraction(interaction);
        } catch (IllegalValueException e) {
            LOG.error("Exception while sending Interaction in ServerSpawner.init()");
            throw new InternalFederateException("Exception while sending Interaction in ServerSpawner.init()", e);
        }
    }

    @Override
    public String toString() {
        return " with apps: " + StringUtils.join(applications, ",");
    }
}
