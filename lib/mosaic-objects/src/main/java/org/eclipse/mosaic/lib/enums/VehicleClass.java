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

package org.eclipse.mosaic.lib.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of vehicle types. Adopted from TPEG RTM / ISO-TS18234-4.
 */
public enum VehicleClass {

    Unknown(0),
    Car(1),
    LightGoodsVehicle(2),
    HeavyGoodsVehicle(3),
    PublicTransportVehicle(4),
    EmergencyVehicle(5),
    WorksVehicle(6),
    ExceptionalSizeVehicle(7),
    VehicleWithTrailer(8),
    HighSideVehicle(9),
    MiniBus(10),
    Taxi(11),
    ElectricVehicle(12),
    /* note: this is not provided in ISO-TS18234-4*/
    AutomatedVehicle(13),
    Bicycle(14),
    Motorcycle(15),
    HighOccupancyVehicle(16);

    public final int id;

    /**
     * Default constructor.
     *
     * @param id identifying integer
     */
    VehicleClass(int id) {
        this.id = id;
        VehicleClassIdMapping.idMapping.put(id, this);
    }

    /**
     * Returns the enum mapped from an integer.
     *
     * @param id identifying integer
     * @return the enum mapped from an integer.
     */
    public static VehicleClass fromId(int id) {
        VehicleClass result = VehicleClassIdMapping.idMapping.get(id);
        if (result == null) {
            throw new IllegalArgumentException(String.format("No such VehicleClass for id=%s", id));
        }
        return result;
    }

    private static class VehicleClassIdMapping {
        private static Map<Integer, VehicleClass> idMapping = new HashMap<>();
    }

}
