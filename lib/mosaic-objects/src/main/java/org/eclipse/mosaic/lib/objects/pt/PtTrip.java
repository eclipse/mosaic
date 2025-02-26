/*
 * Copyright (c) 2025 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.objects.pt;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link PtTrip} represents a single trip using a public transport vehicle. It provides
 * information about the trip, such as the name of the public transport line and the list
 * of all {@link PtStop}s along the trip including arrival and departure times.
 */
public class PtTrip implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String line;
    private final String finalStopName;
    private final List<PtStop> stops = new ArrayList<>();

    /**
     * Creates a new object holding information about the public transport trip.
     *
     * @param line          The name of the line of the public transport route.
     * @param finalStopName The name of the final destination of the public transport route (not the trip), e.g., the head sign of the pt vehicle.
     * @param stops         The list of public transport stops passed by this trip.
     */
    public PtTrip(String line, String finalStopName, List<PtStop> stops) {
        this.line = line;
        this.finalStopName = finalStopName;
        this.stops.addAll(stops);
    }

    /**
     * Returns the list of stops along the public transport route.
     */
    public List<PtStop> getStops() {
        return stops;
    }

    /**
     * Returns the name of the line of the public transport route.
     */
    public String getLine() {
        return line;
    }

    /**
     * Returns the name of the final destination of the public transport route (not the trip).
     */
    public String getFinalStopName() {
        return finalStopName;
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

        PtTrip other = (PtTrip) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.line, other.line)
                .append(this.finalStopName, other.finalStopName)
                .append(this.stops, other.stops)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 89)
                .appendSuper(super.hashCode())
                .append(line)
                .append(finalStopName)
                .append(stops)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("line", line)
                .append("finalStopName", finalStopName)
                .append("stops", stops)
                .toString();
    }
}
