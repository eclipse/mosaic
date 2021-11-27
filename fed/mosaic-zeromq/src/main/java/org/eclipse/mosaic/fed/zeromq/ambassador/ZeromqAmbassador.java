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

import org.eclipse.mosaic.fed.zeromq.config.CZeromq;

import org.zeromq.SocketType;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;
import org.zeromq.ZMsg;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;


public class ZeromqAmbassador extends AbstractFederateAmbassador {

    ZContext ctx = new ZContext();
    private final Socket publisher = ctx.createSocket(SocketType.PUB);
    int backendProxyPort;
    String backendProxyAddr;

    public ZeromqAmbassador(AmbassadorParameter ambassadorParameter) {
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
            backendProxyPort = configuration.getBackendProxy();

        } catch (InstantiationException e) {
            log.error("Could not read configuration. Reason: {}", e.getMessage());
        }
        
        String backendProxyAddr = "tcp://127.0.0.1:" + String.valueOf(backendProxyPort);
        publisher.connect(backendProxyAddr);

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
            publisher.send(json);
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
    
}
