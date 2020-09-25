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

package org.eclipse.mosaic.lib.objects.v2x.etsi.ivim;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.ToDataOutput;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Start or end position of a Segment.
 */
public class SegmentPosition implements ToDataOutput, Serializable {

    /**
     * Edge id.
     */
    private String edgeId;

    /**
     * Edge offset in m.
     */
    private double edgeOffset;

    /**
     * Geographical position.
     */
    private GeoPoint geoPosition;

    /**
     * Heading of the road of the specified geoPosition.
     */
    private double heading;

    public SegmentPosition() {

    }

    SegmentPosition(@Nonnull DataInput dataInput) throws IOException {
        this.edgeId = dataInput.readUTF();
        this.edgeOffset = dataInput.readDouble();
        this.geoPosition = GeoPoint.latLon(dataInput.readDouble(), dataInput.readDouble());
        this.heading = dataInput.readDouble();
    }

    @Override
    public void toDataOutput(@Nonnull DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(edgeId);
        dataOutput.writeDouble(edgeOffset);
        dataOutput.writeDouble(geoPosition.getLatitude());
        dataOutput.writeDouble(geoPosition.getLongitude());
        dataOutput.writeDouble(heading);
    }

    public SegmentPosition setEdgePosition(String edgeId, double edgeOffset) {
        this.edgeId = edgeId;
        this.edgeOffset = edgeOffset;
        return this;
    }

    public SegmentPosition setGeoPosition(GeoPoint position, double heading) {
        this.geoPosition = position;
        this.heading = heading;
        return this;
    }

    public String getEdgeId() {
        return edgeId;
    }

    public double getEdgeOffset() {
        return edgeOffset;
    }

    public GeoPoint getGeoPosition() {
        return geoPosition;
    }

    public double getHeading() {
        return heading;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 27)
                .append(this.edgeId)
                .append(this.edgeOffset)
                .append(this.geoPosition)
                .append(this.heading)
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

        SegmentPosition other = (SegmentPosition) obj;
        return new EqualsBuilder()
                .append(this.edgeId, other.getEdgeId())
                .append(this.edgeOffset, other.getEdgeOffset())
                .append(this.geoPosition, other.getGeoPosition())
                .append(this.heading, other.getHeading())
                .isEquals();
    }

    @Override
    public String toString() {
        return "SegmentPosition(edgeId: " + edgeId + ", offset: " + edgeOffset + ", geoPos: " + geoPosition + ", heading: " + heading + ")";
    }
}
