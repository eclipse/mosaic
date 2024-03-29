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

package org.eclipse.mosaic.fed.sumo.bridge.traci;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.SumoVersion;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.Status;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandChangeVehicleValue;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.TraciDatatypes;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.awt.Color;

/**
 * This class highlights a specific vehicle in the SUMO-GUI.
 */
public class VehicleSetHighlight
        extends AbstractTraciCommand<Void>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSetHighlight {

    /**
     * Creates a new {@link VehicleSetHighlight} object.
     * Access needs to be public, because command is called using Reflection.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_Vehicle_State.html">Vehicle State Change</a>
     */
    @SuppressWarnings("WeakerAccess")
    public VehicleSetHighlight() {
        super(SumoVersion.SUMO_1_7_x);

        write()
                .command(CommandChangeVehicleValue.COMMAND)
                .variable(CommandChangeVehicleValue.VAR_HIGHLIGHT)
                .writeVehicleIdParam()
                .writeByte(TraciDatatypes.COMPOUND)
                .writeInt(4)
                .writeByte(TraciDatatypes.UBYTE_COLOR).writeByteParam().writeByteParam().writeByteParam().writeByte(255)
                .writeByte(TraciDatatypes.DOUBLE).writeDouble(5d) // radius of the circle
                .writeByte(TraciDatatypes.UBYTE).writeByte(0) // alpha value
                .writeByte(TraciDatatypes.DOUBLE).writeDouble(10d); // the duration of the circle to show as a highlight
    }

    /**
     * This method executes the command with the given arguments in order to highlight a vehicle in the SUMO-GUI with a circle
     * which is visible for 10 seconds.
     *
     * @param bridge Connection to SUMO.
     * @param vehicleId Id of the vehicle.
     * @param color     the color to highlight the vehicle with
     * @throws CommandException     if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(Bridge bridge, String vehicleId, Color color) throws CommandException, InternalFederateException {
        super.execute(bridge, vehicleId, color.getRed(), color.getGreen(), color.getBlue());
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}