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

package org.eclipse.mosaic.lib.objects.vehicle;

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
    PARK,
    /**
     * Parks the vehicle at a parking area. The vehicle has to be close to the parking area.
     */
    PARKING_AREA;

    /**
     * Returns the corresponding integer for different stop modes according to
     * <a href="https://sumo.dlr.de/docs/TraCI/Change_Vehicle_State.html#stop_0x12">stop</a>.
     *
     * @return the corresponding int to the stop mode
     */
    public int stopModeToInt() {
        switch (this) {
            case STOP:
                return 0;
            case PARK:
                return 1;
            case PARKING_AREA: // these flags are additive (see sumo docs)
                return 64 + PARK.stopModeToInt();
            case NOT_STOPPED:
            default:
                return -1;
        }
    }

}
