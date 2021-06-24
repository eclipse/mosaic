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

package org.eclipse.mosaic.lib.objects;

public enum UnitType {

    VEHICLE("veh"),
    ROAD_SIDE_UNIT("rsu"),
    TRAFFIC_MANAGEMENT_CENTER("tmc"),
    TRAFFIC_LIGHT("tl"),
    CHARGING_STATION("cs"),
    SERVER("server");

    public final String prefix;

    UnitType(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return prefix;
    }
}