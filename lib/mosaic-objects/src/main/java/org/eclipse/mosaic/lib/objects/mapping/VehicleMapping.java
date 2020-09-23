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

package org.eclipse.mosaic.lib.objects.mapping;

import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A vehicle simulation unit that is equipped with applications.
 */
@Immutable
public final class VehicleMapping extends UnitMapping {

    private static final long serialVersionUID = 1L;

    private final VehicleType vehicleType;

    /**
     * Creates a new ApplicationVehicle.
     *
     * @param name         The name of the vehicle.
     * @param group        The group name of the vehicle.
     * @param applications The list of applications the vehicle is equipped with.
     * @param vehicleType  The vehicle type.
     */
    public VehicleMapping(
            final String name,
            final String group,
            final List<String> applications,
            final VehicleType vehicleType
    ) {
        super(name, group, applications);
        this.vehicleType = vehicleType;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }
}
