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

import java.io.Serializable;
import java.util.Collection;

/**
 * Provides information about a directional road segment.
 */
public interface IConnection extends Serializable {

    /**
     * Returns the ID of the road segment in the form {@code <id-way>_<id-start-junction>_<id-end-junction>}.
     */
    String getId();

    /**
     * Returns the length (in m) of the road segment.
     */
    double getLength();

    /**
     * Returns the start junction of the road segment.
     */
    INode getStartNode();

    /**
     * Returns the end junction of the road segment.
     */
    INode getEndNode();

    /**
     * Returns way properties of this road segment.
     */
    IWay getWay();

    /**
     * Returns the number of lanes on this road segment.
     */
    int getLanes();

    /**
     * Returns all incoming connections.
     */
    Collection<IConnection> getIncomingConnections();

    /**
     * Returns all outgoing connections.
     */
    Collection<IConnection> getOutgoingConnections();

}
