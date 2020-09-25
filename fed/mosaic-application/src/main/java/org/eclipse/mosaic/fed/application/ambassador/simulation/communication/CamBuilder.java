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

package org.eclipse.mosaic.fed.application.ambassador.simulation.communication;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.v2x.etsi.CamContent;
import org.eclipse.mosaic.lib.objects.v2x.etsi.cam.AwarenessData;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.Validate;

/**
 * A builder class to assemble Cooperative Awareness Messages {@link org.eclipse.mosaic.lib.objects.v2x.etsi.Cam}.
 */
public class CamBuilder {

    private GeoPoint position;

    private AwarenessData awarenessData;

    private byte[] userTaggedValue;

    @SuppressWarnings("UnusedReturnValue")
    public CamBuilder position(GeoPoint position) {
        this.position = position;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public CamBuilder awarenessData(AwarenessData awarenessData) {
        this.awarenessData = awarenessData;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public CamBuilder userTaggedValue(byte[] userTaggedValue) {
        this.userTaggedValue = SerializationUtils.clone(userTaggedValue);
        return this;
    }


    /**
     * Assembles the {@link org.eclipse.mosaic.lib.objects.v2x.etsi.Cam}.
     *
     * @param time   time of the CAM
     * @param unitId unit that send the CAM
     * @return the content of the CAM
     */
    public CamContent create(long time, String unitId) {
        return new CamContent(time,
                Validate.notNull(awarenessData),
                Validate.notNull(unitId),
                position,
                userTaggedValue
        );
    }
}
