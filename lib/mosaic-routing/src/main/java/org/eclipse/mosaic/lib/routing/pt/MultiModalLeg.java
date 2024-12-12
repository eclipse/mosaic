/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.routing.pt;

import org.eclipse.mosaic.lib.objects.vehicle.VehicleDeparture;

/**
 * A leg of a multi-modal-journey consists of a planned departure and planned arrival time, and
 * details about the transport mode, such as the walking path or the public transport route.
 */
public class MultiModalLeg {

    public enum Type {
        /**
         * For legs which uses public transport.
         */
        PUBLIC_TRANSPORT,
        /**
         * For legs which will require foot work.
         */
        WALKING,
        /**
         * For legs which require to spawn a new vehicle.
         */
        VEHICLE_PRIVATE,
        /**
         * For legs which uses a shared vehicle which already exists in the simulation.
         */
        VEHICLE_SHARED
    }

    private final Type legType;
    private final long departureTime;
    private final long arrivalTime;

    /**
     * For legs which uses public transport.
     */
    private PtLeg publicTransportationLeg = null;

    /**
     * For legs which will require foot work.
     */
    private WalkLeg walkLeg = null;

    /**
     * For legs which require to spawn a new vehicle.
     */
    private VehicleDeparture vehicleLeg = null;

    /**
     * For legs which uses a shared vehicle which already exists in the simulation.
     */
    private String sharedVehicleId = null;

    /**
     * Creates a new leg in which a new vehicle must be spawned.
     */
    public MultiModalLeg(VehicleDeparture vehicleLeg, long departureTime, long arrivalTime) {
        this.legType = Type.VEHICLE_PRIVATE;
        this.vehicleLeg = vehicleLeg;

        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    /**
     * Creates a new leg which uses public transport.
     */
    public MultiModalLeg(PtLeg ptRoute, long departureTime, long arrivalTime) {
        this.legType = Type.PUBLIC_TRANSPORT;
        this.publicTransportationLeg = ptRoute;

        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    /**
     * Creates a new leg which uses walking mode.
     */
    public MultiModalLeg(WalkLeg walkLeg, long departureTime, long arrivalTime) {
        this.legType = Type.WALKING;
        this.walkLeg = walkLeg;

        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    /**
     * Creates a new leg which uses a shared vehicle by a given vehicle ID.
     */
    public MultiModalLeg(String vehicleId, long departureTime, long arrivalTime) {
        this.legType = Type.VEHICLE_PRIVATE;
        this.sharedVehicleId = vehicleId;

        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public long getDepartureTime() {
        return departureTime;
    }

    public Type getLegType() {
        return legType;
    }

    public Object getLeg() {
        return switch (legType) {
            case VEHICLE_PRIVATE -> vehicleLeg;
            case VEHICLE_SHARED -> sharedVehicleId;
            case PUBLIC_TRANSPORT -> publicTransportationLeg;
            case WALKING -> walkLeg;
        };
    }
}
