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

package org.eclipse.mosaic.lib.objects.agent;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.pt.PtStop;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleDeparture;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A multi-modal route for an agent which consists of one or more legs. Each
 * leg can use a different modality, such as public transport, walking, or using
 * a private or shared vehicle.
 */
public class AgentRoute implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Leg> legs = Lists.newArrayList();

    public AgentRoute(List<Leg> legs) {
        this.legs.addAll(legs);
    }

    /**
     * Returns all legs of the agent route.
     */
    public List<Leg> getLegs() {
        return legs;
    }

    public static abstract class Leg implements Serializable {

        private static final long serialVersionUID = 1L;

        private final long departureTime;

        private Leg(long departureTime) {
            this.departureTime = departureTime;
        }

        public long getDepartureTime() {
            return departureTime;
        }
    }

    public static class PrivateVehicleLeg extends Leg {

        private static final long serialVersionUID = 1L;

        private final String vehicleType;
        private final VehicleDeparture departure;

        public PrivateVehicleLeg(long departureTime, String vehicleType, VehicleDeparture departure) {
            super(departureTime);
            this.vehicleType = vehicleType;
            this.departure = departure;
        }

        public String getVehicleType() {
            return vehicleType;
        }

        public VehicleDeparture getDeparture() {
            return departure;
        }
    }

    public static class SharedVehicleLeg extends Leg {

        private static final long serialVersionUID = 1L;

        private final String vehicleId;

        public SharedVehicleLeg(long departureTime, String vehicleId) {
            super(departureTime);
            this.vehicleId = vehicleId;
        }

        public String getVehicleId() {
            return vehicleId;
        }
    }

    public static class PtLeg extends Leg {

        private static final long serialVersionUID = 1L;

        private final List<PtStop> stops = new ArrayList<>();

        public PtLeg(long departureTime, List<PtStop> stops) {
            super(departureTime);
            this.stops.addAll(stops);
        }

        public final List<PtStop> getStops() {
            return stops;
        }
    }

    public static class WalkLeg extends Leg {

        private static final long serialVersionUID = 1L;

        private final List<GeoPoint> waypoints = new ArrayList<>();

        public WalkLeg(long departureTime, List<GeoPoint> waypoints) {
            super(departureTime);
            this.waypoints.addAll(waypoints);
        }

        public final List<GeoPoint> getWaypoints() {
            return waypoints;
        }
    }

}
