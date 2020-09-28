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
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroup;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TrafficLightAwarenessData extends RsuAwarenessData {

    private static final long serialVersionUID = 1L;
    
    /**
     * Attention: the trafficlight group is nor decoded or encoded into byte array, 
     * therefore it will not be available after a encoded DENM message has been decoded.
     */
    private transient TrafficLightGroup trafficLightGroup = null;
    
    public TrafficLightAwarenessData(DataInput dataInput) throws IOException {
        super(dataInput);
    }

    public TrafficLightAwarenessData(TrafficLightGroup tlg) {
        super(RsuType.TRAFFIC_LIGHT);
        this.trafficLightGroup = tlg;
    }
    
    public TrafficLightGroup getTrafficLightGroup() {
        return trafficLightGroup;
    }
    
    @Override
    public void toDataOutput(DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(AwarenessType.TRAFFIC_LIGHT.id);
        dataOutput.writeInt(getRsuType().id);
    }

}
