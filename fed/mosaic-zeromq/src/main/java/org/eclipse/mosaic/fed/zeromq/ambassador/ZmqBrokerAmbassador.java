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

package org.eclipse.mosaic.fed.zeromq.ambassador;

import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.rti.api.AbstractFederateAmbassador;
import org.eclipse.mosaic.rti.api.Interaction;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.parameters.AmbassadorParameter;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.zeromq.majordomo.MajordomoBroker;
import org.eclipse.mosaic.lib.zeromq.majordomo.MajordomoWorker;
import org.eclipse.mosaic.lib.zeromq.majordomo.MDP;


import org.eclipse.mosaic.fed.zeromq.config.CZeromq;

import org.zeromq.SocketType;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;
import org.zeromq.ZMsg;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Formatter;
import java.util.Map;

public class ZmqBrokerAmbassador extends AbstractFederateAmbassador {

    int backendProxyPort;
    String backendProxyAddr;

    private String internalServicePrefix;
    private int heartbeatLiveness;
    private int heartbeatInterval;
    private int heartbeatExpiry;

    private ZContext   ctx;    // Our context
    private ZMQ.Socket socket; // Socket for clients & workers

    private long                 heartbeatAt;// When to send HEARTBEAT
    private Map<String, Service> services;   // known services
    private Map<String, Worker>  workers;    // known workers
    private Deque<Worker>        waiting;    // idle workers

    private boolean   verbose = false;                    // Print activity to stdout
    private Formatter log     = new Formatter(System.out);

    public ZmqBrokerAmbassador(AmbassadorParameter ambassadorParameter) {
        super(ambassadorParameter);
    }


    @Override
    public void initialize(final long startTime, final long endTime) throws InternalFederateException {
        super.initialize(startTime, endTime);
        this.log.info("Init simulation with startTime={}, endTime={}", startTime, endTime);

        if (log.isTraceEnabled()) {
            log.trace("subscribedMessages: {}", Arrays.toString(this.rti.getSubscribedInteractions().toArray()));
        }

        try {
            CZeromq configuration = new ObjectInstantiation<>(CZeromq.class).readFile(ambassadorParameter.configuration);
            this.backendProxyPort = configuration.getBackendProxy();
            this.internalServicePrefix = configuration.getInternalServicePrefix();
            this.heartbeatExpiry = configuration.getHeartbeatExpiry();
            this.heartbeatLiveness = configuration.getHeartbeatLiveness();
            this.heartbeatInterval = configuration.getHeartbeatInterval();

        } catch (InstantiationException e) {
            log.error("Could not read configuration. Reason: {}", e.getMessage());
        }
        
        this.backendProxyAddr = "tcp://127.0.0.1:" + String.valueOf(backendProxyPort);

        Thread brokerThread = new Thread(new MajordomoBroker(true, this.backendProxyAddr));
        brokerThread.start();
        log.info("Initialized Zeromq Sockets!");
    }


    @Override
    protected void processInteraction(Interaction interaction) throws InternalFederateException {
        try {
            if (interaction.getTypeId().startsWith(VehicleUpdates.TYPE_ID)) {
                this.process((VehicleUpdates) interaction);
            } else {
            }
        } catch (Exception e) {
            throw new InternalFederateException(e);
        }
    }

    private void process(VehicleUpdates interaction) {
            String json = this.createFVDGson(interaction);
            // socket_send(to_worker, interaction)
            ZMsg reply = null;
            ZMsg request = this.workerVehicleUpdates.receive(reply);
    }

    private List<Double> geoPointConvert(GeoPoint pos){
        List<Double> list = Arrays.asList(pos.getLatitude(), pos.getLongitude());
        return list;
    } 

    private String createFVDGson(VehicleUpdates interaction){
        Gson gson = new Gson();
        HashMap<String, Object> updatedSingle = new HashMap<String, Object>();
        HashMap<String, Object> updatedHash = new HashMap<String, Object>();
        List<VehicleData> updated = interaction.getUpdated();

        for (VehicleData vehicle : updated){
            updatedSingle.put("time", vehicle.getTime());
            updatedSingle.put("position",geoPointConvert(vehicle.getPosition()));
            updatedSingle.put("speed", vehicle.getSpeed());
            updatedSingle.put("road_id", vehicle.getRoadPosition().getConnectionId());

            updatedHash.put(vehicle.getName(), updatedSingle);
        }
        String json = gson.toJson(updatedHash);
        return json;
    }

    @Override
    public void finishSimulation() throws InternalFederateException {
        log.info("Finished simulation");
    }

    @Override
    public boolean isTimeConstrained() {
        return false;
    }

    @Override
    public boolean isTimeRegulating() {
        return false;
    }

    private static class Service
    {
        public final String name;     // Service name
        Deque<ZMsg>         requests; // List of client requests
        Deque<Worker>       waiting;  // List of waiting workers

        public Service(String name)
        {
            this.name = name;
            this.requests = new ArrayDeque<ZMsg>();
            this.waiting = new ArrayDeque<Worker>();
        }
    }

    /**
     * This defines one worker, idle or active.
     */
    private static class Worker
    {
        String  identity;// Identity of worker
        ZFrame  address; // Address frame to route to
        Service service; // Owning service, if known
        long    expiry;  // Expires at unless heartbeat

        public Worker(String identity, ZFrame address)
        {
            this.address = address;
            this.identity = identity;
            this.expiry = System.currentTimeMillis() + HEARTBEAT_INTERVAL * HEARTBEAT_LIVENESS;
        }
    }
    
}
