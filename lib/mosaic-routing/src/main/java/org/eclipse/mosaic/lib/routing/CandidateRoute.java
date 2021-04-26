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

    private final List<String> connectionIds;
    private final double length;
    private final double time;

    public CandidateRoute(List<String> connectionIds, double length, double time) {
        this.connectionIds = connectionIds;
        this.length = length;
        this.time = time;
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
}
