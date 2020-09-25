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

/**
 * Enumeration of sensor types that are available in a vehicle.
 */
public enum SensorType {

    FOG(0),
    ICE(1),
    SNOW(2),
    RAIN(3),
    SPEED(17),
    POSITION(18),
    DIRECTION(19),
    CURVE(20),
    OBSTACLE(23),
    PARKING_LOT(24),
    ROADWORKS(25);
    
    public final int id;
    
    /**
     * Default constructor.
     *
     * @param id identifying integer
     */
    SensorType(int id) {
        this.id = id;
    }
    
    /**
     * Returns the enum mapped from an integer.
     *
     * @param id identifying integer
     * @return the enum mapped from an integer.
     */
    public static SensorType fromId(int id) {
        for (SensorType type: SensorType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown SensorType id " + id);
    }
}

