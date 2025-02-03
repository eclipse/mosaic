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

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.pt.PtTrip;

import java.util.ArrayList;
import java.util.List;

/**
 * A public transport route which consists of multiple legs, which either
 * use a public transport facility, or walking.
 */
public class PtRoute {

    private final List<Leg> legs = new ArrayList<>();

    public PtRoute(List<Leg> legs) {
        this.legs.addAll(legs);
    }

    /**
     * Returns the individual legs of this public transport route, which can
     * either be of type {@link PtLeg} or {@link WalkLeg}.
     */
    public List<Leg> getLegs() {
        return legs;
    }

    public static abstract class Leg {

        private final long departureTime;
        private final long arrivalTime;

        protected Leg(long departureTime, long arrivalTime) {
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
        }

        public long getArrivalTime() {
            return arrivalTime;
        }

        public long getDepartureTime() {
            return departureTime;
        }
    }

    public static class PtLeg extends Leg {

        private final PtTrip ptTrip;

        public PtLeg(long departureTime, long arrivalTime, PtTrip publicTransportTrip) {
            super(departureTime, arrivalTime);
            this.ptTrip = publicTransportTrip;
        }

        public PtTrip getPtTrip() {
            return ptTrip;
        }
    }

    public static class WalkLeg extends Leg {

        private final List<GeoPoint> waypoints = new ArrayList<>();

        public WalkLeg(long departureTime, long arrivalTime, List<GeoPoint> waypoints) {
            super(departureTime, arrivalTime);
            this.waypoints.addAll(waypoints);
        }

        /**
         * Returns the list of geographical positions which form the walking route.
         */
        public final List<GeoPoint> getWaypoints() {
            return waypoints;
        }
    }

}
