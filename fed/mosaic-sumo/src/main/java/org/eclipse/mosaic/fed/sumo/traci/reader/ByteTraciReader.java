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

package org.eclipse.mosaic.fed.sumo.traci.reader;

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciResultReader;

import java.io.DataInputStream;
import java.io.IOException;

public class ByteTraciReader extends AbstractTraciResultReader<Integer> {

    public ByteTraciReader() {
        super(null);
    }

    public ByteTraciReader(Matcher<Integer> matcher) {
        super(matcher);
    }

    @Override
    protected Integer readFromStream(DataInputStream in) throws IOException {
        return readUnsignedByte(in);
    }
}
