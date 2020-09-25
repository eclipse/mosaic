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

import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.AdHocModule;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CellModule;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CommunicationModuleOwner;
import org.eclipse.mosaic.fed.application.app.api.Application;
import org.eclipse.mosaic.lib.enums.SensorType;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.util.scheduling.EventManager;
import org.eclipse.mosaic.rti.TIME;

import java.io.File;
import java.util.List;

/**
 * This interface describes all necessary functionality for units to be
 * simulated.
 */
public interface OperatingSystem extends CommunicationModuleOwner {

    /**
     * Returns the path to the application simulator configuration directory.
     *
     * @return The path of the configuration directory as {@link File}.
     */
    File getConfigurationPath();

    /**
     * Returns the event manager for this task to add events.
     *
     * @return The manager to add events.
     */
    EventManager getEventManager();

    /**
     * Returns the id of the unit.
     *
     * @return The unitName (i.e. veh_1, rsu_0).
     */
    String getId();

    /**
     * Returns the simulation time. Unit: [ns].
     *
     * @return the simulation time. Unit: [ns].
     */
    long getSimulationTime();

    /**
     * Returns the time difference in milliseconds since a well-defined start time -
     * here milliseconds since the simulation start time.
     * 1.1.1970 00:00:00.000 Needs to be aligned with SAE J2735
     * DF_DDateTime, DF_DTime, DE_DSecond. Unit: [ms].
     *
     * @return the simulation time. Unit: [ms].
     */
    default long getSimulationTimeMs() {
        return getSimulationTime() / TIME.MILLI_SECOND;
    }

    /**
     * This data element provides an absolute geographical longitude and latitude in a WGS84
     * coordinate system with a granularity of 1/8 micro degrees. Compliant to SAE J2735
     * DE_Longitude.
     *
     * @return the position.
     */
    GeoPoint getInitialPosition();

    /**
     * This data element provides an absolute geographical longitude and latitude in a WGS84
     * coordinate system with a granularity of 1/8 micro degrees. Compliant to SAE J2735
     * DE_Longitude.
     *
     * @return the position.
     */
    GeoPoint getPosition();

    /**
     * Send a log tuple for the ITEF visualizer.
     *
     * @param logTupleId log tuple identifier
     * @param values     list of values to be logged
     */
    void sendItefLogTuple(long logTupleId, int... values);

    /**
     * Send a byte array message to SUMO TraCI.
     *
     * @param command Byte array containing SUMO TraCI message.
     *
     * @return a identifier which can be used to match incoming TraCI command response
     */
    String sendSumoTraciRequest(byte[] command);

    /**
     * Returns The state of the supplied sensor.
     *
     * @param type The {@link SensorType} type to use.
     * @return Strength of the measured environment sensor data.
     */
    int getStateOfEnvironmentSensor(SensorType type);

    /**
     * Returns the cellular communication module of this unit.
     *
     * @return the {@link CellModule} of this unit.
     */
    CellModule getCellModule();

    /**
     * Returns the ad-hoc communication module of this unit.
     *
     * @return the {@link AdHocModule} of this unit.
     */
    AdHocModule getAdHocModule();

    /**
     * Get the list of all applications running on this simulation unit.
     *
     * @return the list containing all applications.
     */
    List<? extends Application> getApplications();

    /**
     * Get the list of all applications running on this simulation unit.
     *
     * @return the list containing all applications.
     */
    <A extends Application> Iterable<A> getApplicationsIterator(Class<A> applicationClass);
}
