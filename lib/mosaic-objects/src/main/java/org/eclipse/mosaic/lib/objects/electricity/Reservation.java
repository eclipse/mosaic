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

package org.eclipse.mosaic.lib.objects.electricity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * The Reservation-Class stores the vehicles by Id and the reservation time.
 */
public class Reservation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Id of the reserved vehicle.
     */
    private final String reservedVehicleId;

    /**
     * Reservation until. Unit: [ns].
     */
    private final long reservationUntil;

    /**
     * Reservation from. Unit: [ns].
     */
    private final long reservationFrom;

    /**
     * Creates a Reservation object.
     *
     * @param reservedVehicleId Id of the vehicle that has reserved the charging station
     * @param reservationFrom   The start time of the reservation
     * @param reservationUntil  The end time of the reservation
     */
    public Reservation(final String reservedVehicleId, final long reservationFrom, final long reservationUntil) {
        this.reservedVehicleId = reservedVehicleId;
        this.reservationUntil = reservationUntil;
        this.reservationFrom = reservationFrom;
    }

    public String getReservedVehicleId() {
        return reservedVehicleId;
    }

    public long getReservationUntil() {
        return reservationUntil;
    }

    public long getReservationFrom() {
        return reservationFrom;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 89)
                .append(reservedVehicleId)
                .append(reservationUntil)
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

        Reservation rhs = (Reservation) obj;
        return new EqualsBuilder()
                .append(this.reservedVehicleId, rhs.reservedVehicleId)
                .append(this.reservationUntil, rhs.reservationUntil)
                .isEquals();
    }

    @Override
    public String toString() {
        return "Reservation{" + "reservedVehicleId=" + reservedVehicleId + ", reservationUntil=" + reservationUntil + '}';
    }

}