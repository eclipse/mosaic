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

import org.eclipse.mosaic.interactions.communication.AdHocCommunicationConfiguration;
import org.eclipse.mosaic.interactions.communication.V2xMessageReception;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.interactions.mapping.ChargingStationRegistration;
import org.eclipse.mosaic.interactions.mapping.RsuRegistration;
import org.eclipse.mosaic.interactions.mapping.TrafficLightRegistration;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.AbstractFederateAmbassador;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.Interaction;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.parameters.AmbassadorParameter;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.mapping.ChargingStationMapping;
import org.eclipse.mosaic.lib.objects.mapping.RsuMapping;
import org.eclipse.mosaic.lib.objects.mapping.TrafficLightMapping;
import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;

import org.eclipse.mosaic.fed.zeromq.config.CZeromq;

import org.zeromq.ZMsg;
import org.eclipse.mosaic.lib.zeromq.majordomo.MajordomoBroker;
import org.eclipse.mosaic.lib.zeromq.majordomo.MajordomoWorker;
import org.eclipse.mosaic.lib.zeromq.majordomo.MajordomoClient;
import org.eclipse.mosaic.lib.zeromq.majordomo.MDP;

import com.google.gson.Gson;
import java.util.Arrays;


public class ZeromqAmbassador extends AbstractFederateAmbassador {

    ZContext ctx = new ZContext();
    private final Socket publisher = ctx.createSocket(SocketType.PUB);
    private final Socket puller = ctx.createSocket(SocketType.PULL);;
    int backendProxyPort;
    int proxyBackendPort;
    ZPoller poller = new ZPoller(ctx);
    Poller items;

    public ZeromqAmbassador(AmbassadorParameter ambassadorParameter) {
        super(ambassadorParameter);
    }

    /**
     * Runs Majordomo Broker in different Thread
     */

     /**
      * 
    @Override
    public void run() {
        MajordomoBroker mdpBroker = new MajordomoBroker(true);
        mdpBroker.bind(this.brokerAddress);
        mdpBroker.mediate();
    }
      */


    @Override
    public void initialize(final long startTime, final long endTime) throws InternalFederateException {
        super.initialize(startTime, endTime);
        this.log.info("Init simulation with startTime={}, endTime={}", startTime, endTime);

        if (log.isTraceEnabled()) {
            log.trace("subscribedMessages: {}", Arrays.toString(this.rti.getSubscribedInteractions().toArray()));
        }

        try {
            CZeromq configuration = new ObjectInstantiation<>(CZeromq.class).readFile(ambassadorParameter.configuration);
            backendProxyPort = configuration.getBackendProxy();
            proxyBackendPort = configuration.getProxyBackend();

        } catch (InstantiationException e) {
            log.error("Could not read configuration. Reason: {}", e.getMessage());
        }
        
        String backendProxyAddr = "tcp://127.0.0.1:" + String.valueOf(5555);
        String proxyBackendAddr = "tcp://127.0.0.1:" + String.valueOf(proxyBackendPort);
        publisher.connect(backendProxyAddr);
        puller.connect(proxyBackendAddr);

        log.info("Initialized Zeromq Sockets!");
    }

        log.info("Initialized MajordomoWorker");
    }

    @Override
    protected void processInteraction(Interaction interaction) throws InternalFederateException {
        try {
            if (interaction.getTypeId().startsWith(RsuRegistration.TYPE_ID)) {
                this.process((RsuRegistration) interaction);
            }  else if (interaction.getTypeId().startsWith(VehicleUpdates.TYPE_ID)) {
                this.process((VehicleUpdates) interaction);
            } else {
            }
        } catch (Exception e) {
            throw new InternalFederateException(e);
        }
    }

    private void process(RsuRegistration interaction) {
        final RsuMapping applicationRsu = interaction.getMapping();
        if (applicationRsu.hasApplication()) {
            log.info("Added RSU id={} position={} @time={}", applicationRsu.getName(), applicationRsu.getPosition(), TIME.format(interaction.getTime()));
        }
    }

    private void process(VehicleUpdates interaction) {
            String json = this.createFVDGson(interaction);
            publisher.send(json);
    }

    }

    private String createInteractionGsonStr(VehicleUpdates interaction){
        Gson gson = new Gson();
        String json = gson.toJson(interaction);
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
    
}
