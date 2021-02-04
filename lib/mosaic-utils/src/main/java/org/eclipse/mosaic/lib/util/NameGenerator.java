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

package org.eclipse.mosaic.lib.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helper class to generate unique names for the objects in the simulation.
 */
public class NameGenerator {
    private static final AtomicInteger vehicleCounter = new AtomicInteger(0);
    private static final AtomicInteger rsuCounter = new AtomicInteger(0);
    private static final AtomicInteger tmcCounter = new AtomicInteger(0);
    private static final AtomicInteger serverCounter = new AtomicInteger(0);
    private static final AtomicInteger tlCounter = new AtomicInteger(0);
    private static final AtomicInteger chargingStationCounter = new AtomicInteger(0);
    private static final AtomicInteger prototypeCounter = new AtomicInteger(0);

    public static String getVehicleName() {
        return "veh_" + vehicleCounter.getAndIncrement();
    }

    public static String getRsuName() {
        return "rsu_" + rsuCounter.getAndIncrement();
    }

    public static String getTmcName() {
        return "tmc_" + tmcCounter.getAndIncrement();
    }

    public static String getServerName() {
        return "server_" + serverCounter.getAndIncrement();
    }

    public static String getTlName() {
        return "tl_" + tlCounter.getAndIncrement();
    }

    public static String getChargingStationName() {
        return "cs_" + chargingStationCounter.getAndIncrement();
    }

    public static String getPrototypeName(String prototype) {
        if (prototype != null) {
            return prototype + "_" + prototypeCounter.getAndIncrement();
        } else {
            return "prot_" + prototypeCounter.getAndIncrement();
        }
    }

    /**
     * Resets all counters for the name generators.
     */
    public static void reset() {
        vehicleCounter.set(0);
        rsuCounter.set(0);
        tmcCounter.set(0);
        serverCounter.set(0);
        tlCounter.set(0);
        chargingStationCounter.set(0);
        prototypeCounter.set(0);
    }
}
