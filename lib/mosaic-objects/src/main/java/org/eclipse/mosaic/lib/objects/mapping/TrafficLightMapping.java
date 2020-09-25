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

package org.eclipse.mosaic.lib.objects.mapping;

import org.eclipse.mosaic.lib.geo.GeoPoint;

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A traffic light simulation unit that is equipped with applications.
 */
@Immutable
public final class TrafficLightMapping extends UnitMapping {

    private static final long serialVersionUID = 1L;
    
    private final GeoPoint position;

    /**
     * Creates a new traffic light simulation unit.
     *
     * @param name         The name of the traffic light.
     * @param group        The group name of the traffic light.
     * @param applications The list of applications with which the traffic light is equipped with.
     * @param position     The position of the traffic light.
     */
    public TrafficLightMapping(
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

