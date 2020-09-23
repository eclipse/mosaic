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

package org.eclipse.mosaic.lib.enums;

/**
 * Represents the direction a vehicle is driving in.
 */
public enum DriveDirection {
    FORWARD(0),
    BACKWARD(1),
    UNAVAILABLE(2);

    public final int id;

    DriveDirection(int id) {
        this.id = id;
    }

    /**
     * Returns the enum mapped from an integer.
     *
     * @param id identifying integer
     * @return the enum mapped from an integer.
     */
    public static DriveDirection fromId(int id) {
        for (DriveDirection direction: DriveDirection.values()) {
            if (direction.id == id) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Unknown DriveDirection id " + id);
    }
}
