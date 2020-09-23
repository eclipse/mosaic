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

import org.eclipse.mosaic.lib.objects.mapping.UnitMapping;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import java.io.Serializable;

/**
 * This class is to be used as an {@link Event} resource,
 * it contains the id of a {@link org.eclipse.mosaic.fed.application.ambassador.simulation.AbstractSimulationUnit}
 * and the respective {@link UnitMapping}.
 */
public class StartApplications implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String unitId;
    private final UnitMapping unitMapping;

    /**
     * The constructor for {@link StartApplications}.
     *
     * @param unitId          the id of the unit.
     * @param unitMapping the {@link UnitMapping}.
     */
    public StartApplications(final String unitId, final UnitMapping unitMapping) {
        this.unitId = unitId;
        this.unitMapping = unitMapping;
    }

    public String getUnitId() {
        return unitId;
    }

    public UnitMapping getUnitMapping() {
        return unitMapping;
    }
}
