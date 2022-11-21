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
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.SpatialObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.TrafficLightObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.VehicleObject;
import org.eclipse.mosaic.fed.application.app.api.perception.PerceptionModule;
import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.spatial.WallFinder;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.spatial.Edge;

import com.google.common.collect.Lists;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
        List<SpatialObject> vehiclesInRange = new ArrayList<>(getVehiclesInRange());
        vehiclesInRange = applyPerceptionModifiers(vehiclesInRange);
        return vehiclesInRange.stream().map(spatialObject -> (VehicleObject) spatialObject).collect(Collectors.toList());
    }

    /**
     * As an intermediate step, this method returns all vehicles in range without applying
     * any perception modifiers.
     *
     * @return the raw list of vehicles in range of the ego vehicle
     */
    abstract List<VehicleObject> getVehiclesInRange();

    @Override
    public List<TrafficLightObject> getPerceivedTrafficLights() {
        List<SpatialObject> trafficLightsInRange = new ArrayList<>(getTrafficLightsInRange());
        trafficLightsInRange = applyPerceptionModifiers(trafficLightsInRange);
        return trafficLightsInRange.stream().map(spatialObject -> (TrafficLightObject) spatialObject).collect(Collectors.toList());
    }

    /**
     * As an intermediate step, this method returns all traffic lights in range without applying
     * any perception modifiers.
     *
     * @return the raw list of traffic lights in range of the ego vehicle
     */
    abstract List<TrafficLightObject> getTrafficLightsInRange();

    @Override
    public List<SpatialObject> getPerceivedObjects() {
        List<SpatialObject> objectsInRange = getObjectsInRange();
        objectsInRange = applyPerceptionModifiers(objectsInRange);
        return objectsInRange;
    }

    /**
     * As an intermediate step, this method returns all spatial objects in range without applying
     * any perception modifiers.
     *
     * @return the raw list of objects in range of the ego vehicle
     */
    abstract List<SpatialObject> getObjectsInRange();

    private List<SpatialObject> applyPerceptionModifiers(List<SpatialObject> objectsInRange) {
        List<SpatialObject> filteredList = objectsInRange;
        for (PerceptionModifier perceptionModifier : configuration.getPerceptionModifiers()) {
            filteredList = perceptionModifier.apply(owner, filteredList); // apply filters in sequence
        }
        return filteredList;
    }
}
