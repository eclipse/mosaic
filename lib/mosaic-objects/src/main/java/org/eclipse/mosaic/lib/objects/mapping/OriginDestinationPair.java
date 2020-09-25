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

package org.eclipse.mosaic.lib.objects.mapping;

import org.eclipse.mosaic.lib.geo.GeoCircle;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import javax.annotation.concurrent.Immutable;

/**
 * Class containing the information about an OD-pair (Origin, Destination). Is
 * used for spawning vehicles by mapping.
 */
@Immutable
public class OriginDestinationPair implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Random point within the given origin circle from which the vehicle will be spawned.
     */
    public final GeoCircle origin;

    /**
     * Random point within the given destination circle to which the vehicle will travel.
     */
    public final GeoCircle destination;

    public OriginDestinationPair(GeoCircle origin, GeoCircle destination) {
        this.origin = origin;
        this.destination = destination;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 71)
                .append(origin)
                .append(destination)
                .toHashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false; 
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;
        
        OriginDestinationPair other = (OriginDestinationPair) obj;
        return new EqualsBuilder()
                .append(this.origin, other.origin)
                .append(this.destination, other.destination)
                .isEquals();
    }
    
    @Override
    public String toString() {
        return "ODPairInformation{"
                + "origin=" + origin
                + ", destination=" + destination + '}';
    }
    
}
