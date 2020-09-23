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

package org.eclipse.mosaic.lib.database.road;

import org.eclipse.mosaic.lib.geo.GeoPoint;

/**
 * This represents {@link TrafficLightNode}.
 */
public class TrafficLightNode extends Node {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@link TrafficLightNode} object.
     *
     * @param id       Id of the traffic light.
     * @param position GeoPosition of the traffic light.
     */
    public TrafficLightNode(String id, GeoPoint position) {
        super(id, position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
