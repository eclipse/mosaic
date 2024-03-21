/*
 * Copyright (c) 2023 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.routing.graphhopper.util;

import com.graphhopper.config.Profile;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.Subnetwork;
import com.graphhopper.routing.ev.TurnCost;
import com.graphhopper.routing.ev.TurnRestriction;
import com.graphhopper.routing.util.DefaultVehicleEncodedValuesFactory;
import com.graphhopper.routing.util.VehicleEncodedValues;
import com.graphhopper.util.PMap;

/**
 * Collection of all {@link com.graphhopper.routing.ev.EncodedValue} implementations
 * required for a vehicle to function within the GraphHopper context. This includes:
 * - "access" - If a vehicle can drive on an edge.<br>
 * - "speed" - The speed limit for the vehicle on the edge.<br>
 * - "priority" - Encode how to prioritize a road type to another.<br>
 * - "subnetwork" - Encode if an edge belongs to a subnetwork<br>
 * - "turnRestriction" / "turnCost" - encoding of costs for a turn from on edge to another.<br>
 * These encoders take care of storing and reading properties on edges.
 */
public class VehicleEncoding {

    private final BooleanEncodedValue accessEnc;
    private final DecimalEncodedValue speedEnc;
    private final BooleanEncodedValue turnRestrictionEnc;
    private final DecimalEncodedValue turnCostEnc;
    private final DecimalEncodedValue priorityEnc;
    private final BooleanEncodedValue subnetworkEnc;

    VehicleEncoding(Profile profile) {
        VehicleEncodedValues vehicle = new DefaultVehicleEncodedValuesFactory()
                .createVehicleEncodedValues(profile.getVehicle(), new PMap());
        this.accessEnc = vehicle.getAccessEnc();
        this.speedEnc = vehicle.getAverageSpeedEnc();
        this.priorityEnc = vehicle.getPriorityEnc();
        this.turnRestrictionEnc = TurnRestriction.create(profile.getVehicle());
        this.turnCostEnc = TurnCost.create(profile.getVehicle(), 255);
        this.subnetworkEnc = Subnetwork.create(profile.getVehicle());
    }

    public BooleanEncodedValue access() {
        return accessEnc;
    }

    public DecimalEncodedValue speed() {
        return speedEnc;
    }

    public DecimalEncodedValue priority() {
        return priorityEnc;
    }

    public BooleanEncodedValue turnRestriction() {
        return turnRestrictionEnc;
    }

    public DecimalEncodedValue turnCost() {
        return turnCostEnc;
    }

    public BooleanEncodedValue subnetwork() {
        return subnetworkEnc;
    }
}
