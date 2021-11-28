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

package org.eclipse.mosaic.lib.zeromq.bidirectional;

import java.util.Arrays;

import org.zeromq.ZFrame;
import org.zeromq.ZMQ;

/**
 * Connecticity Protocol definitions
 */
public enum ASYN
{

    /**
     * This is the version of ASYN/Client we implement
     */
    C_CLIENT("ASYNC01"),

    /**
     * This is the version of ASYN/Worker we implement
     */
    W_WORKER("ASYNW01");

    private final byte[] data;

    ASYN(String value)
    {
        this.data = value.getBytes(ZMQ.CHARSET);
    }

    ASYN(int value)
    { //watch for ints>255, will be truncated
        byte b = (byte) (value & 0xFF);
        this.data = new byte[] { b };
    }

    public ZFrame newFrame()
    {
        return new ZFrame(data);
    }

    public boolean frameEquals(ZFrame frame)
    {
        return Arrays.equals(data, frame.getData());
    }
}