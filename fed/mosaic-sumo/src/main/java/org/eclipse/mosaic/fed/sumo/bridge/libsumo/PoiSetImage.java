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

package org.eclipse.mosaic.fed.sumo.bridge.libsumo;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.eclipse.sumo.libsumo.POI;

/**
 * This class sets the image path of a PoI object in the SUMO-GUI.
 */
public class PoiSetImage implements org.eclipse.mosaic.fed.sumo.bridge.api.PoiSetImage {
    /**
     * Executes the command in order to set the image path of a previously added PoI object.
     *
     * @param bridge Connection to SUMO.
     * @param poiId     the unique ID of the PoI to remove
     * @param imagePath the path to the image file (must be accessible relatively to the execution directory of SUMO)
     * @throws CommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(Bridge bridge, String poiId, String imagePath) throws CommandException, InternalFederateException {
        POI.setImageFile(poiId, imagePath);
    }
}
