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

import org.eclipse.mosaic.fed.sumo.bridge.api.SimulationClose;
import org.eclipse.mosaic.fed.sumo.bridge.api.SimulationGetVersion;
import org.eclipse.mosaic.fed.sumo.bridge.api.SimulationTraciRequest;
import org.eclipse.mosaic.fed.sumo.bridge.facades.PoiFacade;
import org.eclipse.mosaic.fed.sumo.bridge.facades.RouteFacade;
import org.eclipse.mosaic.fed.sumo.bridge.facades.SimulationFacade;
import org.eclipse.mosaic.fed.sumo.bridge.facades.TrafficLightFacade;
import org.eclipse.mosaic.fed.sumo.bridge.facades.VehicleFacade;
import org.eclipse.mosaic.fed.sumo.bridge.traci.AbstractTraciCommand;
import org.eclipse.mosaic.fed.sumo.config.CSumo;
import org.eclipse.mosaic.fed.sumo.util.MosaicConformVehicleIdTransformer;
import org.eclipse.mosaic.lib.objects.traffic.SumoTraciResult;
import org.eclipse.mosaic.lib.util.objects.IdTransformer;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.annotation.Nonnull;

/**
 * Implementation of the bridge between MOSAIC and SUMO using a own TraCI client implementation.
 */
public class TraciClientBridge implements Bridge {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Defines the {@link IdTransformer} which transforms the vehicle IDs if required. Every
     * {@link AbstractTraciCommand} which reads or writes a vehicle ID, uses this transformer. Per default
     * no transformation is done, i.e. passing the vehicle IDs as is. However, for other scenarios
     * a custom transformer may be defined here, which converts the vehicle IDs known by SUMO to
     * vehicle IDs known by the consumer (e.g. MOSAIC) of this TraciClient.
     */
    public static final IdTransformer<String, String> VEHICLE_ID_TRANSFORMER = new MosaicConformVehicleIdTransformer();

    private final CommandRegister commandRegister;

    private final Socket sumoServerSocket;
    private final DataInputStream in;
    private final DataOutputStream out;

    private final SimulationFacade simulationControl;
    private final VehicleFacade vehicleControl;
    private final TrafficLightFacade trafficLightControl;
    private final RouteFacade routeControl;
    private final PoiFacade poiControl;

    private final SimulationTraciRequest simulationTraciRequest;

    private SumoVersion currentVersion;

    /**
     * Constructor for {@link TraciClientBridge} uses a default {@link CommandRegister} if none has been given.
     *
     * @param sumoConfiguration SUMO configuration file.
     * @param sumoServerSocket  SUMO server socket.
     * @throws IOException Error while reading or writing.
     */
    public TraciClientBridge(@Nonnull final CSumo sumoConfiguration, @Nonnull final Socket sumoServerSocket) throws IOException {
        this(sumoConfiguration, sumoServerSocket, new CommandRegister(sumoConfiguration, "traci"));
    }

    /**
     * Creates a new {@link TraciClientBridge} object with the specified SUMO configuration file, the SUMO server socket
     * and the command register.
     *
     * @param sumoConfiguration SUMO configuration file.
     * @param sumoServerSocket  SUMO server socket.
     * @param commandRegister   Command register.
     * @throws IOException Error while reading or writing.
     */
    public TraciClientBridge(@Nonnull final CSumo sumoConfiguration, @Nonnull final Socket sumoServerSocket, @Nonnull CommandRegister commandRegister) throws IOException {
        this.in = new DataInputStream(new BufferedInputStream(sumoServerSocket.getInputStream()));
        this.out = new DataOutputStream(sumoServerSocket.getOutputStream());
        this.sumoServerSocket = sumoServerSocket;
        this.commandRegister = commandRegister;
        this.commandRegister.setBridge(this);

        this.simulationControl = new SimulationFacade(this, sumoConfiguration);

        this.vehicleControl = new VehicleFacade(this);
        this.trafficLightControl = new TrafficLightFacade(this);
        this.routeControl = new RouteFacade(this);
        this.poiControl = new PoiFacade(this);

        this.simulationTraciRequest = commandRegister.getOrCreate(SimulationTraciRequest.class);
    }

