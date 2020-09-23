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

import org.eclipse.mosaic.fed.sumo.traci.TraciVersion;
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandChangeVehicleValue;

public class VehicleSetVehicleLength extends AbstractVehicleSetSingleDoubleValue {

    public VehicleSetVehicleLength() {
        super(TraciVersion.LOWEST, CommandChangeVehicleValue.VAR_LENGTH);
    }
}
