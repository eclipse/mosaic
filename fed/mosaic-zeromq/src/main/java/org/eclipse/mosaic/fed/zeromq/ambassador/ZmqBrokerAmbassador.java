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
    String bindAddr;

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
    private Formatter logBroker = new Formatter(System.out);


    public ZmqBrokerAmbassador(AmbassadorParameter ambassadorParameter) {
        super(ambassadorParameter);
        this.services = new HashMap<String, Service>();
        this.workers = new HashMap<String, Worker>();
        this.waiting = new ArrayDeque<Worker>();
        this.heartbeatAt = System.currentTimeMillis() + heartbeatInterval;
        this.ctx = new ZContext();
        this.socket = ctx.createSocket(SocketType.ROUTER);
    }

    /**
     * Disconnect all workers, destroy context.
     */
    private void destroy()
    {
        Worker[] deleteList = workers.values().toArray(new Worker[0]);
        for (Worker worker : deleteList) {
            deleteWorker(worker, true);
        }
        ctx.destroy();
    }

    /**
     * Process a request coming from a client.
     */
    private void processClient(ZFrame sender, ZMsg msg)
    {
        assert (msg.size() >= 2); // Service name + body
        ZFrame serviceFrame = msg.pop();
        // Set reply return address to client sender
        msg.wrap(sender.duplicate());
        if (serviceFrame.toString().startsWith(internalServicePrefix))
            serviceInternal(serviceFrame, msg);
        else dispatch(requireService(serviceFrame), msg);
        serviceFrame.destroy();
    }

        /**
     * Process message sent to us by a worker.
     */
    private void processWorker(ZFrame sender, ZMsg msg)
    {
        assert (msg.size() >= 1); // At least, command

        ZFrame command = msg.pop();

        boolean workerReady = workers.containsKey(sender.strhex());

        Worker worker = requireWorker(sender);

        if (MDP.W_READY.frameEquals(command)) {
            // Not first command in session || Reserved service name
            if (workerReady || sender.toString().startsWith(internalServicePrefix))
                deleteWorker(worker, true);
            else {
                // Attach worker to service and mark as idle
                ZFrame serviceFrame = msg.pop();
                worker.service = requireService(serviceFrame);
                workerWaiting(worker);
                serviceFrame.destroy();
            }
        }
        else if (MDP.W_REPLY.frameEquals(command)) {
            if (workerReady) {
                // Remove & save client return envelope and insert the
                // protocol header and service name, then rewrap envelope.
                ZFrame client = msg.unwrap();
                msg.addFirst(worker.service.name);
                msg.addFirst(MDP.C_CLIENT.newFrame());
                msg.wrap(client);
                msg.send(socket);
                workerWaiting(worker);
            }
            else {
                deleteWorker(worker, true);
            }
        }
        else if (MDP.W_HEARTBEAT.frameEquals(command)) {
            if (workerReady) {
                worker.expiry = System.currentTimeMillis() + heartbeatExpiry;
            }
            else {
                deleteWorker(worker, true);
            }
        }
        else if (MDP.W_DISCONNECT.frameEquals(command))
            deleteWorker(worker, false);
        else {
            logBroker.format("E: invalid message:\n");
            msg.dump(logBroker.out());
        }
        msg.destroy();
    }

    /**
     * Deletes worker from all data structures, and destroys worker.
     */
    private void deleteWorker(Worker worker, boolean disconnect)
    {
        assert (worker != null);
        if (disconnect) {
            sendToWorker(worker, MDP.W_DISCONNECT, null, null);
        }
        if (worker.service != null)
            worker.service.waiting.remove(worker);
        workers.remove(worker);
        worker.address.destroy();
    }

    /**
     * Finds the worker (creates if necessary).
     */
    private Worker requireWorker(ZFrame address)
    {
        assert (address != null);
        String identity = address.strhex();
        Worker worker = workers.get(identity);
        if (worker == null) {
            worker = new Worker(identity, address.duplicate());
            workers.put(identity, worker);
            if (verbose)
                logBroker.format("I: registering new worker: %s\n", identity);
        }
        return worker;
    }

    /**
     * Locates the service (creates if necessary).
     */
    private Service requireService(ZFrame serviceFrame)
    {
        assert (serviceFrame != null);
        String name = serviceFrame.toString();
        Service service = services.get(name);
        if (service == null) {
            service = new Service(name);
            services.put(name, service);
        }
        return service;
    }


    /**
     * Handle internal service according to 8/MMI specification
     */
    private void serviceInternal(ZFrame serviceFrame, ZMsg msg)
    {
        String returnCode = "501";
        if ("mmi.service".equals(serviceFrame.toString())) {
            String name = msg.peekLast().toString();
            returnCode = services.containsKey(name) ? "200" : "400";
        }
        msg.peekLast().reset(returnCode.getBytes(ZMQ.CHARSET));
        // Remove & save client return envelope and insert the
        // protocol header and service name, then rewrap envelope.
        ZFrame client = msg.unwrap();
        msg.addFirst(serviceFrame.duplicate());
        msg.addFirst(MDP.C_CLIENT.newFrame());
        msg.wrap(client);
        msg.send(socket);
    }

    /**
     * Send heartbeats to idle workers if it's time
     */
    public synchronized void sendHeartbeats()
    {
        // Send heartbeats to idle workers if it's time
        if (System.currentTimeMillis() >= heartbeatAt) {
            for (Worker worker : waiting) {
                sendToWorker(worker, MDP.W_HEARTBEAT, null, null);
            }
            heartbeatAt = System.currentTimeMillis() + heartbeatInterval;
        }
    }

    /**
     * Look for &amp; kill expired workers. Workers are oldest to most recent, so we
     * stop at the first alive worker.
     */
    public synchronized void purgeWorkers()
    {
        for (Worker w = waiting.peekFirst(); w != null
                && w.expiry < System.currentTimeMillis(); w = waiting.peekFirst()) {
            logBroker.format("I: deleting expired worker: %s\n", w.identity);
            deleteWorker(waiting.pollFirst(), false);
        }
    }

        /**
     * This worker is now waiting for work.
     */
    public synchronized void workerWaiting(Worker worker)
    {
        // Queue to broker and service waiting lists
        waiting.addLast(worker);
        worker.service.waiting.addLast(worker);
        worker.expiry = System.currentTimeMillis() + heartbeatExpiry;
        dispatch(worker.service, null);
    }

    /**
     * Dispatch requests to waiting workers as possible
     */
    private void dispatch(Service service, ZMsg msg)
    {
        assert (service != null);
        if (msg != null)// Queue message if any
            service.requests.offerLast(msg);
        purgeWorkers();
        while (!service.waiting.isEmpty() && !service.requests.isEmpty()) {
            msg = service.requests.pop();
            Worker worker = service.waiting.pop();
            waiting.remove(worker);
            sendToWorker(worker, MDP.W_REQUEST, null, msg);
            msg.destroy();
        }
    }

    /**
     * Send message to worker. If message is provided, sends that message. Does
     * not destroy the message, this is the caller's job.
     */
    public void sendToWorker(Worker worker, MDP command, String option, ZMsg msgp)
    {

        ZMsg msg = msgp == null ? new ZMsg() : msgp.duplicate();

        // Stack protocol envelope to start of message
        if (option != null)
            msg.addFirst(new ZFrame(option));
        msg.addFirst(command.newFrame());
        msg.addFirst(MDP.W_WORKER.newFrame());

        // Stack routing envelope to start of message
        msg.wrap(worker.address.duplicate());
        if (verbose) {
            logBroker.format("I: sending %s to worker\n", command);
            msg.dump(logBroker.out());
        }
        msg.send(socket);
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

    private class Service
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
    private class Worker
    {
        String  identity;// Identity of worker
        ZFrame  address; // Address frame to route to
        Service service; // Owning service, if known
        long    expiry;  // Expires at unless heartbeat

        public Worker(String identity, ZFrame address)
        {
            this.address = address;
            this.identity = identity;
            this.expiry = System.currentTimeMillis() + heartbeatLiveness * heartbeatInterval;
        }
    }

    public int getHeartbeatLiveness() {
        return heartbeatLiveness;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }
    
}
