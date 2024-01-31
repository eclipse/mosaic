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

package org.eclipse.mosaic.lib.routing;

import java.util.List;

/**
 * Stores solely a list of connection IDs of the freshly generated route.
 * Additionally, includes the length and time for this route.
 */
public class CandidateRoute {

    /**
     * The list of connections forming this route.
     */
    private final List<String> connectionIds;

    /**
     * The length of this route.
     */
    private final double length;

    /**
     * The approximated driving time on this route.
     */
    private final double time;

    /**
     * The distance in meters from the start node of the first connection in the connectionIds list, to
     * the point the source query was issued.
     */
    private final double offsetFromSource;

    /**
     * The distance in meters from the point the target query was issued, until the end node of the
     * final connection in the connectionIds list.
     */
    private final double offsetToTarget;


    public CandidateRoute(List<String> connectionIds, double length, double time) {
        this(connectionIds, length, time, 0, 0);
    }

    public CandidateRoute(List<String> connectionIds, double length, double time, double offsetFromSource, double offsetToTarget) {
        this.connectionIds = connectionIds;
        this.length = length;
        this.time = time;
        this.offsetFromSource = offsetFromSource;
        this.offsetToTarget = offsetToTarget;
    }

    public List<String> getConnectionIds() {
        return connectionIds;
    }

    public double getLength() {
        return length;
    }

    public double getTime() {
        return time;
    }

    public double getOffsetFromSource() {
        return offsetFromSource;
    }

    public double getOffsetToTarget() {
        return offsetToTarget;
    }
}
