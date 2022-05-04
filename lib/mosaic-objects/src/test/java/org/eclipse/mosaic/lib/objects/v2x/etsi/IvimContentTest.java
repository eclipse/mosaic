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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.junit.EtsiPayloadConfigurationRule;
import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.etsi.ivim.Advice;
import org.eclipse.mosaic.lib.objects.v2x.etsi.ivim.Segment;
import org.eclipse.mosaic.lib.objects.v2x.etsi.ivim.SegmentPosition;
import org.eclipse.mosaic.rti.TIME;

import org.junit.Rule;
import org.junit.Test;

public class IvimContentTest {

    @Rule
    public EtsiPayloadConfigurationRule messageConf = new EtsiPayloadConfigurationRule();

    @Test
    public void decodeMessage() {
        //SETUP
        SegmentPosition startPosition = new SegmentPosition()
                .setEdgePosition("edge-0", 42.0)
                .setGeoPosition(GeoPoint.latLon(22.22, 33.33), 40.3);

        SegmentPosition endPosition = new SegmentPosition()
                .setEdgePosition("edge-1", 24.0)
                .setGeoPosition(GeoPoint.latLon(33.33, 44.44), 34.0);

        Advice advice = new Advice()
                .setAccelerationFactor(0.9)
                .setSpeedAdvice(36.33)
                .setGap(2.1);
        Segment segment = new Segment("name")
                .setStartPosition(startPosition)
                .setEndPosition(endPosition)
                .putAdvice(0, advice);
        IvimContent ivimContent = new IvimContent(4 * TIME.SECOND)
                .addSegment(segment);
        Ivim ivim = new Ivim(mock(MessageRouting.class), ivimContent, 200);
        EncodedPayload encodedMessage = ivim.getPayLoad();

        //PRE-ASSERT
        assertTrue(encodedMessage.getBytes().length > 0);

        //RUN
        IvimContent decodeMessage = (IvimContent) encodedMessage.decodePayload();

        //ASSERT
        assertNotNull(decodeMessage);

        assertNotNull(decodeMessage.getSegments());
        assertEquals(ivimContent.getSegments().size(), decodeMessage.getSegments().size());
        for (int i = 0; i < ivimContent.getSegments().size(); i++) {
            Segment seg1 = ivimContent.getSegments().get(i);
            Segment seg2 = decodeMessage.getSegments().get(i);

            // Compare segment positions
            assertEquals(seg1, seg2);

            // Compare lanes
            for (int j = 0; j < seg1.getAdvices().size(); j++) {
                Advice seg1Advice = seg1.getAdvice(j);
                Advice seg2Advice = seg2.getAdvice(j);

                // Compare advices
                assertEquals(seg1Advice, seg2Advice);
            }
        }
    }
}
