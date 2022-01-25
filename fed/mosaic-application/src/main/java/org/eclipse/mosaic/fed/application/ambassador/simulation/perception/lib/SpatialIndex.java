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


package org.eclipse.mosaic.fed.application.ambassador.simulation.perception.lib;

import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;

import java.util.List;

/**
 * A {@link SpatialIndex} is a representation of space using a special data structure.
 * The goal is to allow for efficient querying of of nearby entities.
 * TODO: probably needs some container
 */
public interface SpatialIndex {

    /**
     * Queries the {@link SpatialIndex} and returns all vehicles inside.
     *
     * @param position        position of the vehicle in cartesian space
     * @param heading         heading of the vehicle [degrees]
     * @param viewingDistance viewing distance of the perception module [m]
     * @param viewingAngle    viewing angle of the perception module [degrees]
     * @return a pre-selection of vehicles that could be perceived by a vehicle.
     */
    List<VehicleData> getVehiclesInIndexRange(CartesianPoint position, double heading, double viewingDistance, double viewingAngle);

    /**
     * Updates the {@link SpatialIndex} using the latest {@link VehicleUpdates} interaction.
     *
     * @param latestUpdates the latest update of the simulation
     */
    void update(VehicleUpdates latestUpdates);
}
