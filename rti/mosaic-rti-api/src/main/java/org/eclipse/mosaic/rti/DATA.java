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

/**
 * Constant data units in MOSAIC. The base unit for data size is one bit.
 */
public final class DATA {

    private DATA() {
        // access to constants only
    }

    /**
     * 1 bit.
     */
    public static final long BIT = 1;

    /**
     * 1 kilobit = 1000 bits.
     */
    public static final long KILOBIT = 1000 * BIT;

    /**
     * 1 megabit = 1000 kilobits.
     */
    public static final long MEGABIT = 1000 * KILOBIT;

    /**
     * 1 gigabit = 1000 megabits.
     */
    public static final long GIGABIT = 1000 * MEGABIT;

    /**
     * 1 terabit = 1000 gigabits.
     */
    public static final long TERABIT = 1000 * GIGABIT;

    /**
     * 1 kibibit = 1024 bits.
     */
    public static final long KIBIBIT = 1024 * BIT;

    /**
     * 1 mebibit = 1024 kibibits.
     */
    public static final long MEBIBIT = 1024 * KIBIBIT;

    /**
     * 1 gibibit = 1024 mebibits.
     */
    public static final long GIBIBIT = 1024 * MEBIBIT;

    /**
     * 1 tebibit = 1024 gibibits.
     */
    public static final long TEBIBIT = 1024 * GIBIBIT;

    /**
     * 1 byte = 8 bits.
     */
    public static final long BYTE = 8 * BIT;

    /**
     * 1 kilobyte = 1000 bytes.
     */
    public static final long KILOBYTE = 1000 * BYTE;

    /**
     * 1 megabyte = 1000 kilobytes.
     */
    public static final long MEGABYTE = 1000 * KILOBYTE;

    /**
     * 1 gigabyte = 1000 megabytes.
     */
    public static final long GIGABYTE = 1000 * MEGABYTE;

    /**
     * 1 terabyte = 1000 gigabytes.
     */
    public static final long TERABYTE = 1000 * GIGABYTE;

    /**
     * 1 kibibyte = 1024 bytes.
     */
    public static final long KIBIBYTE = 1024 * BYTE;

    /**
     * 1 mebibyte = 1024 kibibytes.
     */
    public static final long MEBIBYTE = 1024 * KIBIBYTE;

    /**
     * 1 gibibyte = 1024 mebibytes.
     */
    public static final long GIBIBYTE = 1024 * MEBIBYTE;

    /**
     * 1 tebibyte = 1024 gibibytes.
     */
    public static final long TEBIBYTE = 1024 * GIBIBYTE;
}
