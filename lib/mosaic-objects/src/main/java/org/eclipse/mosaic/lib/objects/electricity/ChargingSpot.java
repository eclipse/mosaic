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

package org.eclipse.mosaic.lib.objects.electricity;

import java.io.Serializable;

/**
 * Definition of an {@link ChargingSpot} based on the <em>ETSI TS 101 556-1</em> definition.
 * <p>
 * <em>A set of 1 to 4 parking places arranged around a pole, where it is possible to charge an EV.</em>
 * </p>
 */
public final class ChargingSpot implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier of the {@link ChargingSpot}.
     */
    private final String name;

    /**
     * Type of this {@link ChargingSpot} in compliance with current standards, including.
     * <em>IEC 62196-2</em>.
     */
    private final int type;

    /**
     * Number of available parking places, 1 to 4 parking places arranged around a pole.
     */
    private final int parkingPlaces;

    /**
     * Flag, indicating if the {@link ChargingSpot} is available.
     */
    private boolean available;

    /**
     * The {@link Reservation} existing for this {@link ChargingSpot}.
     */
    private Reservation reservation;

    /**
     * The id of the vehicle which is already docked to this {@link ChargingSpot}.
     */
    private String dockedVehicle;

    /**
     * Creates a new {@link ChargingSpot} object.
     *
     * @param name          Unique identifier of the {@link ChargingSpot}
     * @param type          Type of this {@link ChargingSpot} in compliance with current standards,
     *                      including <em>IEC 62196-2</em>
     * @param parkingPlaces Number of available parking places, 1 to 4 parking places arranged around a pole
     */
    public ChargingSpot(String name, int type, int parkingPlaces) {
        this.name = name;
        this.type = type;
        this.parkingPlaces = parkingPlaces;
        this.available = true;
        this.reservation = new Reservation("no reservation", 0, 0);
        this.dockedVehicle = null;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public int getParkingPlaces() {
        return parkingPlaces;
    }

    /**
     * Returns the maximum voltage based on the type.
     *
     * @return Maximum voltage available at this <code>ChargingSpot</code>, unit: [V]
     */
    public int getMaximumVoltage() {
        switch (type) {
            case 0: // Mode 1 3,7kW
                return 230;
            case 1: // Mode 2 11kW
                return 400;
            case 2: // Mode 3 22kW
                return 400;
            case 3: // Mode 4 44kW
                return 400;
            default: // fall back
                return 0;
        }
    }

    /**
     * @return Maximum current available at this <code>ChargingSpot</code>, unit: [A]
     */
    public int getMaximumCurrent() {
        switch (type) {
            case 0: // Mode 1 3,7kW
                return 16;
            case 1: // Mode 2 11kW
                return 16;
            case 2: // Mode 3 22kW
                return 32;
            case 3: // Mode 4 44kW
                return 63;
            default: // fall back
                return 0;
        }

    }

    /**
     * Returns the maximum power available at this {@link ChargingSpot}, unit: [W].
     */
    public int getMaximumPower() {
        return getMaximumVoltage() * getMaximumCurrent();
    }

    /**
     * Returns {@code True}, if the {@link ChargingSpot} has at least one parking place available.
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Returns {@code True}, if the {@link ChargingSpot} is reserved for a vehicle.
     */
    public boolean isReserved() {
        return !(this.reservation.getReservedVehicleId().equals("no reservation"));
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getDockedVehicle() {
        return dockedVehicle;
    }

    public void dock(String dockedVehicle) {
        this.dockedVehicle = dockedVehicle;
    }

    public void undock() {
        this.dockedVehicle = null;
    }

    public boolean isDocked() {
        return dockedVehicle != null;
    }

    @Override
    public String toString() {
        return "ChargingSpot [name=" + name + ", type=" + type + ", parkingPlaces=" + parkingPlaces
                + ", available=" + available + ", reservation=" + reservation.toString() + ", dockedVehicle=" + dockedVehicle + ", maximumVoltage=" + getMaximumVoltage()
                + ", maximumCurrent=" + getMaximumCurrent() + ", maximumPower=" + getMaximumPower()
                + "]";
    }

}
