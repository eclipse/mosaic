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

package org.eclipse.mosaic.lib.objects.road;

import org.eclipse.mosaic.lib.geo.GeoPoint;

import java.io.Serializable;

/**
 * Provides information about a node, such as its position.
 */
public interface INode extends Serializable {

    /**
     * Returns the internal id of the node.
     */
    String getId();

    /**
     * Returns the geo position of the node.
     */
    GeoPoint getPosition();

    /**
     * Returns <code>true</code>, if this node is controlled by traffic lights..
     */
    boolean hasTrafficLight();

    /**
     * Returns true if the node is an intersection.
     */
    boolean isIntersection();

}
