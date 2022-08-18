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

package org.eclipse.mosaic.lib.database.building;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

@Immutable
public class Building {

    /**
     * Unique String used to identify the building. This usually corresponds to an OSM way ID.
     */
    public final String id;

    /**
     * The name this specific building is called by.
     */
    public final String name;

    /**
     * The overall height of this building in meters.
     */
    public final double height;

    /**
     * A list of all {@link Wall}s this building is composed of.
     */
    private final List<Wall> walls = new ArrayList<>();

    public Building(@Nonnull String id, @Nonnull String name, double height, Collection<Wall> walls) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.height = height;
        this.walls.addAll(walls);
    }

    /**
     * @return unique {@link String} used to identify the building. This usually corresponds to an OSM way ID.
     */
    @Nonnull
    public String getId() {
        return this.id;
    }

    /**
     * @return the name this specific building is called by
     */
    @Nonnull
    public String getName() {
        return this.name;
    }

    /**
     * @return overall height of this building in meters.
     */
    public double getHeight() {
        return this.height;
    }

    /**
     * A list of all {@link Wall}s this building is composed of.
     *
     * @return unmodifiable {@link List<Wall>} containing all walls.
     */
    @Nonnull
    public List<Wall> getWalls() {
        return Collections.unmodifiableList(this.walls);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;

        Building other = (Building) obj;
        return new EqualsBuilder()
                .append(this.id, other.id)
                .append(this.name, other.name)
                .append(this.height, other.height)
                .append(this.walls, other.walls)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 31)
                .append(id)
                .append(name)
                .append(height)
                .append(walls)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Building{"
                + "id='" + id + '\''
                + ", name=" + name
                + ", height=" + height
                + ", walls=" + walls
                + '}';
    }
}

