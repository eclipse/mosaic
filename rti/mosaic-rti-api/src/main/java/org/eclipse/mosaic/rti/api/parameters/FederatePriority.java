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

package org.eclipse.mosaic.rti.api.parameters;

/**
 * Constant values for prioritizing federates.
 */
public final class FederatePriority {

    /**
     * The highest priority possible (= 0).
     */
    public static final byte HIGHEST = 0;

    /**
     * The lowest priority possible (= 100).
     */
    public static final byte LOWEST = 100;

    /**
     * The default priority assigned to all federates (= 50).
     */
    public static final byte DEFAULT = (HIGHEST + LOWEST) / 2;

    public static boolean isInRange(int priority) {
        return priority > LOWEST || priority < HIGHEST;
    }

    /**
     * Returns a priority which is higher than the given one, if the given priority is not already HIGHEST.
     */
    public static byte higher(byte priority) {
        return (byte) Math.max(HIGHEST, priority - 1);
    }

    /**
     * Returns a priority which is lower than the given one, if the given priority is not already LOWEST.
     */
    public static byte lower(byte priority) {
        return (byte) Math.min(LOWEST, priority + 1);
    }

    /**
     * Compares two priority values with each other.
     * If {@code a} has a higher priority than {@code b}, a value larger than 0 is returned.
     * If {@code a} has a lower priority than {@code b}, a value smaller than 0 is returned.
     * If {@code a} and  {@code b} have same priority, 0 is returned.
     */
    public static int compareTo(byte a, byte b) {
        if (a == b) {
            return 0;
        }
        return a < b ? 1 : -1;
    }
}
