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

package org.eclipse.mosaic.rti;

import org.apache.commons.lang3.StringUtils;

/**
 * Constant time units in MOSAIC. The base unit for simulation time is one nano second.
 */
public final class TIME {

    private TIME() {
        // access to constants only
    }

    /**
     * One nanosecond in simulation time.
     */
    public static final long NANO_SECOND = 1;

    /**
     * One microsecond in simulation time.
     */
    public static final long MICRO_SECOND = 1000 * NANO_SECOND;

    /**
     * One millisecond in simulation time .
     */
    public static final long MILLI_SECOND = 1000 * MICRO_SECOND;

    /**
     * One second in simulation time.
     */
    public static final long SECOND = 1000 * MILLI_SECOND;

    /**
     * One minute in simulation time.
     */
    public static final long MINUTE = 60 * SECOND;

    /**
     * One hour in simulation time.
     */
    public static final long HOUR = 60 * MINUTE;

    /**
     * Format a nanosecond to print a time stamp in a readable format.
     * <p>
     * E.g. {@code TIME.format(356000010000) -> "356.000,010,000 s"}
     * </p>
     *
     * @param nanosecond the nanoseconds to format
     * @return the formatted value
     */
    public static String format(long nanosecond) {
        final String nanosecondString = String.valueOf(nanosecond);
        final int leadingZeros = 10;
        final StringBuilder sb = new StringBuilder(StringUtils.repeat("0", Math.max(0, leadingZeros - nanosecondString.length())));

        sb.append(nanosecondString);
        // insert spacing comma
        sb.insert(sb.length() - 9, '.');
        sb.insert(sb.length() - 6, ',');
        sb.insert(sb.length() - 3, ',');
        sb.append(" s");

        return sb.toString();
    }

}
