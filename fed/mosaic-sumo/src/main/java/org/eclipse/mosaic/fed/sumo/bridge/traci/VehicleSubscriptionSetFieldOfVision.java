/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandVariableSubscriptions;
import org.eclipse.mosaic.rti.api.InternalFederateException;

/**
 * This class represents the SUMO command which allows to filter a previously created context subscription to
 * collect all vehicles surrounding a specific vehicle.
 */
public class VehicleSubscriptionSetFieldOfVision
        extends AbstractTraciCommand<Void>
        implements org.eclipse.mosaic.fed.sumo.bridge.api.VehicleSubscriptionSetFieldOfVision {

    @SuppressWarnings("WeakerAccess")
    public VehicleSubscriptionSetFieldOfVision() {
        super(SumoVersion.SUMO_1_4_x);

        write()
                .command(CommandVariableSubscriptions.COMMAND_ADD_CONTEXT_SUBSCRIPTION_FILTER)
                .writeByte(CommandVariableSubscriptions.SUBSCRIPTION_FILTER_FIELD_OF_VISION)
                .writeDoubleParamWithType();

        read().skipRemaining();
    }

    /**
     * This method executes the command with the given arguments in order to filter a previously created context subscription.
     *
     * @param bridge       Connection to SUMO.
     * @param openingAngle the opening angle of the field of vision of the vehicle for which a context subscription has been created.
     * @throws CommandException          if the status code of the response is ERROR. The connection to SUMO is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public void execute(Bridge bridge, double openingAngle) throws CommandException, InternalFederateException {
        super.execute(bridge, openingAngle);
    }

    @Override
    protected Void constructResult(Status status, Object... objects) {
        return null;
    }
}
