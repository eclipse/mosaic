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

package org.eclipse.mosaic.fed.sumo.bridge.traci;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.TraciVersion;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.Status;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveLaneValue;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.TraciDatatypes;
import org.eclipse.mosaic.fed.sumo.bridge.traci.reader.PolygonTraciReader;
import org.eclipse.mosaic.lib.util.objects.Position;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.List;
import java.util.Locale;

public class LaneGetShape extends AbstractTraciCommand<List<Position>>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.LaneGetShape {

    public LaneGetShape() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandRetrieveLaneValue.COMMAND)
                .variable(CommandRetrieveLaneValue.VAR_SHAPE)
                .writeStringParam();

        read()
                .skipBytes(2)
                .skipString()
                .expectByte(TraciDatatypes.POLYGON)
                .readComplex(new PolygonTraciReader());
    }

    @Override
    public List<Position> execute(Bridge bridge, String laneId) throws CommandException, InternalFederateException {
        return super.executeAndReturn(bridge, laneId).orElseThrow(
                () -> new CommandException(
                        String.format(Locale.ENGLISH, "Couldn't get shape of of Lane %s.", laneId)
                )
        );
    }

    @Override
    protected List<Position> constructResult(Status status, Object... objects) {
        return (List<Position>) objects[0];
    }
}