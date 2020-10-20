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

package org.eclipse.mosaic.fed.application.app.api.os;

import org.eclipse.mosaic.fed.application.ambassador.simulation.tmc.InductionLoop;
import org.eclipse.mosaic.fed.application.ambassador.simulation.tmc.LaneAreaDetector;
import org.eclipse.mosaic.lib.enums.VehicleClass;

import java.util.Collection;

/**
 * This interface extends the basic {@link OperatingSystem} and
 * is implemented by the {@link org.eclipse.mosaic.fed.application.ambassador.simulation.AbstractSimulationUnit}
 * {@link org.eclipse.mosaic.fed.application.ambassador.simulation.TrafficManagementCenterUnit}.
 */
public interface TrafficManagementCenterOperatingSystem extends ServerOperatingSystem {

    /**
     * Returns the lane detector, which provides various measurements, such as traffic flow.
     *
     * @param id The id of the lane detector
     * @return The {@link InductionLoop} with the given id.
     */
    InductionLoop getInductionLoop(String id);

    /**
     * Returns the lane segment, which provides various measurements, such as density.
     *
     * @param id The id of the lane segment.
     * @return The {@link LaneAreaDetector} with the given id.
     */
    LaneAreaDetector getLaneAreaDetector(String id);

    /**
     * Returns all {@link InductionLoop}s configured for this traffic management center.
     *
     * @return A list of all configured {@link InductionLoop}s.
     */
    Collection<InductionLoop> getInductionLoops();

    /**
     * Returns all {@link LaneAreaDetector}s configured for this traffic management center.
     *
     * @return A list of all configured {@link LaneAreaDetector}s.
     */
    Collection<LaneAreaDetector> getLaneAreaDetectors();

    /**
     * Provides a facility to change the lane state of the given lane.
     *
     * @param edge      The ID of the edge.
     * @param laneIndex The index of the lane (0 = right-most lane).
     */
    ChangeLaneState changeLaneState(String edge, int laneIndex);

    interface ChangeLaneState {

        /**
         * Opens the lane for a given set of vehicle classes. All other classes are prohibited to drive on this lane further on.
         */
        ChangeLaneState openOnlyForVehicleClasses(VehicleClass... allowVehicleClasses);

        /**
         * Closes the lane for a given set of vehicle classes. All other classes are allowed to drive on this lane further on.
         */
        ChangeLaneState closeOnlyForVehicleClasses(VehicleClass... allowVehicleClasses);

        /**
         * Closes the lane for all vehicle classes.
         */
        @SuppressWarnings("UnusedReturnValue")
        ChangeLaneState closeForAll();

        /**
         * Opens the lane for all vehicle classes.
         */
        @SuppressWarnings("UnusedReturnValue")
        ChangeLaneState openForAll();

        /**
         * Sets the maximum allowed speed of the lane to the given value.
         *
         * @param maxSpeedMs the maximum speed to set in m/s
         */
        ChangeLaneState setMaxSpeed(double maxSpeedMs);
    }
}
