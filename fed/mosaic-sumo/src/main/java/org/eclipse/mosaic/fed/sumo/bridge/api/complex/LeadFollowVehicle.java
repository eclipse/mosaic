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
 * Holds information about the leading or following vehicle.
 */
@Immutable
public class LeadFollowVehicle {

    public static final LeadFollowVehicle NONE = new LeadFollowVehicle("", Double.POSITIVE_INFINITY);

    private final String otherVehicleId;
    private final double distance;

    /**
     * Creates a new {@link LeadFollowVehicle} object.
     *
     * @param leadingVehicleId the id of the leading or following vehicle
     * @param distance         the distance towards the leading or following vehicle
     */
    public LeadFollowVehicle(final @Nonnull String leadingVehicleId, double distance) {
        this.otherVehicleId = leadingVehicleId;
        this.distance = distance;
    }

    /**
     * Getter for the Id of the leading or following vehicle.
     *
     * @return the id of the leading or following vehicle.
     */
    public final @Nonnull
    String getOtherVehicleId() {
        return otherVehicleId;
    }

    /**
     * Getter for the distance towards the leading or following vehicle.
     *
     * @return The distance towards the leading or following vehicle.
     */
    public final double getDistance() {
        return distance;
    }
}
