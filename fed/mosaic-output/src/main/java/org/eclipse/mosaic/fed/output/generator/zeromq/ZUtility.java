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

import org.eclipse.mosaic.fed.output.ambassador.Handle;

import org.eclipse.mosaic.interactions.communication.V2xMessageReception;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.interactions.electricity.ChargingStationUpdate;
import org.eclipse.mosaic.interactions.mapping.ChargingStationRegistration;
import org.eclipse.mosaic.interactions.mapping.RsuRegistration;
import org.eclipse.mosaic.interactions.mapping.TmcRegistration;
import org.eclipse.mosaic.interactions.mapping.TrafficLightRegistration;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.interactions.traffic.TrafficDetectorUpdates;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.lib.objects.vehicle.*;

import org.eclipse.mosaic.rti.api.Interaction;

import java.util.Arrays;
import java.util.List;

public class ZUtility {

    ZInteract.Builder generic = ZInteract.newBuilder();

    public ZUtility(Interaction interaction){
        
        generic.setTime(interaction.getTime());
        generic.setId(interaction.getId());
        generic.setSenderId(interaction.getSenderId());
        generic.setTypeId(interaction.getTypeId());
    }

    public ZInteract createZMessageLite() {
        return this.generic.build();
    }

    @Handle
    public void process(TrafficLightRegistration interaction){
    }

    @Handle
    public void process(ChargingStationRegistration interaction){
    }

    @Handle
    public void process(ChargingStationUpdate interaction){
    }

    @Handle
    public void process(TrafficDetectorUpdates interaction){
    }

    @Handle
    public void process(TmcRegistration interaction){
    }

    @Handle
    public void process(V2xMessageTransmission interaction){
        
    }

    @Handle
    public void process(VehicleRegistration interaction) {
        VehicleRegistration current = VehicleRegistration.class.cast(interaction);
        // builder for VehicleRegistrationProto
        ZInteract.ZVehicleRegistration.Builder builder = 
            ZInteract.ZVehicleRegistration.newBuilder();
        
        builder.setName(current.getMapping().getName())
            .setGroup(current.getMapping().getGroup());
        
        List<String> applications = current.getMapping().getApplications();
        builder.addAllApplications(applications);
        generic.setZVehicleRegistration(builder);
    }

    @Handle
    public void process(V2xMessageReception interaction) {
        V2xMessageReception current = V2xMessageReception.class.cast(interaction);
        ZInteract.ZV2xMessageReception.Builder builder = 
            ZInteract.ZV2xMessageReception.newBuilder();

        builder.setReceiverName(current.getReceiverName())
            .setMessageId(current.getMessageId())
            .setSendTime(current.getReceiverInformation().getSendTime())
            .setReceiveTime(current.getReceiverInformation().getReceiveTime())
            .setReceiveSignalStrength(current.getReceiverInformation().getReceiveSignalStrength());

        generic.setZV2XMessageReception(builder);
    }
    @Handle
    public void process(RsuRegistration interaction) {
        RsuRegistration current = RsuRegistration.class.cast(interaction);
        ZInteract.ZRsuRegistration.Builder builder = 
            ZInteract.ZRsuRegistration.newBuilder();

        builder.setName(current.getMapping().getName());
        double lat = (current.getMapping().getPosition().getLatitude());
        double lon = (current.getMapping().getPosition().getLongitude());
        double alt = (current.getMapping().getPosition().getAltitude());

        List<Double> position = Arrays.asList(lat, lon, alt);
        builder.addAllPosition(position);
        generic.setZRsuRegistration(builder);

     }
    
     /*
    public ZInteract.ZV2xMessageReception.Builder processBuilder(V2xMessageReception interaction);
    public ZInteract.ZRsuRegistration.Builder processBuilder(RsuRegistration interaction); 
    public ZInteract.ZVehicleRegistration.Builder processBuilder(VehicleRegistration interaction);
    */
    @Handle
    public void process(VehicleUpdates interaction) {
        VehicleUpdates vehicleUpdates = VehicleUpdates.class.cast(interaction);
        ZInteract.ZVehicleUpdates.Builder updates_builder = 
            ZInteract.ZVehicleUpdates.newBuilder();
        ZInteract.ZVehicleData.Builder data_builder = 
            ZInteract.ZVehicleData.newBuilder();

        updates_builder.setTime(vehicleUpdates.getTime());

        List<VehicleData> addedVehicles = vehicleUpdates.getAdded();
        for (VehicleData element : addedVehicles) {
            data_builder.setTime(element.getTime())
                .setName(element.getName());
            double lat = (element.getPosition()).getLatitude();
            double lon = (element.getPosition().getLongitude());
            double alt = (element.getPosition().getAltitude());

            List<Double> position = Arrays.asList(lat, lon, alt);
            data_builder.addAllPosition(position)
                .setRoadId(element.getRoadPosition().getConnectionId())
                .setRouteId(element.getRouteId())
                .setSpeed(element.getSpeed())
                .setLongitudinalAcc(element.getLongitudinalAcceleration());
            
            updates_builder.addAdded(data_builder);
        }

        List<VehicleData> updatedVehicles = vehicleUpdates.getUpdated();
        for (VehicleData element : updatedVehicles) {
            data_builder.setTime(element.getTime());
            data_builder.setName(element.getName());
            double lat = (element.getPosition().getLatitude());
            double lon = (element.getPosition().getLongitude());
            double alt = (element.getPosition().getAltitude());

            List<Double> position = Arrays.asList(lat, lon, alt);
            data_builder.addAllPosition(position)
                .setRoadId(element.getRoadPosition().getConnectionId())
                .setRouteId(element.getRouteId())
                .setSpeed(element.getSpeed())
                .setLongitudinalAcc(element.getLongitudinalAcceleration());
            
            updates_builder.addUpdated(data_builder);
        }
        generic.setZVehicleUpdates(updates_builder);
    }
}
