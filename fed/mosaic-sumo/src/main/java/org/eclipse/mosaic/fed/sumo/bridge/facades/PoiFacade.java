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
 *
 * Contact: mosaic@fokus.fraunhofer.de
 */

package org.eclipse.mosaic.fed.sumo.bridge.facades;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.api.PoiAdd;
import org.eclipse.mosaic.fed.sumo.bridge.api.PoiSetAngle;
import org.eclipse.mosaic.fed.sumo.bridge.api.PoiSetHeight;
import org.eclipse.mosaic.fed.sumo.bridge.api.PoiSetImage;
import org.eclipse.mosaic.fed.sumo.bridge.api.PoiSetWidth;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;

public class PoiFacade {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Bridge bridge;
    private final PoiSetImage poiSetImage;
    private final PoiSetAngle poiSetAngle;
    private final PoiSetHeight poiSetHeight;
    private final PoiSetWidth poiSetWidth;
    private final PoiAdd poiAdd;


    public PoiFacade(Bridge bridge) {
        this.bridge = bridge;

        this.poiAdd = bridge.getCommandRegister().getOrCreate(PoiAdd.class);
        this.poiSetWidth = bridge.getCommandRegister().getOrCreate(PoiSetWidth.class);
        this.poiSetHeight = bridge.getCommandRegister().getOrCreate(PoiSetHeight.class);
        this.poiSetAngle = bridge.getCommandRegister().getOrCreate(PoiSetAngle.class);
        this.poiSetImage = bridge.getCommandRegister().getOrCreate(PoiSetImage.class);
    }

    public void addImagePoi(String id, CartesianPoint position, String imagePath, double width, double height, double angle) throws InternalFederateException {
        addSimplePoi(id, position, Color.WHITE);
        changeImage(id, imagePath, width, height, angle);
    }

    public void changeImage(String id, String imagePath) throws InternalFederateException {
        try {
            poiSetImage.execute(bridge, id, imagePath);
        } catch (CommandException e) {
            log.error("Could not change image of POI", e);
        }
    }

    public void changeImage(String id, String imagePath, double width, double height, double angle) throws InternalFederateException {
        try {
            poiSetImage.execute(bridge, id, imagePath);
            poiSetWidth.execute(bridge, id, width);
            poiSetHeight.execute(bridge, id, height);
            poiSetAngle.execute(bridge, id, angle);
        } catch (CommandException e) {
            log.error("Could not change image of POI", e);
        }
    }

    public void addSimplePoi(String id, CartesianPoint position, Color color) throws InternalFederateException {
        try {
            poiAdd.execute(bridge, id, 1000, position, color);
        } catch (CommandException e) {
            log.error("Could not add POI", e);
        }
    }
}
