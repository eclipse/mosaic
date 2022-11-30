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

import org.eclipse.mosaic.lib.enums.VehicleStopMode;
import org.eclipse.mosaic.lib.objects.vehicle.StoppingPlace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class StoppingPlaceReader extends AbstractTraciResultReader<List<StoppingPlace>> {

    private static Logger log = LoggerFactory.getLogger(StoppingPlaceReader.class);

    public StoppingPlaceReader() {
        this(null);
    }

    protected StoppingPlaceReader(@Nullable Matcher<List<StoppingPlace>> matcher) {
        super(matcher);
    }

    @Override
    protected List<StoppingPlace> readFromStream(DataInputStream in) throws IOException {
        int count = readIntWithType(in); // number of entries * 4 + 1 (don't know the use case)
        int size = readIntWithType(in);
        List<StoppingPlace> stoppingPlaces = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            StoppingPlace.Builder stoppingPlaceBuilder = new StoppingPlace.Builder();
            stoppingPlaceBuilder.laneId(readStringWitType(in));
            stoppingPlaceBuilder.endPos(readDoubleWithType(in));
            stoppingPlaceBuilder.stoppingPlaceId(readStringWitType(in));
            stoppingPlaceBuilder.stopFlags(VehicleStopMode.fromSumoInt(readIntWithType(in)));
            stoppingPlaceBuilder.stopDuration(readDoubleWithType(in));
            stoppingPlaceBuilder.stoppedUntil(readDoubleWithType(in));
            stoppingPlaceBuilder.startPos(readDoubleWithType(in));
            // read unused fields
            readDoubleWithType(in); // intended arrival
            readDoubleWithType(in); // arrival
            readDoubleWithType(in); // depart
            readStringWitType(in); // split
            readStringWitType(in); // join
            readStringWitType(in); // actType
            readStringWitType(in); // tripId
            readStringWitType(in); // line
            readDoubleWithType(in); // speed
            stoppingPlaces.add(stoppingPlaceBuilder.build());
        }
        return stoppingPlaces;
    }

    private String readStringWitType(DataInputStream in) throws IOException {
        readByte(in);
        return readString(in);
    }

    private double readDoubleWithType(DataInputStream in) throws IOException {
        readByte(in);
        return readDouble(in);
    }

    private int readIntWithType(DataInputStream in) throws IOException {
        readByte(in);
        return readInt(in);
    }
}
