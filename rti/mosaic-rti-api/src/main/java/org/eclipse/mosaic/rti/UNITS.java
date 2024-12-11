/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.rti;

public final class UNITS {

    private UNITS() {
        // access to constants only
    }

    /**
     * Distance of one meter.
     */
    public static final double METER = 1;

    /**
     * Distance of one kilometer.
     */
    public static final double KILOMETER = 1000 * METER;

    /**
     * Distance of one mile in meters.
     */
    public static final double MILE = 1609.344 * METER;

    /**
     * Speed of one meter per second.
     */
    public static final double METER_PER_SECOND = 1;

    /**
     * Speed of one kilometer per hour in meter per second.
     */
    public static final double KILOMETER_PER_HOUR = 1 / 3.6;

    /**
     * Speed of one mile per hour in meter per second.
     */
    public static final double MILES_PER_HOUR = 1.609344 * KILOMETER_PER_HOUR;

    /**
     * Speed of one kilometer per hour in meter per second.
     */
    public static final double KMH = KILOMETER_PER_HOUR;

    /**
     * Speed of one mile per hour in meter per second.
     */
    public static final double MPH = MILES_PER_HOUR;
}
