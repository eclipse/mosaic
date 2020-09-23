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
 */

package org.eclipse.mosaic.fed.sumo.util;

import org.eclipse.mosaic.lib.enums.VehicleClass;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

public class SumoVehicleClassMapping {

    private final static BiMap<String, VehicleClass> vehicleClassBiMap = new ImmutableBiMap.Builder<String, VehicleClass>()
            .put("private", VehicleClass.Unknown)
            .put("passenger", VehicleClass.Car)
            .put("delivery", VehicleClass.LightGoodsVehicle)
            .put("truck", VehicleClass.HeavyGoodsVehicle)
            .put("bus", VehicleClass.PublicTransportVehicle)
            .put("emergency", VehicleClass.EmergencyVehicle)
            .put("coach", VehicleClass.WorksVehicle)
            .put("army", VehicleClass.ExceptionalSizeVehicle)
            .put("trailer", VehicleClass.VehicleWithTrailer)
            .put("vip", VehicleClass.HighSideVehicle)
            .put("taxi", VehicleClass.Taxi)
            .put("evehicle", VehicleClass.ElectricVehicle)
            .put("custom1", VehicleClass.AutomatedVehicle)
            .put("bicycle", VehicleClass.Bicycle)
            .put("motorcycle", VehicleClass.Motorcycle)
            .put("hov", VehicleClass.HighOccupancyVehicle)
            .build();

    public static String toSumo(VehicleClass vehicleClass) {
        return vehicleClassBiMap.inverse().getOrDefault(vehicleClass, "passenger");
    }

    public static VehicleClass fromSumo(String sumoVehicleClass) {
        return vehicleClassBiMap.getOrDefault(sumoVehicleClass, VehicleClass.Car);
    }
}
