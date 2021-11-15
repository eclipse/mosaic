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
import org.eclipse.mosaic.interactions.mapping.TmcRegistration;
import org.eclipse.mosaic.interactions.mapping.TrafficLightRegistration;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.interactions.traffic.TrafficDetectorUpdates;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.lib.objects.vehicle.*;

import org.eclipse.mosaic.rti.api.Interaction;

import java.util.Arrays;
import java.util.List;

class ZmqMosaicInterface {
    static GenericInteract add(Interaction interaction) throws Exception {
        GenericInteract.Builder generic = GenericInteract.newBuilder();

        generic.setTime(interaction.getTime());
        generic.setId(interaction.getId());
        generic.setSenderId(interaction.getSenderId());
        generic.setTypeId(interaction.getTypeId());

        if (interaction instanceof V2xMessageReception) {
            V2xMessageReception v2xinteraction = V2xMessageReception.class.cast(interaction);
            GenericInteract.V2xMessageReceptionBuf.Builder builder = 
                GenericInteract.V2xMessageReceptionBuf.newBuilder();

            builder.setReceiverName(v2xinteraction.getReceiverName());
            builder.setMessageId(v2xinteraction.getMessageId());
            builder.setSendTime(v2xinteraction.getReceiverInformation().getSendTime());
            builder.setReceiveTime(v2xinteraction.getReceiverInformation().getReceiveTime());
            builder.setReceiveSignalStrength(v2xinteraction.getReceiverInformation().getReceiveSignalStrength());
        
        } else if (interaction instanceof V2xMessageTransmission) {
            NoOp();
        
        } else if (interaction instanceof ChargingStationUpdate) {
            NoOp();
        
        } else if (interaction instanceof ChargingStationRegistration) {
            NoOp();
        
        } else if (interaction instanceof RsuRegistration) {
            RsuRegistration rsuInteraction = RsuRegistration.class.cast(interaction);
            GenericInteract.RsuRegistrationBuf.Builder builder = 
                GenericInteract.RsuRegistrationBuf.newBuilder();
            builder.setName(rsuInteraction.getMapping().getName());
            double lat = (rsuInteraction.getMapping().getPosition().getLatitude());
            double lon = (rsuInteraction.getMapping().getPosition().getLongitude());
            double alt = (rsuInteraction.getMapping().getPosition().getAltitude());

            List<Double> position = Arrays.asList(lat, lon, alt);
            builder.addAllPosition(position);
        
        } else if (interaction instanceof TmcRegistration) {
            NoOp();
        
        } else if (interaction instanceof TrafficLightRegistration) {
            NoOp();
        
        } else if (interaction instanceof VehicleRegistration) {
            // cast interaction to VehicleRegistration
            VehicleRegistration vehicleRegistration = VehicleRegistration.class.cast(interaction);
            // builder for VehicleRegistrationProto
            GenericInteract.VehicleRegistrationBuf.Builder builder = 
                GenericInteract.VehicleRegistrationBuf.newBuilder();
            builder.setName(vehicleRegistration.getMapping().getName());
            builder.setGroup(vehicleRegistration.getMapping().getGroup());
            List<String> applications = vehicleRegistration.getMapping().getApplications();
            builder.addAllApplications(applications);
        
        } else if (interaction instanceof TrafficDetectorUpdates) {
            NoOp();
        
        } else if (interaction instanceof VehicleUpdates) {
            VehicleUpdates vehicleUpdates = VehicleUpdates.class.cast(interaction);
            GenericInteract.VehicleUpdatesBuf.Builder vehicle_updates_builder = 
                GenericInteract.VehicleUpdatesBuf.newBuilder();
            GenericInteract.VehicleDataBuf.Builder vehicle_data_builder = 
                GenericInteract.VehicleDataBuf.newBuilder();

            vehicle_updates_builder.setTime(vehicleUpdates.getTime());

            List<VehicleData> addedVehicles = vehicleUpdates.getAdded();
            for (VehicleData element : addedVehicles) {
                vehicle_data_builder.setTime(element.getTime());
                vehicle_data_builder.setName(element.getName());
                double lat = (element.getPosition().getLatitude());
                double lon = (element.getPosition().getLongitude());
                double alt = (element.getPosition().getAltitude());
    
                List<Double> position = Arrays.asList(lat, lon, alt);
                vehicle_data_builder.addAllPosition(position);
                vehicle_data_builder.setRoadId(element.getRoadPosition().getConnectionId());
                vehicle_data_builder.setRouteId(element.getRouteId());
                vehicle_data_builder.setSpeed(element.getSpeed());
                vehicle_data_builder.setLongitudinalAcc(element.getLongitudinalAcceleration());
                
                vehicle_updates_builder.addAdded(vehicle_data_builder);
            }

            List<VehicleData> updatedVehicles = vehicleUpdates.getUpdated();
            for (VehicleData element : updatedVehicles) {
                vehicle_data_builder.setTime(element.getTime());
                vehicle_data_builder.setName(element.getName());
                double lat = (element.getPosition().getLatitude());
                double lon = (element.getPosition().getLongitude());
                double alt = (element.getPosition().getAltitude());
    
                List<Double> position = Arrays.asList(lat, lon, alt);
                vehicle_data_builder.addAllPosition(position);
                vehicle_data_builder.setRoadId(element.getRoadPosition().getConnectionId());
                vehicle_data_builder.setRouteId(element.getRouteId());
                vehicle_data_builder.setSpeed(element.getSpeed());
                vehicle_data_builder.setLongitudinalAcc(element.getLongitudinalAcceleration());
                
                vehicle_updates_builder.addAdded(vehicle_data_builder);
            }

        } else {
            throw new Exception("Unknown interaction type");
        }
        return generic.build();
    }

    private static <T extends Interaction> Object castInteraction(Interaction interaction, Class<T> clazz) {
        return null;
    }
    private static void NoOp() {
    }
}

