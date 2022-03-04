/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

import org.eclipse.sumo.libsumo.Junction;
import org.eclipse.sumo.libsumo.TraCIPosition;

public class JunctionGetPosition implements org.eclipse.mosaic.fed.sumo.bridge.api.JunctionGetPosition {

    public Position execute(Bridge bridge, String junctionId) throws CommandException {
        try {
            TraCIPosition traCIPosition = Junction.getPosition(junctionId);
            try {
                return new Position(CartesianPoint.xyz(traCIPosition.getX(), traCIPosition.getY(), traCIPosition.getZ() < -1000 ? 0 : traCIPosition.getZ()));
            } finally {
                traCIPosition.delete();
            }
        } catch(IllegalArgumentException e) {
            throw new CommandException("Could not position of junction with ID: " + junctionId);
        }

    }
}
