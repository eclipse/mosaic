/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

/**
 * Describes how to stop the vehicle / how the vehicle is stopped.
 */
public enum VehicleStopMode {

    /**
     * Vehicle is not/will not be stopped.
     */
    NOT_STOPPED,
    /**
     * Stops the vehicle on the specified lane index. Other vehicles,
     * which approach behind the stopped vehicle, might be disrupted.
     */
    STOP,
    /**
     * Parks the vehicle at the road side. Other vehicles won't be
     * disrupted by the parking vehicle.
     */
    PARK_ON_ROADSIDE,
    /**
     * Stops the vehicle at a bus or train stop.
     */
    BUS_STOP,
    /**
     * Parks the vehicle at a parking area. The vehicle has to be close to the parking area.
     */
    PARK_IN_PARKING_AREA;

    public boolean isParking() {
        return this == PARK_ON_ROADSIDE || this == PARK_IN_PARKING_AREA;
    }


    /**
     * Getter for the stop mode (stop, park).
     *
     * @param stopFlag Encoded number indicating the stop mode.
     * @return The stop mode.
     */
    public static VehicleStopMode fromSumoInt(int stopFlag) {
        if ((stopFlag & 0b110000000) > 0) {
            return VehicleStopMode.PARK_IN_PARKING_AREA;
        }
        if ((stopFlag & 0b10000) > 0) {
            return VehicleStopMode.BUS_STOP;
        }
        if ((stopFlag & 0b0010) > 0) {
            return VehicleStopMode.PARK_ON_ROADSIDE;
        }
        if ((stopFlag & 0b0001) > 0) {
            return VehicleStopMode.STOP;
        }
        return VehicleStopMode.NOT_STOPPED;
    }

    /**
     * Returns the corresponding integer for different stop modes according to
     * <a href="https://sumo.dlr.de/docs/TraCI/Change_Vehicle_State.html#stop_0x12">stop</a>.
     *
     * @return the corresponding int to the stop mode
     */
    public static int toSumoInt(VehicleStopMode stopMode) {
        return switch (stopMode) {
            case STOP -> 0;
            case PARK_ON_ROADSIDE -> 1;
            case BUS_STOP -> 8;
            case PARK_IN_PARKING_AREA -> // these flags are additive (see sumo docs)
                    64 + toSumoInt(VehicleStopMode.PARK_ON_ROADSIDE);
            default -> -1;
        };
    }
}
