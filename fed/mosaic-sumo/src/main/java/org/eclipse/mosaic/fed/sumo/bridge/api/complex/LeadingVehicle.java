/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.sumo.bridge.api.complex;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Holds information about the leading vehicle.
 */
@Immutable
public class LeadingVehicle {

    private final String leadingVehicleId;
    private final double leadingVehicleDistance;

    /**
     * Creates a new {@link LeadingVehicle} object.
     *
     * @param leadingVehicleId the id of the leading vehicle
     * @param distance         the distance towards the leading vehicle
     */
    public LeadingVehicle(final @Nonnull String leadingVehicleId, double distance) {
        this.leadingVehicleId = leadingVehicleId;
        this.leadingVehicleDistance = distance;
    }

    /**
     * Getter for the Id of the leading vehicle.
     *
     * @return the id of the leading vehicle.
     */
    public final @Nonnull
    String getLeadingVehicleId() {
        return leadingVehicleId;
    }

    /**
     * Getter for the distance towards the leading vehicle.
     *
     * @return The distance towards the leading vehicle.
     */
    public final double getLeadingVehicleDistance() {
        return leadingVehicleDistance;
    }
}
