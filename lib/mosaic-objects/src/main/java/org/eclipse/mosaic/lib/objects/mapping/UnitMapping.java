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

package org.eclipse.mosaic.lib.objects.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A simulation unit which may be equipped with applications.
 */
@Immutable
public abstract class UnitMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The name of the unit.
     */
    private final String name;

    /**
     * List of applications the unit should run.
     */
    private final List<String> applications;

    private final String group;

    /**
     * Creates a new ApplicationUnit.
     *
     * @param name         The name of the unit.
     * @param group        The name of the units' group.
     * @param applications The list of applications the unit is equipped with.
     */
    UnitMapping(final String name, final String group, final List<String> applications) {
        this.name = name;
        this.group = group;
        if (applications == null) {
            this.applications = Collections.unmodifiableList(new ArrayList<>());
        } else {
            this.applications = Collections.unmodifiableList(applications);
        }
    }

    /**
     * Get the name of the unit.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the group name.
     *
     * @return the group name
     */
    public String getGroup() {
        return group;
    }

    public List<String> getApplications() {
        return applications;
    }

    public boolean hasApplication() {
        return applications.size() > 0;
    }

    @Override
    public String toString() {
        return "ApplicationUnit{" + "name=" + name + ", applications=" + applications + '}';
    }

}
