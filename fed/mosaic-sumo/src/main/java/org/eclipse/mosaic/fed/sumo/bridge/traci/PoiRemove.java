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
import org.eclipse.mosaic.fed.sumo.bridge.TraciVersion;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.Status;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandChangePoiState;
import org.eclipse.mosaic.rti.api.InternalFederateException;

/**
 * This removes adds a PoI object in the SUMO-GUI.
 */
public class PoiRemove
        extends AbstractTraciCommand<Void>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.PoiRemove {

    /**
     * Creates a new {@link PoiRemove} traci command.
     *
     * @see <a href="https://sumo.dlr.de/docs/TraCI/Change_PoI_State.html">Lane State Change</a>
     */
    @SuppressWarnings("WeakerAccess")
    public PoiRemove() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandChangePoiState.COMMAND)
                .variable(CommandChangePoiState.VAR_REMOVE)
                .writeStringParam() // POI id
                .writeIntParamWithType(); // layer
    }


    /**
     * Executes the command in order to remove a previously added PoI in the SUMO-GUI.
     *
     * @param bridge Connection to SUMO.
     * @param poiId  the unique ID of the PoI to remove
     * @param layer  the layer index of the PoI to remove
     * @throws CommandException          if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(Bridge bridge, String poiId, int layer) throws CommandException, InternalFederateException {
        super.execute(bridge, poiId, layer);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
