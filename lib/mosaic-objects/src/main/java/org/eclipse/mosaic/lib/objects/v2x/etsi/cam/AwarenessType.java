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

package org.eclipse.mosaic.lib.objects.v2x.etsi.cam;

public enum AwarenessType {
    VEHICLE(0),
    RSU(1),
    TRAFFIC_LIGHT(2);

    public final int id;

    AwarenessType(int id) {
        this.id = id;
    }

    /**
     * Returns the enum mapped from an integer.
     *
     * @param id identifying integer
     * @return the enum mapped from an integer.
     */
    public static AwarenessType fromId(int id) {
        for (AwarenessType type : AwarenessType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AwarenessType id " + id);
    }
}