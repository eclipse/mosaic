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

package org.eclipse.mosaic.fed.mapping.config.units;

import org.eclipse.mosaic.lib.geo.GeoPoint;

import java.util.List;

/**
 * Definition of an EV charging station based on ETSI TS 101 556-1. An
 * infrastructure which provides one or several EV charging spots to supply
 * electric energy for charging EVs.
 */
public class CChargingStation {

    /**
     * Definition of an EV charging spot based on ETSI TS 101 556-1. A set of 1 to 4
     * parking places arranged around a pole, where it is possible to charge an EV.
     */
    public static class CChargingSpot {

        /**
         * Unique identifier of the charging spot.
         */
        public Integer id;

        /**
         * The type of this EV charging spot in compliance with current standards,
         * including IEC 62196-2 (mandatory).
         */
        public Integer type;

        /**
         * Number of available parking places, i.e. 1 to 4 parking places arranged
         * around a pole (mandatory).
         */
        public Integer parkingPlaces;
    }

    /**
     * The geographic position at which the EV charging station will
     * be created (mandatory).
     */
    public GeoPoint position;

    /**
     * The group name.
     */
    public String group;

    /**
     * The EV charging station operator (e.g. energy provider) identification
     * (mandatory).
     */
    public String operator;

    /**
     * Access restrictions, e.g. open to all or restricted to some communities,
     * free of access or paying access (mandatory).
     */
    public String access;

    /**
     * List of the EV charging spots associated with this EV charging station
     * (mandatory).
     */
    public List<CChargingSpot> chargingSpots;

    /**
     * The name of the prototype to be matched against this object (will replace
     * missing properties) (optional).
     */
    public String name;

    /**
     * Specifies the application(-s) to be used for this object (optional).
     */
    public List<String> applications;
}
