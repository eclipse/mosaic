/*
 * Copyright (c) 2023 Fraunhofer FOKUS and others. All rights reserved.
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
import org.eclipse.mosaic.lib.objects.vehicle.PublicTransportData;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class StoppingPlaceReader extends AbstractTraciResultReader<List<PublicTransportData.StoppingPlace>> {

    public StoppingPlaceReader() {
        this(null);
    }

    protected StoppingPlaceReader(@Nullable Matcher<List<PublicTransportData.StoppingPlace>> matcher) {
        super(matcher);
    }

    @Override
    protected List<PublicTransportData.StoppingPlace> readFromStream(DataInputStream in) throws IOException {
        int count = readIntWithType(in);
        List<PublicTransportData.StoppingPlace> stoppingPlaces = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            PublicTransportData.StoppingPlace.Builder stoppingPlaceBuilder = new PublicTransportData.StoppingPlace.Builder();
            stoppingPlaceBuilder.laneId(readStringWitType(in));
            stoppingPlaceBuilder.endPos(readDoubleWithType(in));
            stoppingPlaceBuilder.stoppingPlaceId(readStringWitType(in));
            stoppingPlaceBuilder.stopFlags(VehicleStopMode.fromSumoInt(readIntWithType(in)));
            stoppingPlaceBuilder.stopDuration(readDoubleWithType(in));
            stoppingPlaceBuilder.stoppedUntil(readDoubleWithType(in));

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
