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

package org.eclipse.mosaic.app.trafficmonitor;

import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.os.RoadSideUnitOperatingSystem;
import org.eclipse.mosaic.lib.enums.SensorType;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.etsi.Denm;
import org.eclipse.mosaic.lib.objects.v2x.etsi.DenmContent;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.database.road.Connection;
import java.util.stream.Collectors;
import java.util.Collection;


import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.SocketType;

import java.util.ArrayList;

/**
 * This class acts as an omniscient application for a server that warns vehicles
 * about certain hazards on the road. The hazard is hard-coded for tutorial purposes,
 * in more realistic scenarios the location would've been updated dynamically.
 */
public class MonitorWarning extends AbstractApplication<RoadSideUnitOperatingSystem> {

    ZContext ctx = new ZContext();
    private final Socket puller = ctx.createSocket(SocketType.PULL);
    private final Socket pusher = ctx.createSocket(SocketType.PUSH);
    private byte[] warningMessage;
    Poller items = ctx.createPoller(1);

    private Database database = Database.loadFromFile("scenarios/Monaco/application/Monaco.db");
    private ArrayList<Connection> connectionList = database.getConnections().stream().collect(Collectors.toCollection(ArrayList::new));
    private ArrayList<String> connectionStrings = new ArrayList<String>();

    private final static long INTERVAL = 1 * TIME.SECOND;

    private final static SensorType SENSOR_TYPE = SensorType.ICE;
    private final static float SPEED = 25 / 3.6f;

    @Override
    public void onStartup() {
        getLog().infoSimTime(this, "Initialize WeatherServer application");
        getOs().getCellModule().enable();
        getLog().infoSimTime(this, "Activated Cell Module");

        String proxyBackendAddr = "tcp://127.0.0.1:" + String.valueOf(1111);
        String backendProxyAddr = "tcp://127.0.0.1:" + String.valueOf(2222);

        puller.connect(proxyBackendAddr);
        pusher.connect(backendProxyAddr);
        items.register(puller, Poller.POLLIN);


        for (Connection conn : connectionList){
            connectionStrings.add(conn.getId());
        }

        sample();
    }

    /**
     * This method is called by mosaic-application when a previously triggered event is handed over to the
     * application for processing.
     * Events can be triggered be this application itself, e.g. to run a routine periodically.
     *
     * @param event The event to be processed
     */
    @Override
    public void processEvent(Event event) throws Exception {
        sample();
    }

    private boolean incomingWarning(){
        items.poll(0);
        if (items.pollin(0)) {
            warningMessage = puller.recv(0);
            return true;
        }else{
            return false;
        }
    }

    private boolean checkWarningValidity(byte[] warningMsg){
        String candidateWarning = new String(warningMsg);
        if (connectionStrings.contains(candidateWarning)){
            return true;
        } else {
            return false;
        }
    }

    private void sample() {

        boolean retWarning = incomingWarning();
        if (retWarning) {
            boolean retValid = checkWarningValidity(warningMessage);

            if (retValid) {
                byte[] success = "1".getBytes();
                pusher.send(success, ZMQ.SNDMORE);
                pusher.send(warningMessage, 0);
                String warningRoadId = new String(warningMessage);
                final Denm denm = constructDenm(warningRoadId);
                getOs().getCellModule().sendV2xMessage(denm);
                getLog().infoSimTime(this, "Sent DENM");
                // Line up new event for periodic sending
                getOs().getEventManager().addEvent(
                        getOs().getSimulationTime() + INTERVAL, this);
            }
            else{
                byte[] fail = "0".getBytes();
                pusher.send(fail, ZMQ.SNDMORE);
                pusher.send("Invalid Connection ID", 0);
            }
            } else {
                getLog().debug("No MonitorWarning Message Detected");
            }
    }

    private Denm constructDenm(String road_id) {
        final GeoCircle geoCircle = new GeoCircle(getOs().getPosition(), 5000.0D);
        final MessageRouting routing = getOs().getCellModule().createMessageRouting().geoBroadcastBasedOnUnicast(geoCircle);

        final int strength = getOs().getStateOfEnvironmentSensor(SENSOR_TYPE);

        return new Denm(routing,
                new DenmContent(
                        getOs().getSimulationTime(),
                        getOs().getInitialPosition(),
                        road_id,
                        SENSOR_TYPE,
                        strength,
                        SPEED,
                        0.0f,
                        null,
                        null,
                        null
                )
        );
    }

    /**
     * This method is called by mosaic-application when the simulation has finished.
     */
    @Override
    public void onShutdown() {
        getLog().infoSimTime(this, "Shutdown application");
    }

}
