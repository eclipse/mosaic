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

package org.eclipse.mosaic.fed.sumo.traci;

public enum TraciVersion {
    UNKNOWN(0),

    API_18(18),
    API_19(19),
    API_20(20),

    /**
     * the lowest version supported by this client.
     */
    LOWEST(API_18.getApiVersion()),

    /**
     * the highest version supported by this client.
     */
    HIGHEST(API_20.getApiVersion());

    private final int apiVersion;

    TraciVersion(int apiVersion) {
        this.apiVersion = apiVersion;
    }

    public int getApiVersion() {
        return apiVersion;
    }

    public static TraciVersion getTraciVersion(int apiVersion) {
        for (TraciVersion version : TraciVersion.values()) {
            if (version.getApiVersion() == apiVersion) {
                return version;
            }
        }
        if (apiVersion > HIGHEST.getApiVersion()) {
            return HIGHEST;
        }
        return UNKNOWN;
    }
}
