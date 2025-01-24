/*
 * Copyright (c) 2023 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.objects.pt;

import org.eclipse.mosaic.lib.enums.VehicleStopMode;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * Additional data holding information about the public transport line of the vehicle associated with this.
 * The data is generated and filled by SumoAmbassador and can be consumed by any vehicle application
 * which is mapped onto the public transport vehicle.
 */
public class PtVehicleData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The line the train belongs to.
     */
    private final String lineId;

    /**
     * Contains a list of the next stops of the vehicle, e.g., train or bus.
     */
    private final List<StoppingPlace> nextStops;

    private PtVehicleData(String lineId, List<StoppingPlace> nextStops) {
        this.lineId = lineId;
        this.nextStops = nextStops;
    }

    public String getLineId() {
        return lineId;
    }

    public List<StoppingPlace> getNextStops() {
        return nextStops;
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

        PtVehicleData other = (PtVehicleData) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.lineId, other.lineId)
                .append(this.nextStops, other.nextStops)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 89)
                .appendSuper(super.hashCode())
                .append(lineId)
                .append(nextStops)
                .toHashCode();
    }

    public static class Builder {
        private String lineId;
        private List<StoppingPlace> nextStops;

        public Builder withLineId(String lineId) {
            this.lineId = lineId;
            return this;
        }

        public Builder nextStops(List<StoppingPlace> nextStops) {
            this.nextStops = nextStops;
            return this;
        }

        public PtVehicleData build() {
            return new PtVehicleData(lineId, nextStops);
        }
    }

    /**
     * Class representing a vehicle stopping place.
     */
    public static class StoppingPlace implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * Id of the stop.
         */
        private final String stoppingPlaceId;
        /**
         * Lane the stop is on.
         */
        private final String laneId;
        /**
         * Start position of the stop on lane.
         */
        private final double startPos;
        /**
         * End position of the stop on lane. (also the position the vehicle will halt at)
         */
        private final double endPos;
        /**
         * Minimum duration of the stop.
         */
        private final double stopDuration;
        /**
         * Time until the vehicle ist stopped here.
         */
        private final double stoppedUntil;
        /**
         * Int representing the type of stop.
         */
        private final VehicleStopMode stopType;

        private StoppingPlace(String stoppingPlaceId, String laneId, double startPos, double endPos,
                              VehicleStopMode stopType, double stopDuration, double stoppedUntil) {
            this.stoppingPlaceId = stoppingPlaceId;
            this.laneId = laneId;
            this.startPos = startPos;
            this.endPos = endPos;
            this.stopType = stopType;
            this.stoppedUntil = stoppedUntil;
            this.stopDuration = stopDuration;
        }

        public String getStoppingPlaceId() {
            return stoppingPlaceId;
        }

        public String getLaneId() {
            return laneId;
        }

        public double getStartPos() {
            return startPos;
        }

        public double getEndPos() {
            return endPos;
        }

        public double getStopDuration() {
            return stopDuration;
        }

        public double getStoppedUntil() {
            return stoppedUntil;
        }

        public VehicleStopMode getStopType() {
            return stopType;
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

            StoppingPlace other = (StoppingPlace) obj;
            return new EqualsBuilder()
                    .append(this.getStoppingPlaceId(), other.getStoppingPlaceId())
                    .append(this.getLaneId(), other.getLaneId())
                    .append(this.getStartPos(), other.getStartPos())
                    .append(this.getEndPos(), other.getEndPos())
                    .append(this.getStopDuration(), other.getStopDuration())
                    .append(this.getStoppedUntil(), other.getStoppedUntil())
                    .append(this.getStopType(), other.getStopType())
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(3, 89)
                    .appendSuper(super.hashCode())
                    .append(stoppingPlaceId)
                    .append(laneId)
                    .append(startPos)
                    .append(endPos)
                    .append(stopDuration)
                    .append(stoppedUntil)
                    .append(stopType)
                    .toHashCode();
        }

        public static class Builder {
            private String stoppingPlaceId;
            private String laneId;
            private double startPos;
            private double endPos;
            private VehicleStopMode stopType;
            private double stopDuration;
            private double stoppedUntil;


            public Builder stoppingPlaceId(String stoppingPlaceId) {
                this.stoppingPlaceId = stoppingPlaceId;
                return this;
            }

            public Builder laneId(String laneId) {
                this.laneId = laneId;
                return this;
            }

            public Builder startPos(double startPos) {
                this.startPos = startPos;
                return this;
            }

            public Builder endPos(double endPos) {
                this.endPos = endPos;
                return this;
            }

            public Builder stopFlags(VehicleStopMode stopType) {
                this.stopType = stopType;
                return this;
            }

            public Builder stopDuration(double stopDuration) {
                this.stopDuration = stopDuration;
                return this;
            }

            public Builder stoppedUntil(double stoppedUntil) {
                this.stoppedUntil = stoppedUntil;
                return this;
            }

            public StoppingPlace build() {
                return new StoppingPlace(stoppingPlaceId, laneId, startPos, endPos, stopType, stopDuration, stoppedUntil);
            }
        }
    }
}
