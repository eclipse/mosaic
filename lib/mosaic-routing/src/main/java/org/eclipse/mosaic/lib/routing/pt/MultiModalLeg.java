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

    private Type legType;

    //For legs where a vehicle needs to be spawned
    private VehicleDeparture vehicleLeg = null;

    //For PTlegs
    private PtLeg publicTransportationLeg = null;

    private WalkLeg walkLeg = null;

    //For legs where a vehicle already exists
    private String carID = null;

    public long departureTime;
    public long arrivalTime;

    public MultiModalLeg(VehicleDeparture vehicleLeg, long departureTime, long arrivalTime) {
        this.legType = Type.VEHICLE_PRIVATE;
        this.vehicleLeg = vehicleLeg;

        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public MultiModalLeg(PtLeg ptRoute, long departureTime, long arrivalTime) {
        this.legType = Type.PUBLIC_TRANSPORT;
        this.publicTransportationLeg = ptRoute;

        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public MultiModalLeg(WalkLeg walkLeg, long departureTime, long arrivalTime) {
        this.legType = Type.WALKING;
        this.walkLeg = walkLeg;

        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public MultiModalLeg(String carID, long departureTime, long arrivalTime) {
        this.legType = Type.VEHICLE_PRIVATE;
        this.carID = carID;

        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public Object getLeg() {
        return switch (legType) {
            case VEHICLE_PRIVATE -> vehicleLeg;
            case VEHICLE_SHARED -> carID;
            case PUBLIC_TRANSPORT -> publicTransportationLeg;
            case WALKING -> walkLeg;
        };
    }

    public Type getLegType() {
        return legType;
    }
}
