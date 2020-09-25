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

package org.eclipse.mosaic.interactions.mapping;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.electricity.ChargingSpot;
import org.eclipse.mosaic.lib.objects.mapping.ChargingStationMapping;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collections;
import java.util.List;

/**
 * This extension of {@link Interaction} is intended to be used to forward an added charging
 * station to the RTI.
 */
public final class ChargingStationRegistration extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public static final String TYPE_ID = createTypeIdentifier(ChargingStationRegistration.class);

    /**
     * Charging station operator (e.g. energy provider) identification
     */
    private final String operator;

    /**
     * Access restrictions, e.g. open to all or restricted to some communities,
     * free of access or paying access.
     */
    private final String access;

    /**
     * List of the {@link ChargingSpot} units belonging to this charging station.
     */
    private final List<ChargingSpot> chargingSpots;

    /**
     * Reference to the {@link ChargingStationMapping}.
     */
    private final ChargingStationMapping chargingStationMapping;

    /**
     * Creates a new {@link ChargingStationRegistration} interaction.
     *
     * @param time          Timestamp of this interaction, unit: [ns]
     * @param name          ITS identification of the charging station
     * @param group         The group identifier.
     * @param applications  List of applications the charging station is equipped with.
     * @param operator      Charging station operator (e.g. energy provider) identification
     * @param position      The position of the charging station.
     * @param access        Access restrictions, e.g. open to all or restricted to some communities, free of
     *                      access or paying access
     * @param chargingSpots List of the {@link ChargingSpot} units belonging to this charging station
     */
    public ChargingStationRegistration(long time, String name, final String group, List<String> applications,
                                       GeoPoint position, String operator, String access,
                                       List<ChargingSpot> chargingSpots) {
        super(time);
        this.operator = operator;
        this.access = access;
        this.chargingSpots = Collections.unmodifiableList(chargingSpots);
        this.chargingStationMapping = new ChargingStationMapping(name, group, applications, position);
    }

    public String getOperator() {
        return operator;
    }

    public String getAccess() {
        return access;
    }

    /**
     * Returns the list of the {@link ChargingSpot} units belonging to this charging station.
     *
     * @return List of the {@link ChargingSpot} units.
     */
    public List<ChargingSpot> getChargingSpots() {
        return Collections.unmodifiableList(chargingSpots);
    }

    public ChargingStationMapping getMapping() {
        return chargingStationMapping;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 83)
                .append(operator)
                .append(access)
                .append(chargingSpots)
                .append(chargingStationMapping)
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

        ChargingStationRegistration rhs = (ChargingStationRegistration) obj;
        return new EqualsBuilder()
                .append(this.operator, rhs.operator)
                .append(this.access, rhs.access)
                .append(this.chargingSpots, rhs.chargingSpots)
                .append(this.chargingStationMapping, rhs.chargingStationMapping)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("operator", operator)
                .append("access", access)
                .append("chargingSpots", chargingSpots)
                .append("chargingStationMapping", chargingStationMapping)
                .toString();
    }
}