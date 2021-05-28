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

package org.eclipse.mosaic.lib.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helper class to generate unique names for the objects in the simulation.
 */
public class UnitNameGenerator {

    private static final Map<UnitType, AtomicInteger> unitCounters = new HashMap<>();

    private static final AtomicInteger prototypeCounter = new AtomicInteger(0);

    static {
        for (UnitType unitType : UnitType.values()) {
            unitCounters.put(unitType, new AtomicInteger(0));
        }
    }

    public static String nextVehicleName() {
        return nextUnitName(UnitType.VEHICLE);
    }

    public static String nextRsuName() {
        return nextUnitName(UnitType.ROAD_SIDE_UNIT);
    }

    public static String nextTmcName() {
        return nextUnitName(UnitType.TRAFFIC_MANAGEMENT_CENTER);
    }

    public static String nextServerName() {
        return nextUnitName(UnitType.SERVER);
    }

    public static String nextTlName() {
        return nextUnitName(UnitType.TRAFFIC_LIGHT);
    }

    public static String nextChargingStationName() {
        return nextUnitName(UnitType.CHARGING_STATION);
    }

    public static String nextPrototypeName(String prototype) {
        if (prototype != null) {
            return prototype + "_" + prototypeCounter.getAndIncrement();
        } else {
            return "prot_" + prototypeCounter.getAndIncrement();
        }
    }

    private static String nextUnitName(UnitType unitType) {
        return unitType.prefix + "_" + unitCounters.get(unitType).getAndIncrement();
    }

    /**
     * Resets all counters for the name generators.
     */
    public static void reset() {
        unitCounters.values().forEach(c -> c.set(0));
        prototypeCounter.set(0);
    }

    /**
     * Explicitly sets the counter of the given {@link UnitType}. This number will
     * be used for generating the name of the next unit and will be increased further on.
     */
    public static void setNextUnitIndex(final UnitType unitType, final int counter) {
        unitCounters.get(unitType).set(counter);
    }

    /**
     * Returns the next unit index number which would be allocated with
     * a call of {@link #nextVehicleName()}, {@link #nextRsuName()}, and so on.
     */
    public static int getNextUnitIndex(final UnitType unitType) {
        return unitCounters.get(unitType).get();
    }
}
