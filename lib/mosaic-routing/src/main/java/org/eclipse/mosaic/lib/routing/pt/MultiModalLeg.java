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

public class MultiModalLeg {

    public enum Type {
        WALKING, VEHICLE_SHARED, VEHICLE_PRIVATE, PUBLIC_TRANSPORT
    }

    private final Type legType;

    // For legs where a vehicle needs to be spawned
    private VehicleDeparture vehicleLeg = null;

    // For public transport legs
    private PtLeg publicTransportationLeg = null;

    // For walk legs
    private WalkLeg walkLeg = null;

    // For legs where a vehicle already exists
    private String sharedVehicleId = null;

    public long departureTime;
    public long arrivalTime;

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

    public Object getLeg() {
        return switch (legType) {
            case VEHICLE_PRIVATE -> vehicleLeg;
            case VEHICLE_SHARED -> sharedVehicleId;
            case PUBLIC_TRANSPORT -> publicTransportationLeg;
            case WALKING -> walkLeg;
        };
    }

    public Type getLegType() {
        return legType;
    }
}
