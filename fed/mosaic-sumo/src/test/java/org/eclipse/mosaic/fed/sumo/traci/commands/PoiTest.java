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

package org.eclipse.mosaic.fed.sumo.traci.commands;

import org.eclipse.mosaic.lib.geo.CartesianPoint;

import org.junit.Test;

import java.awt.Color;

public class PoiTest extends AbstractTraciCommandTest {

    @Test
    public void executeAddChangeAndRemove() throws Exception {
        //SETUP
        String id = "poi_0";
        String image = "poi.png";
        CartesianPoint position = CartesianPoint.xyz(100, 200, 0);
        double[] dimension = new double[]{10d, 20d, 180d};

        new PoiAdd().execute(traci.getTraciConnection(), id, 0, position, Color.BLACK);
        new PoiSetImage().execute(traci.getTraciConnection(), id, image);
        new PoiSetWidth().execute(traci.getTraciConnection(), id, dimension[0]);
        new PoiSetHeight().execute(traci.getTraciConnection(), id, dimension[1]);
        new PoiSetAngle().execute(traci.getTraciConnection(), id, dimension[2]);
        new PoiRemove().execute(traci.getTraciConnection(), id, 0);

    }
}