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

package org.eclipse.mosaic.lib.database.road;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * This represents a roundabout. Well defined, it contains all {@link Node}-references
 * and all Edge-references that are part of the roundabout.
 */
public class Roundabout {

    private final String id;

    private final List<Node> nodes = new ArrayList<>();

    /**
     * Default constructor.
     *
     * @param id    Unique identifier of the roundabout
     * @param nodes Nodes of the roundabout.
     */
    public Roundabout(@Nonnull String id, @Nonnull List<Node> nodes) {
        this.id = Objects.requireNonNull(id);
        this.nodes.addAll(nodes);
    }

    /**
     * Returns the roundabout's ID.
     *
     * @return Id of the roundabout.
     */
    @Nonnull
    public String getId() {
        return id;
    }

    /**
     * Returns all {@link Node}-references that are part of the roundabout.
     *
     * @return List of the Nodes.
     */
    @Nonnull
    public List<Node> getNodes() {
        return nodes;
    }
}
