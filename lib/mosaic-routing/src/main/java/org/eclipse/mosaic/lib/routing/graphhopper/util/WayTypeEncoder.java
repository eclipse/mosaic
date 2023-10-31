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

package org.eclipse.mosaic.lib.routing.graphhopper.util;

import org.eclipse.mosaic.lib.database.road.Way;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import com.graphhopper.routing.ev.EdgeIntAccess;
import com.graphhopper.routing.ev.IntEncodedValueImpl;

import java.util.Set;

public class WayTypeEncoder extends IntEncodedValueImpl {

    public final static String KEY = "waytype";

    private static final BiMap<String, Integer> wayTypeIntMap = HashBiMap.create();
    private static final Set<String> highwayTypes = Sets.newHashSet(
            "motorway", "motorway_link"
    );
    private static final Set<String> residentialTypes = Sets.newHashSet(
            "unclassified", "residential", "living_street"
    );
    private static final Set<String> oneLaneIgnoreTypes = Sets.newHashSet(
            "motorway", "motorway_link", "primary", "primary_link", "secondary_link"
    );
    private static final Set<String> mainroadTypes = Sets.newHashSet(
            "primary", "primary_link", "secondary", "secondary_link"
    );
    private static final Set<String> cyclewayTypes = Sets.newHashSet(
            "cycleway"
    );

    private static final int HIGHWAY = 1 << 15;
    private static final int RESIDENTIAL = 1 << 14;
    private static final int TUNNEL = 1 << 13;
    private static final int TOLL = 1 << 12;
    private static final int BAD_ROAD = 1 << 11;
    private static final int ONE_LANE = 1 << 10;
    private static final int MAIN_ROAD = 1 << 9;
    private static final int CYCLEWAY = 1 << 8;
    private static final int TYPE_MASK = 0x03FFFFFF;

    static {
        // autobahn
        wayTypeIntMap.put("motorway", 100);
        wayTypeIntMap.put("motorway_link", 99);
        // bundesstrasse
        wayTypeIntMap.put("trunk", 80);
        wayTypeIntMap.put("trunk_link", 79);
        // linking bigger town
        wayTypeIntMap.put("primary", 60);
        wayTypeIntMap.put("primary_link", 59);
        // linking towns + villages
        wayTypeIntMap.put("secondary", 50);
        wayTypeIntMap.put("secondary_link", 49);
        // streets without middle line separation
        wayTypeIntMap.put("tertiary", 40);
        wayTypeIntMap.put("tertiary_link", 39);
        // any other roads
        wayTypeIntMap.put("unclassified", 30);
        wayTypeIntMap.put("residential", 29);
        wayTypeIntMap.put("living_street", 28);
        wayTypeIntMap.put("service", 27);
        wayTypeIntMap.put("road", 26);
        wayTypeIntMap.put("track", 25);
    }

    private WayTypeEncoder() {
        super(KEY,  31, false);
    }

    public static WayTypeEncoder create() {
        return new WayTypeEncoder();
    }

    public static String decode(int type) {
        String result = wayTypeIntMap.inverse().get(type & TYPE_MASK);
        if (result != null) {
            return result;
        }
        return "unknown";
    }

    public static int encode(String wayType, int numberLanes) {
        int flags = 0;
        if (highwayTypes.contains(wayType)) {
            flags |= HIGHWAY;
        }
        if (residentialTypes.contains(wayType)) {
            flags |= RESIDENTIAL;
        }
        if (numberLanes == 1 && !oneLaneIgnoreTypes.contains(wayType)) {
            flags |= ONE_LANE;
        }
        if (mainroadTypes.contains(wayType)) {
            flags |= MAIN_ROAD;
        }
        if (cyclewayTypes.contains(wayType)) {
            flags |= CYCLEWAY;
        }

        Integer result = wayTypeIntMap.get(wayType);
        if (result != null) {
            return result | flags;
        }
        return 0;
    }

    public static boolean isHighway(int wayTypeEncoded) {
        return (wayTypeEncoded & HIGHWAY) != 0;
    }

    public static boolean isResidential(int wayTypeEncoded) {
        return (wayTypeEncoded & RESIDENTIAL) != 0;
    }

    public static boolean isTunnel(int wayTypeEncoded) {
        return (wayTypeEncoded & TUNNEL) != 0;
    }

    public static boolean isToll(int wayTypeEncoded) {
        return (wayTypeEncoded & TOLL) != 0;
    }

    public static boolean isBadRoad(int wayTypeEncoded) {
        return (wayTypeEncoded & BAD_ROAD) != 0;
    }

    public static boolean isOneLane(int wayTypeEncoded) {
        return (wayTypeEncoded & ONE_LANE) != 0;
    }

    public static boolean isMainRoad(int wayTypeEncoded) {
        return (wayTypeEncoded & MAIN_ROAD) != 0;
    }

    public static boolean isCycleway(int wayTypeEncoded) {
        return (wayTypeEncoded & CYCLEWAY) != 0;
    }

    public static boolean isLowerType(int a, int b) {
        int typeA = a & TYPE_MASK;
        int typeB = b & TYPE_MASK;
        return typeB + 4 < typeA;
    }

    public void setWay(Way way, int lanes, int edge, EdgeIntAccess access) {
        setInt(false, edge, access, encode(way.getType(), lanes));
    }
}
