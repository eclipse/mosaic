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
 */

package org.eclipse.mosaic.lib.routing.database;

import org.eclipse.mosaic.lib.database.Database;
import org.eclipse.mosaic.lib.database.road.Node;
import org.eclipse.mosaic.lib.database.road.TrafficLightNode;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.road.INode;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nullable;

/**
 * <p>
 * Provides information about a node of a {@link IRoadPosition}. Any missing information is gathered lazy by requesting the
 * scenario-database as soon as the respective getter method is called. Those information is cached for later calls of the same method by
 * storing the node from the scenario-database..</p>
 * <p>
 * The id of the node <b>must</b> be known in order to retrieve the geographic position of the node.</p>
 */
public class LazyLoadingNode implements INode {

    private static final long serialVersionUID = 1L;

    /* This reference must be kept transient, since it should never be serialized (e.g. by GSON) */
    @Nullable
    private final transient Database database;

    /* This reference must be kept transient, since it should never be serialized (e.g. by GSON) */
    @SuppressWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    @Nullable
    private transient Node scenarioDatabaseNode;

    private final INode currentNode;

    public LazyLoadingNode(Node scenarioDatabaseNode) {
        this(null, null);
        this.scenarioDatabaseNode = scenarioDatabaseNode;
    }

    public LazyLoadingNode(INode currentNode, Database database) {
        this.currentNode = currentNode;
        this.database = database;
    }

    @Override
    public String getId() {
        return scenarioDatabaseNode != null ? scenarioDatabaseNode.getId() : currentNode.getId();
    }

    @Override
    public GeoPoint getPosition() {
        if (scenarioDatabaseNode == null && currentNode.getPosition() == null && database != null) {
            scenarioDatabaseNode = database.getNode(getId());
        }
        return scenarioDatabaseNode != null ? scenarioDatabaseNode.getPosition() : null;
    }

    @Override
    public boolean hasTrafficLight() {
        if (scenarioDatabaseNode == null && database != null) {
            scenarioDatabaseNode = database.getNode(getId());
        }
        return scenarioDatabaseNode != null && scenarioDatabaseNode instanceof TrafficLightNode;
    }

    @Override
    public boolean isIntersection() {
        return scenarioDatabaseNode != null && scenarioDatabaseNode.isIntersection();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 41)
                .append(this.currentNode)
                .append(this.scenarioDatabaseNode)
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

        LazyLoadingNode sdn = (LazyLoadingNode) obj;
        return new EqualsBuilder()
                .append(this.currentNode, sdn.currentNode)
                .append(this.scenarioDatabaseNode, sdn.scenarioDatabaseNode)
                .isEquals();
    }

}