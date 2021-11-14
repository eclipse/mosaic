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

package org.eclipse.mosaic.fed.output.generator.zeromq;

import org.eclipse.mosaic.interactions.communication.V2xMessageReception;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.interactions.electricity.ChargingStationUpdate;
import org.eclipse.mosaic.interactions.mapping.ChargingStationRegistration;
import org.eclipse.mosaic.interactions.mapping.RsuRegistration;
import org.eclipse.mosaic.interactions.mapping.TrafficLightRegistration;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.interactions.mapping.TmcRegistration;
import org.eclipse.mosaic.interactions.traffic.TrafficDetectorUpdates;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.rti.api.Interaction;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Queues;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;

@SuppressWarnings("UnstableApiUsage")
public class SocketZeromqServer extends ZContext {

    private static final int MAX_MESSAGES_LIST = 1000;

    /**
     * Separate message id to transmit removed vehicles without dropping
     * them in VehicleUpdates.
     */
    private static final String VEHICLES_REMOVE_TYPE_ID = "VehiclesRemove";

    private final AtomicReference<VehicleUpdates> vehicleUpdatesReference = new AtomicReference<>();
    private final Queue<String> vehiclesToRemove = createQueue();

    private final Queue<V2xMessageTransmission> sentV2xMessages = createQueue();
    private final Queue<V2xMessageReception> receivedV2xMessages = createQueue();
    private final Queue<TrafficDetectorUpdates> trafficDetectorUpdates = createQueue();

    private final Queue<VehicleRegistration> vehicleRegistrations = createQueue();
    private final Queue<RsuRegistration> rsuRegistrations = createQueue();
    private final Queue<TrafficLightRegistration> trafficLightRegistrations = createQueue();
    private final Queue<ChargingStationRegistration> chargingStationRegistrations = createQueue();
    private final Queue<TmcRegistration> TrafficManagementCenterRegistrations = createQueue();
    private final Queue<ChargingStationUpdate> chargingStationUpdates = createQueue();

    ZContext context = new ZContext();
    Socket publisher = context.createSocket(SocketType.XPUB);

    public SocketZeromqServer(Integer port) {

    }

    private VehicleUpdates reduceVehicleUpdates(VehicleUpdates original) {
        List<VehicleData> reducedUpdates = new ArrayList<>();
        for (VehicleData veh : original.getUpdated()) {
            reducedUpdates.add(new VehicleData.Builder(veh.getTime(), veh.getName())
                    .position(veh.getPosition(), veh.getProjectedPosition())
                    .create());
        }
        return new VehicleUpdates(original.getTime(), Collections.EMPTY_LIST, reducedUpdates, Collections.EMPTY_LIST);
    }

    public synchronized void updateVehicleUpdates(VehicleUpdates interaction) {
        vehicleUpdatesReference.set(interaction);
        /* VehicleUpdates can be dropped as only the latest VehicleUpdates is sent when the server is ready for the next message.
         * To avoid dropping removed vehicles, we collect then in this extra queue and send them all together. */
        vehiclesToRemove.addAll(interaction.getRemovedNames());
    }

    public synchronized void sendV2xMessage(V2xMessageTransmission interaction) {
        sentV2xMessages.add(interaction);
    }

    public synchronized void receiveV2xMessage(V2xMessageReception interaction) {
        receivedV2xMessages.add(interaction);
    }

    public synchronized void addRoadsideUnit(RsuRegistration interaction) {
        rsuRegistrations.add(interaction);
    }

    public synchronized void addTrafficLight(TrafficLightRegistration interaction) {
        trafficLightRegistrations.add(interaction);
    }

    public synchronized void addChargingStation(ChargingStationRegistration interaction) {
        chargingStationRegistrations.add(interaction);
    }

    public synchronized void addTrafficManagementCenter(TmcRegistration interaction) {
        TrafficManagementCenterRegistrations.add(interaction);
    }

    public synchronized void updateTrafficDetectors(TrafficDetectorUpdates interaction) {
        trafficDetectorUpdates.add(interaction);
    }

    public synchronized void updateChargingStation(ChargingStationUpdate interaction) {
        chargingStationUpdates.add(interaction);
    }

    public synchronized void addVehicle(VehicleRegistration interaction) {
        vehicleRegistrations.add(interaction);
    }

    private static <T> Queue<T> createQueue() {
        return Queues.synchronizedQueue(EvictingQueue.create(MAX_MESSAGES_LIST));
    }

    public void start() {
    }

}
