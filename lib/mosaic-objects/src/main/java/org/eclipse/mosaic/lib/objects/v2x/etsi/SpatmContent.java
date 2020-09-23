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

package org.eclipse.mosaic.lib.objects.v2x.etsi;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.ToDataOutput;
import org.eclipse.mosaic.lib.util.SerializationUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 * Signal Phase and Timing Message
 */
public class SpatmContent implements ToDataOutput, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Current time stamp of the sending node
     */
    private final long time;

    /**
     * GPS-position of the sending node
     * coordinates of the sending node
     */
    private final GeoPoint senderPosition;

    /**
     * Current phase in boolean values.
     */
    private final boolean phaseRed; //was true
    private final boolean phaseYellow; // was false
    private final boolean phaseGreen; // was false

    /**
     * Phases of all phases (red, yellow, green). Unit: [ns].
     */
    private final long phaseDurationRed;
    private final long phaseDurationYellow;
    private final long phaseDurationGreen;

    /**
     * Remaining phase time (regardless of interleaving phases with other traffic lights,
     * i.e. only real state changes of this TL is regarded
     */
    private final long remainingPhaseTime;

    public SpatmContent(final long time, final GeoPoint senderPosition, final boolean phaseRed, final boolean phaseYellow, final boolean phaseGreen, final long phaseDurationRed, final long phaseDurationYellow, final long phaseDurationGreen, final long remainingPhaseTime) {
        this.time = time;
        this.senderPosition = senderPosition;
        this.phaseRed = phaseRed;
        this.phaseYellow = phaseYellow;
        this.phaseGreen = phaseGreen;
        this.phaseDurationRed = phaseDurationRed;
        this.phaseDurationYellow = phaseDurationYellow;
        this.phaseDurationGreen = phaseDurationGreen;
        this.remainingPhaseTime = remainingPhaseTime;
    }

    public SpatmContent(final DataInput dIn) throws IOException {
        this.time = dIn.readLong();
        this.senderPosition = SerializationUtils.decodeGeoPoint(dIn);
        this.phaseRed = dIn.readBoolean();
        this.phaseYellow = dIn.readBoolean();
        this.phaseGreen = dIn.readBoolean();
        this.phaseDurationRed = dIn.readLong();
        this.phaseDurationYellow = dIn.readLong();
        this.phaseDurationGreen = dIn.readLong();
        this.remainingPhaseTime = dIn.readLong();
    }

    SpatmContent(final SpatmContent spatm) {
        this(spatm.getTime(), spatm.getSenderPosition(), spatm.isPhaseRed(), spatm.isPhaseYellow(), spatm.isPhaseGreen(), spatm.getPhaseDurationRed(), spatm.getPhaseDurationYellow(), spatm.getPhaseDurationGreen(), spatm.getRemainingPhaseTime());
    }

    public long getTime() {
        return time;
    }

    public GeoPoint getSenderPosition() {
        return senderPosition;
    }

    public boolean isPhaseRed() {
        return phaseRed;
    }

    public boolean isPhaseYellow() {
        return phaseYellow;
    }

    public boolean isPhaseGreen() {
        return phaseGreen;
    }

    public long getPhaseDurationRed() {
        return phaseDurationRed;
    }

    public long getPhaseDurationYellow() {
        return phaseDurationYellow;
    }

    public long getPhaseDurationGreen() {
        return phaseDurationGreen;
    }

    public long getRemainingPhaseTime() {
        return remainingPhaseTime;
    }

    @Override
    public void toDataOutput(DataOutput dataOutput) throws IOException {
        dataOutput.writeLong(time);
        SerializationUtils.encodeGeoPoint(dataOutput, senderPosition);
        dataOutput.writeBoolean(phaseRed);
        dataOutput.writeBoolean(phaseYellow);
        dataOutput.writeBoolean(phaseGreen);
        dataOutput.writeLong(phaseDurationRed);
        dataOutput.writeLong(phaseDurationYellow);
        dataOutput.writeLong(phaseDurationGreen);
        dataOutput.writeLong(remainingPhaseTime);
    }
}