    /**
     * Returns a facade which offers methods to control the simulation.
     */
    public SimulationFacade getSimulationControl() {
        return simulationControl;
    }

    /**
     * Returns a facade which offers methods to control the vehicles in the simulation.
     */
    public VehicleFacade getVehicleControl() {
        return vehicleControl;
    }

    /**
     * Returns a facade which offers methods to control traffic lights in the simulation.
     */
    public TrafficLightFacade getTrafficLightControl() {
        return trafficLightControl;
    }

    /**
     * Returns a facade which offers methods to control routes taken by vehicles in the simulation.
     */
    public RouteFacade getRouteControl() {
        return routeControl;
    }

    /**
     * Returns a facade which offers methods to add POIs to the SUMO GUI and change its properties.
     */
    public PoiFacade getPoiControl() {
        return poiControl;
    }

    /**
     * Sends a custom byte array to the traci channel.
     *
     * @param msgId the laneId of the byte array message which is included in the result
     * @param msg   the bytes to be sent. this should only contain the command, the header information will be added automatically.
     * @return an object containing the response of TraCI (without header information)
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public SumoTraciResult writeByteArrayMessage(final String msgId, final byte[] msg) throws InternalFederateException {
        try {
            return simulationTraciRequest.execute(this, msgId, msg);
        } catch (CommandException e) {
            throw new InternalFederateException(e);
        }
    }

    /**
     * Requests to close the simulation and determines all socket connections to
     * the SUMO.
     */
    public void close() {
        try {
            commandRegister.getOrCreate(SimulationClose.class).execute(this);
        } catch (Exception e) {
            log.error("Simulation could not be closed properly.", e);
        } finally {
            closeStreamsAndSockets();
        }
    }

    @Override
    public SumoVersion getCurrentVersion() {
        if (currentVersion == null) {
            try {
                SimulationGetVersion.CurrentVersion actualVersion = commandRegister.getOrCreate(SimulationGetVersion.class).execute(this);
                this.currentVersion = SumoVersion.getSumoVersion(actualVersion.sumoVersion);
                if (currentVersion == SumoVersion.UNKNOWN && actualVersion.apiVersion == SumoVersion.HIGHEST.getApiVersion()) {
                    log.warn("This SUMO Version {} is currently not supported (but might work anyhow).",
                            actualVersion.sumoVersion
                    );
                    this.currentVersion = SumoVersion.HIGHEST;
                } else if (actualVersion.apiVersion > SumoVersion.HIGHEST.getApiVersion()) {
                    log.warn("This TraCI API version {} is currently not supported (but might work anyhow).",
                            actualVersion.apiVersion
                    );
                    this.currentVersion = SumoVersion.HIGHEST;
                }
            } catch (Exception e) {
                throw new IllegalStateException("Could not retrieve TraCI API version.", e);
            }
        }
        return currentVersion;
    }

    @Override
    public DataInputStream getIn() {
        return in;
    }

    @Override
    public DataOutputStream getOut() {
        return out;
    }

    public CommandRegister getCommandRegister() {
        return commandRegister;
    }

    @Override
    public void emergencyExit(Throwable e) {
        log.error("Close all TraCI streams due to an error", e);
        closeStreamsAndSockets();
    }

    private void closeStreamsAndSockets() {
        closeWithWarning(getIn(), "Could not close input stream to TraCI properly.");
        closeWithWarning(getOut(), "Could not close output stream from TraCI properly.");
        closeWithWarning(sumoServerSocket, "Could not close server socket properly.");
    }

    private void closeWithWarning(Closeable closeable, String warning) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            log.warn(warning);
        }
    }
}
