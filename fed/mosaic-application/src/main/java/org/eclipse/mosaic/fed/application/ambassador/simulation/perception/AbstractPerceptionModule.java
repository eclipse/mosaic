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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception;

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.errormodels.PerceptionModifier;
import org.eclipse.mosaic.fed.application.app.api.perception.PerceptionModule;
import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.spatial.WallFinder;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.spatial.Edge;

import com.google.common.collect.Lists;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;

public abstract class AbstractPerceptionModule
        implements PerceptionModule<SimplePerceptionConfiguration>, WallProvider {

    private static final double DEFAULT_VIEWING_ANGLE = 40;
    private static final double DEFAULT_VIEWING_RANGE = 200;

    protected final PerceptionModuleOwner owner;
    protected final Logger log;
    protected final Database database;

    protected SimplePerceptionConfiguration configuration;

    private WallFinder wallIndex = null;

    AbstractPerceptionModule(PerceptionModuleOwner owner, Database database, Logger log) {
        this.owner = owner;
        this.log = log;
        this.database = database;
    }

    @Override
    public void enable(SimplePerceptionConfiguration configuration) {
        if (configuration == null) {
            log.warn("Provided perception configuration is null. Using default configuration with viewingAngle={}°, viewingRange={}m.",
                    DEFAULT_VIEWING_ANGLE, DEFAULT_VIEWING_RANGE);
            this.configuration = new SimplePerceptionConfiguration(DEFAULT_VIEWING_ANGLE, DEFAULT_VIEWING_ANGLE);
        } else {
            this.configuration = configuration;
        }
    }

    @Override
    public boolean isEnabled() {
        return getConfiguration() != null;
    }

    @Override
    public SimplePerceptionConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public Collection<Edge<Vector3d>> getSurroundingWalls() {
        if (database == null) {
            log.warn("No database for retrieving walls available.");
            return Lists.newArrayList();
        }

        if (wallIndex == null) {
            if (database.getBuildings().isEmpty()) {
                log.warn("No buildings to retrieve walls available.");
            }
            wallIndex = new WallFinder(database);
        }
        return wallIndex.getWallsInRadius(
                owner.getVehicleData().getProjectedPosition().toVector3d(),
                getConfiguration().getViewingRange()
        );
    }

    @Override
    public List<VehicleObject> getPerceivedVehicles() {
        List<VehicleObject> filteredList = getVehiclesInRange();
        for (PerceptionModifier perceptionModifier : configuration.getPerceptionModifiers()) {
            filteredList = perceptionModifier.apply(owner, filteredList); // apply filters in sequence
        }
        return filteredList;
    }

    /**
     * As an intermediate step, this method returns all vehicles in range without applying
     * any perception modifiers.
     * @return the raw list of vehicles in range of the ego
     */
    abstract List<VehicleObject> getVehiclesInRange();
}
