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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.mosaic.lib.enums.DriveDirection;
import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.junit.EtsiPayloadConfigurationRule;
import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.etsi.cam.VehicleAwarenessData;
import org.eclipse.mosaic.rti.TIME;

import org.junit.Rule;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class CamContentTest {

    @Rule
    public EtsiPayloadConfigurationRule messageConf = new EtsiPayloadConfigurationRule();

    @Test
    public void decodeMessage() {

        //SETUP
        VehicleAwarenessData awarenessData = new VehicleAwarenessData(
                VehicleClass.Car,
                33d,
                12d, 5.2,
                1.9,
                DriveDirection.FORWARD,
                0,
                0.3
        );
        GeoPoint position = GeoPoint.latLon(52.5, 13.3);
        byte[] userTaggedValue = "this is a test".getBytes(StandardCharsets.UTF_8);

        Cam cam = new Cam(mock(MessageRouting.class), new CamContent(4 * TIME.SECOND, awarenessData, "veh_1", position, userTaggedValue), 200);
        EncodedPayload encodedMessage = cam.getPayLoad();

        //PRE-ASSERT
        assertNotNull(encodedMessage.getBytes());
        assertTrue(encodedMessage.getBytes().length > 0);

        //RUN
        CamContent decodedCam = encodedMessage.decodePayload();

        //ASSERT
        assertNotNull(decodedCam);
        assertEquals(cam.getGenerationTime(), decodedCam.getGenerationTime());
        assertEquals(cam.getUnitID(), decodedCam.getUnitId());
        assertEquals(cam.getPosition(), decodedCam.getPosition());
        assertArrayEquals(cam.getUserTaggedValue(), decodedCam.getUserTaggedValue());
        assertEquals("this is a test", new String(decodedCam.getUserTaggedValue(), StandardCharsets.UTF_8));

        assertTrue(decodedCam.getAwarenessData() instanceof VehicleAwarenessData);

        VehicleAwarenessData decodedAwarenessData = (VehicleAwarenessData) decodedCam.getAwarenessData();
        assertEquals(awarenessData.getDirection(), decodedAwarenessData.getDirection());
        assertEquals(awarenessData.getLaneIndex(), decodedAwarenessData.getLaneIndex());
        assertEquals(awarenessData.getHeading(), decodedAwarenessData.getHeading(), 0.0001d);
        assertEquals(awarenessData.getLength(), decodedAwarenessData.getLength(), 0.0001d);
        assertEquals(awarenessData.getWidth(), decodedAwarenessData.getWidth(), 0.0001d);
        assertEquals(awarenessData.getSpeed(), decodedAwarenessData.getSpeed(), 0.0001d);
        assertEquals(awarenessData.getLongitudinalAcceleration(), decodedAwarenessData.getLongitudinalAcceleration(), 0.01);
        assertEquals(awarenessData.getVehicleClass(), decodedAwarenessData.getVehicleClass());
    }

}
