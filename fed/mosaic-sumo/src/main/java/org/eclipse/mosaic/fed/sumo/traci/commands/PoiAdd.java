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

package org.eclipse.mosaic.fed.sumo.traci.commands;

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciCommand;
import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.TraciConnection;
import org.eclipse.mosaic.fed.sumo.traci.TraciVersion;
import org.eclipse.mosaic.fed.sumo.traci.complex.Status;
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandChangePoiState;
import org.eclipse.mosaic.fed.sumo.traci.constants.TraciDatatypes;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.awt.Color;

/**
 * This class adds a PoI object in the SUMO-GUI.
 */
public class PoiAdd extends AbstractTraciCommand<Void> {

    /**
     * Creates a new {@link PoiAdd} traci command.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_PoI_State.html">Lane State Change</a>
     */
    @SuppressWarnings("WeakerAccess")
    public PoiAdd() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandChangePoiState.COMMAND)
                .variable(CommandChangePoiState.VAR_ADD)
                .writeStringParam() // POI id
                .writeByte(TraciDatatypes.COMPOUND)
                .writeInt(4)
                .writeStringParamWithType() // POI name
                .writeByte(TraciDatatypes.UBYTE_COLOR)
                .writeByteParam() // red
                .writeByteParam() // green
                .writeByteParam() // blue
                .writeByteParam() // alpha
                .writeIntParamWithType() // layer
                .writeByte(TraciDatatypes.POSITION2D)
                .writeDoubleParam() // position x
                .writeDoubleParam(); // position y
    }

    /**
     * Executes the command in order to add a new PoI in the SUMO-GUI.
     *
     * @param traciCon Connection to Traci.
     * @param poiId    the unique ID of the PoI
     * @param layer    the layer index of the PoI
     * @param position the cartesian position of the center of the PoI
     * @param color    the color of the PoI
     * @throws TraciCommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(TraciConnection traciCon, String poiId, int layer, CartesianPoint position, Color color) throws TraciCommandException, InternalFederateException {
        super.execute(traciCon, poiId, poiId,
                color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(),
                layer, position.getX(), position.getY()
        );
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
