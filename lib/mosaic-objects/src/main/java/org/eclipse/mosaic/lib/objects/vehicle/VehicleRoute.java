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

package org.eclipse.mosaic.lib.objects.vehicle;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * This is a complete route that can be driven by a vehicle.
 */
public class VehicleRoute implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier for this route.
     */
    private final String routeId;

    /**
     * The list of all connection IDs this route passes.
     */
    private final List<String> connectionIds;

    /**
     * The list of all node IDs this route passes.
     */
    private final List<String> nodeIds;

    /**
     * The length of the complete route described from edges.
     * {@code 0} if no length is given.
     */
    private final double length;

    /**
     * This creates a route a vehicle can drive on.
     *
     * @param id     may NOT be null
     * @param connections  may be but should not be null
     * @param nodes  may be but should not be null
     * @param length the length of the complete route described from edges. <code>0.0</code> if no length is given.
     */
    public VehicleRoute(@Nonnull String id, List<String> connections, List<String> nodes, double length) {
        this.routeId = Objects.requireNonNull(id);
        this.connectionIds = (connections == null) ? new ArrayList<>() : connections;
        this.nodeIds = (nodes == null) ? new ArrayList<>() : nodes;
        this.length = length;
    }

    /**
     * The unique identifier for this route.
     *
     * @return the routeID
     */
    public String getId() {
        return this.routeId;
    }

    /**
     * The list of all edge IDs this route passes.
     *
     * @return the edgeIdList
     */
    @Nonnull
    public List<String> getConnectionIds() {
        return this.connectionIds;
    }

    /**
     * The list of all node IDs this route passes.
     *
     * @return nodeIdList
     */
    @Nonnull
    public List<String> getNodeIds() {
        return this.nodeIds;
    }

    /**
     * The length of the complete route described from edges.
     *
     * @return the length. <code>0</code> if no length is given.
     */
    public double getLength() {
        return length;
    }

    /**
     * Returns the last edge id. Returns <code>null</code> if no edge is given.
     *
     * @return the last edge id.
     */
    public String getLastConnectionId() {
        return Iterables.getLast(connectionIds, null);
    }

    public String getLastNodeId() {
        return Iterables.getLast(nodeIds, null);
    }

    @Override
    public String toString() {
        return "Route{" + "id=" + routeId + ", connetions=" + connectionIds + ", nodes=" + nodeIds + ", length=" + length + '}';
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 23)
                .append(routeId)
                .append(connectionIds)
                .append(nodeIds)
                .append(length)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        VehicleRoute other = (VehicleRoute) obj;
        return new EqualsBuilder()
                .append(this.routeId, other.routeId)
                .append(this.connectionIds, other.connectionIds)
                .append(this.nodeIds, other.nodeIds)
                .append(this.length, other.length)
                .isEquals();
    }


}
