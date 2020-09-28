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

package org.eclipse.mosaic.lib.objects.v2x.etsi.cam;

import org.eclipse.mosaic.lib.enums.RsuType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import javax.annotation.concurrent.Immutable;

@Immutable
public class RsuAwarenessData implements AwarenessData {

    private static final long serialVersionUID = 1L;

    /**
     * Type of the rsu.
     */
    private final RsuType rsuType;

    public RsuAwarenessData(DataInput dataInput) throws IOException {
        rsuType = RsuType.fromId(dataInput.readInt());
    }

    public RsuAwarenessData(RsuType type) {
        this.rsuType = type;
    }

    public RsuType getRsuType() {
        return rsuType;
    }

    @Override
    public void toDataOutput(DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(AwarenessType.RSU.id);
        dataOutput.writeInt(rsuType.id);
    }

    @Override
    public String toString() {
        return String.format("[RsuType:%s]", rsuType);
    }

}
