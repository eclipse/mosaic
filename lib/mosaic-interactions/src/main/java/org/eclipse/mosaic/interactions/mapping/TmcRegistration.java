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

package org.eclipse.mosaic.interactions.mapping;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.objects.mapping.TmcMapping;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * This extension of {@link Interaction} informs about a new Traffic Management Center (TMC) added to the simulation.
 */
public final class TmcRegistration extends Interaction {

    private static final long serialVersionUID = 1;

    public final static String TYPE_ID = createTypeIdentifier(TmcRegistration.class);

    private final TmcMapping tmcMapping;

    /**
     * Creates a new interaction that informs about a recently added TMC in the simulation.
     *
     * @param time              Timestamp of this interaction, unit: [ns]
     * @param name              TMC identifier
     * @param group             TMC group identifier
     * @param applications      List of applications the TMC is equipped with.
     * @param inductionLoops    List of induction loops the TMC is responsible for.
     * @param laneAreaDetectors List of lane area detectors the TMC is responsible for.
     */
    public TmcRegistration(final long time, final String name, final String group,
                           final List<String> applications, final List<String> inductionLoops, final List<String> laneAreaDetectors) {
        super(time);
        this.tmcMapping = new TmcMapping(name, group, applications, inductionLoops, laneAreaDetectors);
    }

    public TmcMapping getMapping() {
        return tmcMapping;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 59)
                .append(tmcMapping)
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

        TmcRegistration other = (TmcRegistration) obj;
        return new EqualsBuilder()
                .append(this.tmcMapping, other.tmcMapping)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("tmcMapping", tmcMapping)
                .toString();
    }
}
