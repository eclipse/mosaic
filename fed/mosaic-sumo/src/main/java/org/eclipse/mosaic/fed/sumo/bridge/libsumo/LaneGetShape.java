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

package org.eclipse.mosaic.fed.sumo.bridge.libsumo;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.util.objects.Position;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.eclipse.sumo.libsumo.Lane;
import org.eclipse.sumo.libsumo.TraCPositionVector;

import java.util.ArrayList;
import java.util.List;

public class LaneGetShape implements org.eclipse.mosaic.fed.sumo.bridge.api.LaneGetShape {
    @Override
    public List<Position> execute(Bridge bridge, String laneId) throws CommandException, InternalFederateException {
        TraCPositionVector shapeVector = Lane.getShape(laneId).getValue();
        List<Position> shape = new ArrayList<>();
        for (org.eclipse.sumo.libsumo.TraCIPosition traciPosition : shapeVector) {
            shape.add(new Position(CartesianPoint.xy(traciPosition.getX(), traciPosition.getY())));
        }
        return shape;
    }
}