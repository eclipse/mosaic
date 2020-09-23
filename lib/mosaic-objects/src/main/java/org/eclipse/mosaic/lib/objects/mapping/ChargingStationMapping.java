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

import org.eclipse.mosaic.lib.geo.GeoPoint;

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A Charging Station that is equipped with applications.
 */
@Immutable
public final class ChargingStationMapping extends UnitMapping {

    private static final long serialVersionUID = 1L;

    private final GeoPoint position;

    /**
     * Creates a new Charging Station that is equipped with applications.
     *
     * @param name         name of the charging station
     * @param group        group name of the charging station
     * @param applications list of application the charging station is equipped with
     * @param position     static position of the charging station
     */
    public ChargingStationMapping(
            final String name,
            final String group,
            final List<String> applications,
            final GeoPoint position
    ) {
        super(name, group, applications);
        this.position = position;
    }

    public GeoPoint getPosition() {
        return position;
    }
}

