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

package org.eclipse.mosaic.lib.objects.v2x.etsi.cam;

import org.eclipse.mosaic.lib.enums.DriveDirection;
import org.eclipse.mosaic.lib.enums.VehicleClass;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import javax.annotation.concurrent.Immutable;

/**
 * This contains the AwarenessData for a CAM in the context of a vehicle acting as a sender.
 * This awareness data wrapps the HighFrequency as well as the optional LowFrequency block
 * as defined in ETSI EN 302 637-2 v1.3.1 (2014-09).
 */
@Immutable
public class VehicleAwarenessData implements AwarenessData {

    private static final long serialVersionUID = 1L;

    /**
     * Indicates a negative {@link #longitudinalAcceleration} of -16m/s^2 or less.
     */
    public final static double LONGITUDINAL_ACC_MAX_NEGATIVE = -16.0;

    /**
     * Indicates a negative {@link #longitudinalAcceleration} of 16m/s^2 or more.
     */
    public final static double LONGITUDINAL_ACC_MAX_POSITIVE = 16.0;

    /**
     * Indicates that a {@link #longitudinalAcceleration} is not available.
     */
    public final static double LONGITUDINAL_ACC_UNAVAILABLE = Double.MAX_VALUE;

    /**
     * Heading of the Vehicle. Unit: [degrees].
     */
    private final double heading;

    /**
     * Speed of the unit. Unit: [m/s].
     */
    private final double speed;

    /**
     * The vehicles driving direction
     * (so values like {@link this.longitudalAcceleration} can be correctly interpreted).
     */
    private final DriveDirection direction;

    /**
     * The road lane on which the vehicle is driving on (0 = rightmost lane).
     */
    private final int laneIndex;

    /**
     * Length of the vehicle in. Unit: [m].
     */
    private final double length;

    /**
     * Width of the vehicle in m. Unit: [m].
     */
    private final double width;

    /**
     * The longitudinal acceleration in [m/s^2].
     */
    private final double longitudinalAcceleration;

    /**
     * Type of the vehicle.
     */
    private final VehicleClass vehicleClass;

    public VehicleAwarenessData(DataInput dataInput) throws IOException {
        speed = dataInput.readDouble();
        length = dataInput.readDouble();
        width = dataInput.readDouble();
        heading = dataInput.readDouble();
        vehicleClass = VehicleClass.fromId(dataInput.readInt());
        direction = DriveDirection.fromId(dataInput.readInt());
        laneIndex = dataInput.readInt();
        longitudinalAcceleration = dataInput.readDouble();
    }

    public VehicleAwarenessData(
            final VehicleClass vehicleClass,
            final double speed, final double heading, final int laneIndex) {
        this(vehicleClass, speed, heading, laneIndex, 5, 2);
    }

    public VehicleAwarenessData(
            final VehicleClass vehicleClass,
            final double speed, final double heading, final int laneIndex,
            final double length, final double width
    ) {
        this(vehicleClass, speed, heading, length, width, DriveDirection.UNAVAILABLE, laneIndex, LONGITUDINAL_ACC_UNAVAILABLE);
    }

    public VehicleAwarenessData(
            final VehicleClass vehicleClass,
            final double speed, final double heading,
            final double length, final double width,
            final DriveDirection direction,
            final int laneIndex,
            final double longitudinalAcceleration
    ) {
        this.vehicleClass = vehicleClass;
        this.speed = speed;
        this.heading = heading;
        this.length = length;
        this.width = width;
        this.direction = direction;
        this.laneIndex = laneIndex;
        this.longitudinalAcceleration = longitudinalAcceleration;
    }

    public VehicleClass getVehicleClass() {
        return vehicleClass;
    }

    public double getWidth() {
        return width;
    }

    public double getLength() {
        return length;
    }

    public double getHeading() {
        return heading;
    }

    public double getSpeed() {
        return speed;
    }

    public DriveDirection getDirection() {
        return direction;
    }

    public int getLaneIndex() {
        return laneIndex;
    }

    public double getLongitudinalAcceleration() {
        return longitudinalAcceleration;
    }

    @Override
    public void toDataOutput(DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(AwarenessType.VEHICLE.id);
        dataOutput.writeDouble(speed);
        dataOutput.writeDouble(length);
        dataOutput.writeDouble(width);
        dataOutput.writeDouble(heading);
        dataOutput.writeInt(vehicleClass.id);
        dataOutput.writeInt(direction.id);
        dataOutput.writeInt(laneIndex);
        dataOutput.writeDouble(longitudinalAcceleration);
    }

    /**
     * This stores the string representation of this object for further use.
     */
    @SuppressWarnings(value = "JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS", justification = "is only used for caching purposes")
    private transient String cachedString = null;

    @Override
    public String toString() {
        if (cachedString == null) {
            StringBuilder sb = new StringBuilder("VehicleAwarenessData[");
            sb.append("vehicleClass:").append(vehicleClass);
            sb.append(", width:").append(width);
            sb.append(", length:").append(length);
            sb.append(", heading:").append(heading);
            sb.append(", speed:").append(speed);
            sb.append(", direction:").append(direction);
            sb.append(", lanePosition:").append(laneIndex);
            sb.append(", longitudinalAcceleration:");
            sb.append(
                    (longitudinalAcceleration == LONGITUDINAL_ACC_UNAVAILABLE)
                            ? "unavailable"
                            : longitudinalAcceleration
            );
            sb.append("]");
            cachedString = sb.toString();
        }
        return cachedString;
    }
}
