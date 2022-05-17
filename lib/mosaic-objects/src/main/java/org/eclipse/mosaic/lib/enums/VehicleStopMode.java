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
     * Parks the vehicle at a parking area. The vehicle has to be close to the parking area.
     */
    PARK_IN_PARKING_AREA;

    public boolean isParking() {
        return this == PARK_ON_ROADSIDE || this == PARK_IN_PARKING_AREA;
    }
}
