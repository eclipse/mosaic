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
import org.eclipse.mosaic.fed.output.generator.zeromq.zprotobuf.ZInteractMinimal;
import org.eclipse.mosaic.fed.output.generator.zeromq.zprotobuf.ZVehicleDataMinimal;
import org.eclipse.mosaic.fed.output.generator.zeromq.zprotobuf.ZVehicleUpdatesMinimal;
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

import com.google.common.io.ByteArrayDataOutput;
import com.google.protobuf.ByteString;

public class ZUtilityMini {

    ZInteractMinimal.Builder generic = ZInteractMinimal.newBuilder();

    public ZUtilityMini(Interaction interaction){

        generic.clear();
        
        generic.setTime(interaction.getTime());
        generic.setId(interaction.getId());
        generic.setSenderId(interaction.getSenderId());
        generic.setTypeId(interaction.getTypeId());
    }

    public ZInteractMinimal createZMessageLite() {
        return this.generic.build();
    }

    public String createPubTopic(){
        return generic.getTypeId();
    }

    public byte[] createPubTopicByteArray(){
        byte[] ser = generic.getTypeIdBytes().toByteArray();
        return ser;
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
    }

    @Handle
    public void process(V2xMessageReception interaction) {
    }
    @Handle
    public void process(RsuRegistration interaction) {
    }
    
     /*
    public ZInteractMinimal.ZV2xMessageReception.Builder processBuilder(V2xMessageReception interaction);
    public ZInteractMinimal.ZRsuRegistration.Builder processBuilder(RsuRegistration interaction); 
    public ZInteractMinimal.ZVehicleRegistration.Builder processBuilder(VehicleRegistration interaction);
    */
    @Handle
    public void process(VehicleUpdates interaction) {
        VehicleUpdates vehicleUpdates = VehicleUpdates.class.cast(interaction);
        ZVehicleUpdatesMinimal.Builder updates_builder = ZVehicleUpdatesMinimal.newBuilder();
        ZVehicleDataMinimal.Builder data_builder = ZVehicleDataMinimal.newBuilder();

        List<VehicleData> updatedVehicles = vehicleUpdates.getUpdated();
        for (VehicleData element : updatedVehicles) {
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
            data_builder.clear();
        }
        generic.setVehicleUpdates(updates_builder);
        updates_builder.clear();
    }
}
