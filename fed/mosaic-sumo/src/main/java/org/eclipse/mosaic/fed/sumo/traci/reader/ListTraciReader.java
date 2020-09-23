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
import java.util.ArrayList;
import java.util.List;

public class ListTraciReader<T> extends AbstractTraciResultReader<List<T>> {

    private final AbstractTraciResultReader<T> itemReader;
    private final boolean expectByteBeforeLength;

    public ListTraciReader(AbstractTraciResultReader<T> itemReader) {
        this(itemReader, false);
    }

    public ListTraciReader(AbstractTraciResultReader<T> itemReader, boolean expectByteBeforeLength) {
        super(null);
        this.itemReader = itemReader;
        this.expectByteBeforeLength = expectByteBeforeLength;
    }

    @Override
    protected List<T> readFromStream(DataInputStream in) throws IOException {
        if (expectByteBeforeLength) {
            readByte(in);
        }

        int count = readInt(in);

        List<T> resultList = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            resultList.add(itemReader.read(in, totalBytesLeft - numBytesRead));
            numBytesRead += itemReader.getNumberOfBytesRead();
        }

        return resultList;
    }
}
