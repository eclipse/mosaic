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
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.AbstractFederateAmbassador;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.Interaction;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.eclipse.mosaic.rti.api.parameters.AmbassadorParameter;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.fed.zeromq.config.CZeromq;
import org.eclipse.mosaic.fed.zeromq.device.AmbassadorBroker;
import org.eclipse.mosaic.fed.zeromq.device.AmbassadorWorker;
import org.eclipse.mosaic.fed.zeromq.interactions.FlowBreakdownInteraction;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Random;


public class ZeromqAmbassador extends AbstractFederateAmbassador {

    private Thread brokerThread;
    private AmbassadorWorker worker;
    private AmbassadorBroker broker;
    private String backend, frontend;
    private Random r;
    private double noiseLat;
    private double noiseLon;

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
            backend = configuration.getBackend();
            frontend = configuration.getFrontend();
            broker = new AmbassadorBroker(frontend, backend);
            brokerThread = new Thread(broker);
            brokerThread.start();
            this.log.info("AmbassadorBroker started in Thread {}!, cooldown for 1sec!", brokerThread);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                this.log.info("AmbassadorBroker cooldown phase finalized!");
            }
        } catch (InstantiationException e) {
            log.error("Could not read configuration. Reason: {}", e.getMessage());
        }

        worker = new AmbassadorWorker(backend, "req.interaction");
        this.log.info("AmbassadorWorker {} started!, cooldown for 1sec!", worker);
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
        String json = createFVDGson(interaction);
        worker.recvAndSend(json);

        Integer chance = r.nextInt(1000);
        if (chance.compareTo(998) > 0){
            FlowBreakdownInteraction flowbreakInteraction = new FlowBreakdownInteraction(
                interaction.getTime() + 5 * TIME.SECOND, null, interaction);
            try {
                this.rti.triggerInteraction(flowbreakInteraction);
            } catch (IllegalValueException | InternalFederateException e) {

            }
        }
    }

    private List<Double> geoPointConvert(GeoPoint pos, boolean flagNoise){
        List<Double> list;
        if (flagNoise) {
            noiseLat = noiseGenerator(1E-7);
            noiseLon = noiseGenerator(1E-7);
            list = Arrays.asList(pos.getLatitude() + noiseLat, pos.getLongitude() + noiseLon);
        } else{
            list = Arrays.asList(pos.getLatitude(), pos.getLongitude());
        }

        return list;
    }

    private double noiseGenerator(double power){
        double noise = r.nextGaussian() * Math.sqrt(1/2);
        return power * noise;
    }

    private String createFVDGson(VehicleUpdates interaction){
        Gson gson = new Gson();
        HashMap<String, Object> updatedSingle = new HashMap<String, Object>();
        HashMap<String, Object> updatedHash = new HashMap<String, Object>();
        List<VehicleData> updated = interaction.getUpdated();

        for (VehicleData vehicle : updated){
            updatedSingle.put("time", vehicle.getTime());
            updatedSingle.put("position",geoPointConvert(vehicle.getPosition(), false));
            updatedSingle.put("road_id", vehicle.getRoadPosition().getConnectionId());

            updatedHash.put(vehicle.getName(), updatedSingle);
        }
        String json = gson.toJson(updatedHash);
        return json;
    }

    @Override
    public void finishSimulation() throws InternalFederateException {
        worker.destroy();
        brokerThread.interrupt();
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
