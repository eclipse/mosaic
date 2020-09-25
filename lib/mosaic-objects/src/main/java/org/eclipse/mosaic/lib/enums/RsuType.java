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
 * Enumeration of rsu types.
 */
public enum RsuType {

    UNKNOWN(0),
    STATIC_TRAFFIC_SIGN(1),
    VARIABLE_TRAFFIC_SIGN(2),
    STATIC_ROADWORKS(3),
    MOVING_ROADWORKS(4),
    REPEATER(5),
    TRAFFIC_LIGHT(6),
    CHARGING_STATION(7);
    
    public final int id;
    
    RsuType(int id) {
        this.id = id;
    }
    
    /**
     * Returns the enum mapped from an integer.
     *
     * @param id identifying integer
     * @return the enum mapped from an integer.
     */
    public static RsuType fromId(int id) {
        for (RsuType type: RsuType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown RsuType id " + id);
    }
}

