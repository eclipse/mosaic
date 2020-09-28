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

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.UnitData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * This class encapsulates the semi-persistent state of a ChargingStation based on the
 * <em>ETSI TS 101 556-1</em> definition.
 * <p>
 * <em>An infrastructure, which provides one or several {@link ChargingSpot} units to supply electric
 * energy for charging EVs.</em>
 * </p>
 */
public final class ChargingStationData extends UnitData {

    private static final long serialVersionUID = 1L;

    /**
     * List of all {@link ChargingSpot} units belonging to this <code>ChargingStation</code>.
     */
    private final List<ChargingSpot> chargingSpots;

    private final ArrayList<String> rejectedVehicleIds;

    private int acceptedChargeRequest;

    private int deniedChargeRequest;

    /**
     * Number of vehicles the <code>ChargingStation</code> can serve at the same time.
     */
    private final int capacity;

    /**
     * Creates a new <code>ChargingStation</code> object.
     *
     * @param time                  current simulation time
     * @param name                  Unique identifier of the <code>ChargingStation</code>
     * @param longLat               GPS coordinates (WGS 84 decimal degrees) of the <code>ChargingStation</code>, axis
     *                              order: [x=longitude, y=latitude]
     * @param chargingSpots         List of all {@link ChargingSpot ChargingSpots} belonging to this
     *                              <code>ChargingStation</code>
     * @param rejectedVehicleIds    a List of Vehicle Ids which were rejected because of no free charging spot
     * @param acceptedChargeRequest number of accepted charge requests overall
     * @param deniedChargeRequest   number of rejected charge requests overall
     */

    public ChargingStationData(long time, String name, GeoPoint longLat, List<ChargingSpot> chargingSpots,
                               ArrayList<String> rejectedVehicleIds, int acceptedChargeRequest, int deniedChargeRequest) {
        super(time, name, longLat);
        this.chargingSpots = Collections.unmodifiableList(chargingSpots);
        this.rejectedVehicleIds = rejectedVehicleIds;
        this.acceptedChargeRequest = acceptedChargeRequest;
        this.deniedChargeRequest = deniedChargeRequest;
        int capacity = 0;
        for (ChargingSpot chargingSpot : this.chargingSpots) {
            capacity += chargingSpot.getParkingPlaces();
        }
        this.capacity = capacity;
    }

    public ChargingStationData createNewTime(final long time) {
        return new ChargingStationData(time, getName(), getPosition(), getChargingSpots(),
                getRejectedVehicles(), getAcceptedChargeRequest(), getDeniedChargeRequest());
    }

    /**
     * Sets a reservation for a free charging spot.
     *
     * @param reservation The vehicle reservation.
     * @return true, if a free charging spot was found
     */
    public boolean addReservation(Reservation reservation) {
        for (ChargingSpot chargingSpot : chargingSpots) {
            if (chargingSpot.isAvailable()) {
                chargingSpot.setAvailable(false);
                chargingSpot.setReservation(reservation);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of ids of all so far rejected vehicles.
     */
    public ArrayList<String> getRejectedVehicles() {
        return this.rejectedVehicleIds;
    }

    /**
     * Counts the rejected vehicles and adds them to a list.
     *
     * @param vehicleId of rejected vehicle
     */
    public void addRejectedVehicle(String vehicleId) {
        deniedChargeRequest++;
        this.rejectedVehicleIds.add(vehicleId);
    }

    public void incrementAcceptedChargeRequests() {
        acceptedChargeRequest++;
    }

    /**
     * Returns the number of vehicles the {@link ChargingSpot}s of this
     * <code>ChargingStation</code> can serve at the same time.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns <code>True</code>, if the <code>ChargingStation</code> has a free {@link ChargingSpot} available.
     * Charging Spots can be reserved by the Charging Station App or be engaged by a car without reservation app.
     */
    public boolean isAvailable() {
        for (ChargingSpot chargingSpot : chargingSpots) {
            if (chargingSpot.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the number of free {@link ChargingSpot}s.
     *
     * @return free spots.
     */
    public int getNumberOfAvailableSpots() {
        int available = 0;
        for (ChargingSpot chargingSpot : chargingSpots) {
            if (chargingSpot.isAvailable()) {
                available++;
            }
        }
        return available;
    }

    /**
     * Returns the number of reserved {@link ChargingSpot}s.
     *
     * @return reserved spots.
     */
    public int getNumberOfReservedSpots() {
        int reserved = 0;
        for (ChargingSpot chargingSpot : chargingSpots) {
            if (chargingSpot.isReserved() && !chargingSpot.isDocked()) {
                reserved++;
            }
        }
        return reserved;
    }

    /**
     * Returns the number of blocked {@link ChargingSpot}s.
     *
     * @return blocked spots. (vehicle is currently docked to charging station)
     */
    public int getNumberOfBlockedSpots() {
        int blocked = 0;
        for (ChargingSpot chargingSpot : chargingSpots) {
            if (chargingSpot.isDocked()) {
                blocked++;
            }
        }
        return blocked;
    }

    /**
     * Returns a list of {@link ChargingSpot}s.
     *
     * @return ChargingSpots
     */
    @Nonnull
    public List<ChargingSpot> getChargingSpots() {
        return this.chargingSpots;
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        return "ChargingStation [chargingSpots="
                + chargingSpots.subList(0, Math.min(chargingSpots.size(), maxLen)) + ", capacity=" + capacity
                + ", name=" + getName() + ", position="
                + getPosition() + "]";
    }

    /**
     * Returns the number of accepted Charge Requests.
     *
     * @return acceptedChargeRequests
     */

    public int getAcceptedChargeRequest() {
        return acceptedChargeRequest;
    }

    /**
     * Returns the number of denied Charge Requests.
     *
     * @return deniedChargeRequests
     */
    public int getDeniedChargeRequest() {
        return deniedChargeRequest;
    }
}
