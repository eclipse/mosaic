/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.database.persistence;

final class TABLES {
    final static String PROPERTIES = "Properties";
    final static String NODE = "Node";
    final static String WAY = "Way";
    final static String WAY_CONSISTS_OF = "WayConsistsOf";
    final static String CONNECTION = "Connection";
    final static String CONNECTION_CONSISTS_OF = "ConnectionConsistsOf";
    final static String RESTRICTION = "Restriction";
    final static String TRAFFIC_SIGNALS = "TrafficSignals";
    final static String ROUTE = "Route";
    final static String ROUNDABOUT = "Roundabout";
    final static String ROUNDABOUT_CONSISTS_OF = "RoundaboutConsistsOf";
    final static String BUILDING = "Building";
    final static String BUILDING_CONSISTS_OF = "BuildingConsistsOf";
    final static String CONNECTION_DETAILS = "ConnectionDetails";
    /**
     * Will no longer be written to, but used to update old versions of databases.
     */
    @Deprecated
    final static String CORNER = "Corner";
    /**
     * Will no longer be written to, but used to update old versions of databases.
     */
    @Deprecated
    final static String WALL = "Wall";
}
