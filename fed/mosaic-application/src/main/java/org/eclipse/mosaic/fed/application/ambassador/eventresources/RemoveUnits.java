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

package org.eclipse.mosaic.fed.application.ambassador.eventresources;

import org.eclipse.mosaic.lib.util.scheduling.Event;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This class is to be used as an {@link Event} resource,
 * it contains a list of unit names to be removed.
 */
public class RemoveUnits implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * List of unit IDs (strings) identifying removed units, such as vehicles.
     */
    private final List<String> unitsToRemove;

    /**
     * The constructor for {@link RemoveUnits}.
     *
     * @param unitsToRemove a list of unit names to remove
     */
    public RemoveUnits(List<String> unitsToRemove) {
        this.unitsToRemove = Collections.unmodifiableList(unitsToRemove);
    }

    /**
     * Returns the list of units to be removed.
     */
    public List<String> getUnitsToRemove() {
        return unitsToRemove;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.unitsToRemove);
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
        final RemoveUnits other = (RemoveUnits) obj;
        return Objects.equals(this.unitsToRemove, other.unitsToRemove);
    }

    @Override
    public String toString() {
        return "RemoveUnits{" + "unitsToRemove=" + unitsToRemove + '}';
    }
}
