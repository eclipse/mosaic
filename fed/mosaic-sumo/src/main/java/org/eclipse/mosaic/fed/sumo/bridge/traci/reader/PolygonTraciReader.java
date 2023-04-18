/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.sumo.bridge.traci.reader;

import org.eclipse.mosaic.lib.util.objects.Position;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class PolygonTraciReader extends AbstractTraciResultReader<List<Position>> {

    Position2dTraciReader positionReader = new Position2dTraciReader();

    public PolygonTraciReader() {
        this(null);
    }

    public PolygonTraciReader(@Nullable Matcher<List<Position>> matcher) {
        super(matcher);
    }

    @Override
    protected List<Position> readFromStream(DataInputStream in) throws IOException {
        int count = readUnsignedByte(in);

        List<Position> resultPolygon = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            resultPolygon.add(positionReader.read(in, totalBytesLeft - numBytesRead));
            numBytesRead += positionReader.getNumberOfBytesRead();
        }

        return resultPolygon;
    }
}
