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

import org.eclipse.mosaic.lib.geo.GeoPoint;

/**
 * Provides properties for edges, such as the speed limit, length, or road type.
 */
public interface EdgeProperties {

    /**
     * Returns the length of the edge in [m].
     *
     * @return the length of the edge in [m]
     */
    double getLength();

    /**
     * Returns the speed limit on the edge in [m/s].
     *
     * @return the speed limit on the edge in [m/s]
     */
    double getSpeed();

    /**
     * Returns the complete geometry of the edge as a list of geo points with elevation data.
     * Be aware that this is an expensive operation which should not be called too often.
     *
     * @return the complete geometry of the edge.
     */
    Iterable<GeoPoint> getGeometry();

    /**
     * Returns the id of the connection the edge is presenting.
     * Be aware that this might be an expensive operation which should not be called too often.
     *
     * @return the id of the connection the edge is presenting
     */
    String getConnectionId();


    /**
     * Returns the the type of the belonging way, such as 'primary', 'secondary', or 'residential' depending
     * on the source for the routing graph.
     * Be aware that this might be an expensive operation which should not be called too often.
     *
     * @return the type of the belonging way
     */
    String getWayType();


}
