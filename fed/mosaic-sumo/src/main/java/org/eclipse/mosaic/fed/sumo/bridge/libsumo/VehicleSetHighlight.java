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

import org.eclipse.sumo.libsumo.TraCIColor;
import org.eclipse.sumo.libsumo.Vehicle;

import java.awt.Color;

/**
 * This class highlights a specific vehicle in the SUMO-GUI.
 */
public class VehicleSetHighlight implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetHighlight {
    /**
     * This method executes the command with the given arguments in order to highlight a vehicle in the SUMO-GUI with a circle
     * which is visible for 10 seconds.
     *
     * @param traciCon  Connection to Traci.
     * @param vehicleId Id of the vehicle.
     * @param color     the color to highlight the vehicle with
     * @throws CommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(Bridge traciCon, String vehicleId, Color color) throws CommandException, InternalFederateException {
        Vehicle.highlight(vehicleId, new TraCIColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
    }
}
