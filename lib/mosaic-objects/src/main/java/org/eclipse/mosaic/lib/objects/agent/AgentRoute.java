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

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.pt.PtTrip;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleDeparture;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        AgentRoute that = (AgentRoute) other;
        return new EqualsBuilder().append(legs, that.legs).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(legs).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("legs", legs)
                .toString();
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

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            Leg leg = (Leg) other;
            return new EqualsBuilder().append(departureTime, leg.departureTime).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(departureTime).toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                    .append("departureTime", departureTime)
                    .toString();
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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            PrivateVehicleLeg that = (PrivateVehicleLeg) o;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(vehicleType, that.vehicleType)
                    .append(departure, that.departure)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .appendSuper(super.hashCode())
                    .append(vehicleType)
                    .append(departure)
                    .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                    .appendSuper(super.toString())
                    .append("vehicleType", vehicleType)
                    .append("departure", departure)
                    .toString();
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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            SharedVehicleLeg that = (SharedVehicleLeg) o;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(vehicleId, that.vehicleId)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .appendSuper(super.hashCode())
                    .append(vehicleId)
                    .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                    .appendSuper(super.toString())
                    .append("vehicleId", vehicleId)
                    .toString();
        }
    }

    public static class PtLeg extends Leg {

        private static final long serialVersionUID = 1L;

        private final PtTrip ptTrip;

        public PtLeg(long departureTime, PtTrip publicTransportTrip) {
            super(departureTime);
            this.ptTrip = publicTransportTrip;
        }

        /**
         * Returns the {@link PtTrip} object which holds all the information about the public transport trip of this leg.
         */
        public PtTrip getTrip() {
            return ptTrip;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            PtLeg that = (PtLeg) o;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(ptTrip, that.ptTrip)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .appendSuper(super.hashCode())
                    .append(ptTrip)
                    .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                    .appendSuper(super.toString())
                    .append("ptTrip", ptTrip)
                    .toString();
        }
    }

    public static class WalkLeg extends Leg {

        private static final long serialVersionUID = 1L;

        private final List<GeoPoint> waypoints = new ArrayList<>();
        private final double walkingSpeed;

        public WalkLeg(long departureTime, List<GeoPoint> waypoints, double walkingSpeed) {
            super(departureTime);
            this.waypoints.addAll(waypoints);
            this.walkingSpeed = walkingSpeed;
        }

        /**
         * Return all waypoints of this leg.
         */
        public final List<GeoPoint> getWaypoints() {
            return waypoints;
        }

        public double getWalkingSpeed() {
            return walkingSpeed;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            WalkLeg that = (WalkLeg) o;
            return new EqualsBuilder()
                    .appendSuper(super.equals(o))
                    .append(waypoints, that.waypoints)
                    .append(walkingSpeed, that.walkingSpeed)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .appendSuper(super.hashCode())
                    .append(waypoints)
                    .append(walkingSpeed)
                    .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                    .appendSuper(super.toString())
                    .append("waypoints", waypoints)
                    .append("walkingSpeed", walkingSpeed)
                    .toString();
        }
    }

}
