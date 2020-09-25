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

package org.eclipse.mosaic.lib.objects.v2x.etsi;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.ToDataOutput;
import org.eclipse.mosaic.lib.objects.v2x.etsi.cam.AwarenessData;
import org.eclipse.mosaic.lib.objects.v2x.etsi.cam.AwarenessType;
import org.eclipse.mosaic.lib.objects.v2x.etsi.cam.RsuAwarenessData;
import org.eclipse.mosaic.lib.objects.v2x.etsi.cam.TrafficLightAwarenessData;
import org.eclipse.mosaic.lib.objects.v2x.etsi.cam.VehicleAwarenessData;
import org.eclipse.mosaic.lib.util.SerializationUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Contains specific CAM data
 */
public class CamContent implements ToDataOutput, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Time the CAM was created.
     */
    private final long generationTime;

    /**
     * The Id of the unit which created this CAM.
     */
    private final String unitID;

    /**
     * The position where this CAM has been created.
     */
    private final GeoPoint position;

    /**
     * The high-frequency data container of this CAM.
     */
    private final AwarenessData awarenessData;

    private final byte[] userTaggedValue;

    /**
     * Creates a new {@link CamContent} object.
     *
     * @param generationTime  the simulation time in ns the CAM was created.
     * @param awarenessData   the high-frequency data container of this CAM.
     * @param unitID          the Id of the unit which created this CAM.
     * @param position        the position at which this CAM has been created
     * @param userTaggedValue custom data to transmit with this CAM
     */
    public CamContent(
            final long generationTime,
            @Nonnull final AwarenessData awarenessData,
            @Nonnull final String unitID,
            final GeoPoint position,
            final byte[] userTaggedValue
    ) {
        this.generationTime = generationTime;
        this.awarenessData = Objects.requireNonNull(awarenessData);
        this.unitID = Objects.requireNonNull(unitID);
        this.position = position;
        this.userTaggedValue = (userTaggedValue != null) ? userTaggedValue.clone() : null;
    }

    public CamContent(DataInput dIn) throws IOException {
        dIn.readInt(); //version
        this.generationTime = dIn.readLong();
        this.unitID = dIn.readUTF();
        if (dIn.readBoolean()) {
            this.position = SerializationUtils.decodeGeoPoint(dIn);
        } else {
            this.position = null;
        }

        AwarenessType awarenessType = AwarenessType.fromId(dIn.readByte());
        switch (awarenessType) {
            case VEHICLE:
                this.awarenessData = new VehicleAwarenessData(dIn);
                break;
            case RSU:
                this.awarenessData = new RsuAwarenessData(dIn);
                break;
            case TRAFFIC_LIGHT:
                this.awarenessData = new TrafficLightAwarenessData(dIn);
                break;
            default:
                this.awarenessData = null;
        }

        if (dIn.readBoolean()) {
            this.userTaggedValue = new byte[dIn.readInt()];
            for (int b = 0; b < userTaggedValue.length; b++) {
                userTaggedValue[b] = dIn.readByte();
            }
        } else {
            this.userTaggedValue = null;
        }
    }

    CamContent(CamContent camContent) {
        this(camContent.getGenerationTime(), camContent.getAwarenessData(), camContent.getUnitId(), camContent.getPosition(), camContent.getUserTaggedValue());
    }

    @Override
    public void toDataOutput(DataOutput dataOutput) throws IOException {
        //write protocol version and general header
        dataOutput.writeInt(0); //protocol version: defined in CAM standard
        dataOutput.writeLong(generationTime);
        dataOutput.writeUTF(unitID);

        dataOutput.writeBoolean(position != null);
        if (position != null) {
            SerializationUtils.encodeGeoPoint(dataOutput, position);
        }

        // write message body
        awarenessData.toDataOutput(dataOutput);

        // write tagged values in three steps

        // first step: boolean flag
        dataOutput.writeBoolean(userTaggedValue != null);

        // third step: write the userDefinedValue
        if (userTaggedValue != null) {
            dataOutput.writeInt(userTaggedValue.length);
            dataOutput.write(userTaggedValue);
        }
    }

    public long getGenerationTime() {
        return generationTime;
    }

    public String getUnitId() {
        return unitID;
    }

    public GeoPoint getPosition() {
        return position;
    }

    public AwarenessData getAwarenessData() {
        return awarenessData;
    }

    public byte[] getUserTaggedValue() {
        if (userTaggedValue == null) {
            return null;
        }
        return userTaggedValue.clone();
    }

}
