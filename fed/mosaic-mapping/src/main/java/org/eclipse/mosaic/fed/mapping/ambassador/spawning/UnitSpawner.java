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

package org.eclipse.mosaic.fed.mapping.ambassador.spawning;

import org.eclipse.mosaic.fed.mapping.config.CPrototype;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class is to be extended by all classes, that are supposed to
 * spawn units into a simulation (e.g {@link ChargingStationSpawner}, {@link RoadSideUnitSpawner}, etc.).
 * It offers basic functionality for the handling of applications on simulation units.
 */
abstract class UnitSpawner {
    /**
     * All applications defined for a unit.
     */
    List<String> applications = new ArrayList<>();
    /**
     * The prototype of the unit.
     */
    String prototypeName;
    /**
     * The group of the unit.
     */
    String group;

    /**
     * Constructor for {@link UnitSpawner}.
     *
     * @param applications  String representation of all applications added to the unit
     * @param prototypeName the name of the prototype of the unit
     * @param group         the name of the group the unit belongs to
     */
    UnitSpawner(List<String> applications, String prototypeName, String group) {
        if (applications != null) {
            // add all applications, remove the ones that are null
            this.applications.addAll(applications.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
        this.prototypeName = prototypeName;
        this.group = group;
    }

    public String getPrototypeName() {
        return prototypeName;
    }

    public String getGroup() {
        return group;
    }

    /**
     * This method provides a view of the applications.
     *
     * @return A view of {@link #applications}
     */
    public List<String> getApplications() {
        return Collections.unmodifiableList(applications);
    }

    /**
     * This method fills in {@link #applications} and
     * {@link #group}, if they weren't set in the units'
     * configuration using definitions from the prototype.
     *
     * @param prototypeConfiguration the prototype configuration to be used to fill in applications
     */
    public void fillInPrototype(CPrototype prototypeConfiguration) {
        // no valid prototype configuration
        if (prototypeConfiguration == null) {
            return;
        }

        if (applications.isEmpty() && prototypeConfiguration.applications != null) {
            // inherit applications from prototype, dismiss all applications set as null
            applications.addAll(prototypeConfiguration.applications.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }

        // No group name given, try to use the CPrototype group name
        group = ObjectUtils.defaultIfNull(group, prototypeConfiguration.group);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(ToStringStyle.SHORT_PREFIX_STYLE)
                .append("protoTypeName", prototypeName)
                .append("group", group)
                .append("applications", applications)
                .build();
    }
}
