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

package org.eclipse.mosaic.fed.sumo.traci.facades;

import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.TraciConnection;
import org.eclipse.mosaic.fed.sumo.traci.commands.PoiAdd;
import org.eclipse.mosaic.fed.sumo.traci.commands.PoiSetAngle;
import org.eclipse.mosaic.fed.sumo.traci.commands.PoiSetHeight;
import org.eclipse.mosaic.fed.sumo.traci.commands.PoiSetImage;
import org.eclipse.mosaic.fed.sumo.traci.commands.PoiSetWidth;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;

public class TraciPoiFacade {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final TraciConnection traciConnection;
    private final PoiSetImage poiSetImage;
    private final PoiSetAngle poiSetAngle;
    private final PoiSetHeight poiSetHeight;
    private final PoiSetWidth poiSetWidth;
    private final PoiAdd poiAdd;


    public TraciPoiFacade(TraciConnection traciConnection) {
        this.traciConnection = traciConnection;

        this.poiAdd = traciConnection.getCommandRegister().getOrCreate(PoiAdd.class);
        this.poiSetWidth = traciConnection.getCommandRegister().getOrCreate(PoiSetWidth.class);
        this.poiSetHeight = traciConnection.getCommandRegister().getOrCreate(PoiSetHeight.class);
        this.poiSetAngle = traciConnection.getCommandRegister().getOrCreate(PoiSetAngle.class);
        this.poiSetImage = traciConnection.getCommandRegister().getOrCreate(PoiSetImage.class);
    }

    public void addImagePoi(String id, CartesianPoint position, String imagePath, double width, double height, double angle) throws InternalFederateException {
        addSimplePoi(id, position, Color.WHITE);
        changeImage(id, imagePath, width, height, angle);
    }

    public void changeImage(String id, String imagePath) throws InternalFederateException {
        try {
            poiSetImage.execute(traciConnection, id, imagePath);
        } catch (TraciCommandException e) {
            log.error("Could not change image of POI", e);
        }
    }

    public void changeImage(String id, String imagePath, double width, double height, double angle) throws InternalFederateException {
        try {
            poiSetImage.execute(traciConnection, id, imagePath);
            poiSetWidth.execute(traciConnection, id, width);
            poiSetHeight.execute(traciConnection, id, height);
            poiSetAngle.execute(traciConnection, id, angle);
        } catch (TraciCommandException e) {
            log.error("Could not change image of POI", e);
        }
    }

    public void addSimplePoi(String id, CartesianPoint position, Color color) throws InternalFederateException {
        try {
            poiAdd.execute(traciConnection, id, 1000, position, color);
        } catch (TraciCommandException e) {
            log.error("Could not add POI", e);
        }
    }
}
