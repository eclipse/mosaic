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
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveJunctionValue;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.TraciDatatypes;
import org.eclipse.mosaic.fed.sumo.bridge.traci.reader.Position2dTraciReader;
import org.eclipse.mosaic.lib.util.objects.Position;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.Locale;

public class JunctionGetPosition
        extends AbstractTraciCommand<Position>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.JunctionGetPosition {

    /**
     * Creates a new {@link JunctionGetPosition} object.
     * Access needs to be public, because command is called using Reflection.
     */
    @SuppressWarnings("WeakerAccess")
    public JunctionGetPosition() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandRetrieveJunctionValue.COMMAND)
                .variable(CommandRetrieveJunctionValue.VAR_POSITION)
                .writeStringParam();

        read()
                .skipBytes(2)
                .skipString()
                .expectByte(TraciDatatypes.POSITION2D)
                .readComplex(new Position2dTraciReader());
    }

    /**
     * This method executes the command with the given arguments and returns the position of the junction.
     *
     * @param traciCon Connection to Traci.
     * @param junctionId  Id of the junction
     * @return Position of the junction.
     * @throws CommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public Position execute(Bridge traciCon, String junctionId) throws CommandException, InternalFederateException {
        return super.executeAndReturn(traciCon, junctionId).orElseThrow(
                () -> new CommandException(
                        String.format(Locale.ENGLISH, "Couldn't get Position of Junction %s.", junctionId),
                        new Status((byte) Status.STATUS_ERR, "")
                )
        );
    }

    @Override
    protected Position constructResult(Status status, Object... objects) {
        return (Position) objects[0];
    }
}
