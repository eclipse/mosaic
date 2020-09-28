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

package org.eclipse.mosaic.fed.sumo.traci.reader;

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciResultReader;

import java.io.DataInputStream;
import java.io.IOException;

public class ByteArrayReader extends AbstractTraciResultReader<byte[]> {

    private final int length;

    public ByteArrayReader() {
        this(Integer.MAX_VALUE);
    }

    public ByteArrayReader(int length) {
        super(null);
        this.length = length;
    }

    @Override
    protected byte[] readFromStream(DataInputStream in) throws IOException {
        return readFullyByLength(in, Math.min(length, totalBytesLeft));
    }
}
