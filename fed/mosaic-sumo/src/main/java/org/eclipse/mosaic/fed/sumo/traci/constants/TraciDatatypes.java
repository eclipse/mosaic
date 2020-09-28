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

package org.eclipse.mosaic.fed.sumo.traci.constants;

public class TraciDatatypes {

    /**
     * 8Bit unsigned byte.
     */
    public static final byte UBYTE = 0x07;
    /**
     * 8Bit signed byte.
     */
    public static final byte BYTE = 0x08;
    /**
     * 32 Bit Integer number.
     */
    public static final byte INTEGER = 0x09;

    /**
     * 4-byte array for colors.
     */
    public static final byte UBYTE_COLOR = 0x11;

    /**
     * 32 Bit floating point number.
     */
    public static final byte FLOAT = 0x0A;

    /**
     * 64 Bit floating point number.
     */
    public static final byte DOUBLE = 0x0B;

    /**
     * List of Strings, starting with a number referring to the length of the list.
     */
    public static final byte STRING_LIST = 0x0E;

    /**
     * Compound object data type.
     */
    public static final byte COMPOUND = 0x0F;

    /**
     * returns 32 bit string length, followed by text coded as 8 bit ASCII.
     */
    public static final byte STRING = 0x0C;

    /**
     * 2DPosition.
     */
    public static final byte POSITION2D = 0x01;

    /**
     * 3DPosition.
     */
    public static final byte POSITION3D = 0x03;

    /**
     * Invalid value returned by TraCI, e.g. for positions of vehicles which are not simulated yet.
     */
    public static final int INVALID_VALUE = -1001;
}
