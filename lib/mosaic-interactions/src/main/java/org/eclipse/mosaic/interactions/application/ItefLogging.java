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

package org.eclipse.mosaic.interactions.application;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.rti.api.Interaction;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This extension of {@link Interaction} is used to exchange log-tuples for the ITEF (Integrated Testing and Evaluation Framework).
 */
@SuppressWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public final class ItefLogging extends Interaction {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_ID = createTypeIdentifier(ItefLogging.class);

    /**
     * string identifying a simulated unit.
     */
    private final String unitId;
    private final long logTupleId;
    private final int[] values;

    /**
     * Creates a new ItefLogging.
     *
     * @param time       Timestamp of this interaction, unit: [ns]
     * @param unitId     unit identifier
     * @param logTupleId log tuple identifier
     * @param values     list of values
     */
    public ItefLogging(long time, String unitId, long logTupleId, int... values) {
        super(time);
        this.unitId = unitId;
        this.logTupleId = logTupleId;
        this.values = values;
    }

    public String getUnitId() {
        return unitId;
    }

    public long getLogTupleId() {
        return logTupleId;
    }

    public int[] getValues() {
        return values.clone();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 89)
                .append(unitId)
                .append(values)
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

        ItefLogging other = (ItefLogging) obj;
        return new EqualsBuilder()
                .append(this.unitId, other.unitId)
                .append(this.values, other.values)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("unitId", unitId)
                .append("concatenatedTupleParameters", StringUtils.join(values, ','))
                .toString();
    }
}
