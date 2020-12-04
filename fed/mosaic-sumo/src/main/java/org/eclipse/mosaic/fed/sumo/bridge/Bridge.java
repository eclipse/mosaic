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

package org.eclipse.mosaic.fed.sumo.bridge;

import org.eclipse.mosaic.fed.sumo.bridge.facades.PoiFacade;
import org.eclipse.mosaic.fed.sumo.bridge.facades.RouteFacade;
import org.eclipse.mosaic.fed.sumo.bridge.facades.SimulationFacade;
import org.eclipse.mosaic.fed.sumo.bridge.facades.TrafficLightFacade;
import org.eclipse.mosaic.fed.sumo.bridge.facades.VehicleFacade;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Interface of a TraCI connection.
 */
public interface Bridge {

    /**
     * Getter for the input stream.
     *
     * @return the input stream from the server for reading.
     */
    DataInputStream getIn();

    /**
     * Getter for the output stream.
     *
     * @return the output stream towards the server for writing.
     */
    DataOutputStream getOut();

    /**
     * Getter for the currently running SUMO version.
     *
     * @return the SUMO Version of the TraCI server.
     */
    SumoVersion getCurrentVersion();

    /**
     * Getter for the command register.
     *
     * @return the register where command instances are stored.
     */
    CommandRegister getCommandRegister();

    /**
     * Returns a facade which offers methods to control the simulation.
     */
    SimulationFacade getSimulationControl();

    /**
     * Returns a facade which offers methods to control the vehicles in the simulation.
     */
    VehicleFacade getVehicleControl();

    /**
     * Returns a facade which offers methods to control traffic lights in the simulation.
     */
    TrafficLightFacade getTrafficLightControl();

    /**
     * Returns a facade which offers methods to control routes taken by vehicles in the simulation.
     */
    RouteFacade getRouteControl();

    /**
     * Returns a facade which offers methods to control routes taken by vehicles in the simulation.
     */
    PoiFacade getPoiControl();

    /**
     * Closes the simulation.
     */
    void close();

    /**
     * Call this method if the traci-connection should be closed immediately.
     */
    void emergencyExit(Throwable e);
}
