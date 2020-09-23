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
import org.eclipse.mosaic.fed.sumo.traci.constants.TraciDatatypes;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.util.objects.Position;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

public class Position2dTraciReader extends AbstractTraciResultReader<Position> {

    private static Logger log = LoggerFactory.getLogger(Position2dTraciReader.class);

    public Position2dTraciReader() {
        this(null);
    }

    protected Position2dTraciReader(Matcher<Position> matcher) {
        super(matcher);
    }

    @Override
    protected Position readFromStream(DataInputStream in) throws IOException {
        double x = readDouble(in);
        double y = readDouble(in);

        if (isValid(x) && isValid(y)) {
            try {
                return new Position(CartesianPoint.xyz(x, y, 0));
            } catch (Exception e) {
                log.warn("Could not transform read position at ({}, {})", x, y);
            }
        }
        return Position.INVALID;
    }

    private boolean isValid(double val) {
        return val > TraciDatatypes.INVALID_VALUE;
    }
}
