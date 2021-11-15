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

import org.apache.commons.lang3.ClassUtils;
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

import org.eclipse.mosaic.fed.output.generator.zeromq.ZUtility;

class ZMosaic {
    public ZInteract createMessageLiteNew(Interaction interaction) throws Exception {

        ZInteract.Builder generic = ZInteract.newBuilder();
        
        generic.setTime(interaction.getTime());
        generic.setId(interaction.getId());
        generic.setSenderId(interaction.getSenderId());
        generic.setTypeId(interaction.getTypeId());
        
        ZUtility utility = new ZUtility(generic);

        if (interaction instanceof V2xMessageReception) {
            utility.process((V2xMessageReception) interaction);
        } else if (interaction instanceof V2xMessageTransmission) {
            NoOp();
        
        } else if (interaction instanceof ChargingStationUpdate) {
            NoOp();
        
        } else if (interaction instanceof ChargingStationRegistration) {
            NoOp();
        
        } else if (interaction instanceof RsuRegistration) {
            utility.process((RsuRegistration) interaction);
        
        } else if (interaction instanceof TmcRegistration) {
            NoOp();
        
        } else if (interaction instanceof TrafficLightRegistration) {
            NoOp();
        
        } else if (interaction instanceof VehicleRegistration) {
            utility.process((VehicleRegistration) interaction);
        
        } else if (interaction instanceof TrafficDetectorUpdates) {
            NoOp();
        
        } else if (interaction instanceof VehicleUpdates) {
            utility.process((VehicleUpdates) interaction);

        } else {
            throw new Exception("Unknown interaction type");
        }

        return utility.createZMessageLite();
    }

    public ZInteract createMessageLite(Interaction interaction) throws Exception {
        ZInteract.Builder generic = ZInteract.newBuilder();

        generic.setTime(interaction.getTime());
        generic.setId(interaction.getId());
        generic.setSenderId(interaction.getSenderId());
        generic.setTypeId(interaction.getTypeId());

        if (interaction instanceof V2xMessageReception) {
            V2xMessageReception v2x = V2xMessageReception.class.cast(interaction);
            ZInteract.ZV2xMessageReception.Builder builder = 
                ZInteract.ZV2xMessageReception.newBuilder();

            builder.setReceiverName(v2x.getReceiverName())
                .setMessageId(v2x.getMessageId())
                .setSendTime(v2x.getReceiverInformation().getSendTime())
                .setReceiveTime(v2x.getReceiverInformation().getReceiveTime())
                .setReceiveSignalStrength(v2x.getReceiverInformation().getReceiveSignalStrength());
            
            generic.setZV2XMessageReception(builder);

        } else if (interaction instanceof V2xMessageTransmission) {
            NoOp();
        
        } else if (interaction instanceof ChargingStationUpdate) {
            NoOp();
        
        } else if (interaction instanceof ChargingStationRegistration) {
            NoOp();
        
        } else if (interaction instanceof RsuRegistration) {
            RsuRegistration rsuInteraction = RsuRegistration.class.cast(interaction);
            ZInteract.ZRsuRegistration.Builder builder = 
                ZInteract.ZRsuRegistration.newBuilder();
            builder.setName(rsuInteraction.getMapping().getName());
            double lat = (rsuInteraction.getMapping().getPosition().getLatitude());
            double lon = (rsuInteraction.getMapping().getPosition().getLongitude());
            double alt = (rsuInteraction.getMapping().getPosition().getAltitude());

            List<Double> position = Arrays.asList(lat, lon, alt);
            builder.addAllPosition(position);
            generic.setZRsuRegistration(builder);
        
        } else if (interaction instanceof TmcRegistration) {
            NoOp();
        
        } else if (interaction instanceof TrafficLightRegistration) {
            NoOp();
        
        } else if (interaction instanceof VehicleRegistration) {
            // cast interaction to VehicleRegistration
            VehicleRegistration vehicleRegistration = VehicleRegistration.class.cast(interaction);
            // builder for VehicleRegistrationProto
            ZInteract.ZVehicleRegistration.Builder builder = 
                ZInteract.ZVehicleRegistration.newBuilder();
            builder.setName(vehicleRegistration.getMapping().getName());
            builder.setGroup(vehicleRegistration.getMapping().getGroup());
            List<String> applications = vehicleRegistration.getMapping().getApplications();
            builder.addAllApplications(applications);
            generic.setZVehicleRegistration(builder);
        
        } else if (interaction instanceof TrafficDetectorUpdates) {
            NoOp();
        
        } else if (interaction instanceof VehicleUpdates) {


        } else {
            throw new Exception("Unknown interaction type");
        }
        return generic.build();
    }

    private static void NoOp() {
    }
}

