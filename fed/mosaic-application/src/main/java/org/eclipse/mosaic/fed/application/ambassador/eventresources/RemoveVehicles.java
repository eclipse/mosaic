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

package org.eclipse.mosaic.fed.application.ambassador.eventresources;

import org.eclipse.mosaic.lib.util.scheduling.Event;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This class is to be used as an {@link Event} resource,
 * it contains a list of vehicle names to be removed.
 */
public class RemoveVehicles implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * List of vehicle IDs (strings) identifying removed vehicles.
     */
    private final List<String> removedNames;

    /**
     * The constructor for {@link RemoveVehicles}.
     *
     * @param removedNames a list of vehicle names to remove
     */
    public RemoveVehicles(List<String> removedNames) {
        this.removedNames = Collections.unmodifiableList(removedNames);
    }

    public List<String> getRemovedNames() {
        return removedNames;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.removedNames);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoveVehicles other = (RemoveVehicles) obj;
        return Objects.equals(this.removedNames, other.removedNames);
    }

    @Override
    public String toString() {
        return "RemoveVehicles{" + "removedNames=" + removedNames + '}';
    }
}
