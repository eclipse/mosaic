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
import org.eclipse.mosaic.lib.objects.electricity.ChargingType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * Definition of an EV charging station.
 * An infrastructure which provides one or several EV charging spots to supply
 * electric energy for charging EVs.
 */
public class CChargingStation {

    /**
     * Definition of an EV charging spot.
     */
    public static class CChargingSpot {
        /**
         * The type of this EV charging spot.
         */
        public ChargingType chargingType;

        /**
         * The maximal voltage this charging spot can deliver.
         */
        public double maxVoltage;

        /**
         * The maximal current this charging spot can deliver.
         */
        public double maxCurrent;

        @Override
        public String toString() {
            // NO_CLASS_NAME_STYLE for better formatting in ChargingStationSpawner
            return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                    .append("chargingType", chargingType)
                    .append("maxVoltage", maxVoltage)
                    .append("maxCurrent", maxCurrent)
                    .build();
        }
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
