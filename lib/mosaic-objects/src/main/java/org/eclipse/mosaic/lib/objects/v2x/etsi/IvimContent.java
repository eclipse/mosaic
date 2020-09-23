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

import org.eclipse.mosaic.lib.objects.ToDataOutput;
import org.eclipse.mosaic.lib.objects.v2x.etsi.ivim.Segment;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class IvimContent implements ToDataOutput, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Current time stamp of the sending node. Unit: [ns].
     */
    private final long time;

    /**
     * Inframix: List of segments
     */
    private List<Segment> segments = new ArrayList<>();

    public IvimContent(final long time) {
        this.time = time;
    }

    public IvimContent(@Nonnull DataInput din) throws IOException {
        this.time = din.readLong();
        int listLength = din.readInt();
        for (int i = 0; i < listLength; i++) {
            segments.add(new Segment(din));
        }
    }

    IvimContent(final IvimContent ivimContent) {
        this(ivimContent.getTime());
        this.segments = ivimContent.getSegments();
    }

    @Override
    public void toDataOutput(DataOutput dataOutput) throws IOException {
        dataOutput.writeLong(time);
        dataOutput.writeInt(segments.size());
        for (Segment segment : segments) {
            segment.toDataOutput(dataOutput);
        }
    }

    public IvimContent addSegment(Segment segment) {
        segments.add(segment);
        return this;
    }

    public IvimContent addSegments(List<Segment> segments) {
        this.segments.addAll(segments);
        return this;
    }

    public long getTime() {
        return time;
    }

    public List<Segment> getSegments() {
        return segments;
    }
}
