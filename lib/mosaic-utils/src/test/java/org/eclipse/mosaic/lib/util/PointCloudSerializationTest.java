/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.math.VectorUtils;
import org.eclipse.mosaic.lib.spatial.PointCloud;
import org.eclipse.mosaic.lib.spatial.RotationMatrix;
import org.eclipse.mosaic.rti.TIME;

import org.junit.Test;

import java.util.List;

public class PointCloudSerializationTest {

    @Test
    public void serialization() {
        PointCloud.Point p1 = new PointCloud.Point(new Vector3d(4.0, 5.0, 0.0), 0f, (byte) 0);
        PointCloud.Point p2 = new PointCloud.Point(new Vector3d(-1.0, 2.0, 4.0), 0f, (byte) 1);

        PointCloud expected = new PointCloud(4 * TIME.SECOND,
                new Vector3d(3, 1, 0), new RotationMatrix().rotate(90, VectorUtils.UP),
                List.of(p1, p2), PointCloud.PointReference.RELATIVE
        );

        //RUN
        byte[] result = PointCloudSerialization.toByteArray(expected);
        PointCloud actual = PointCloudSerialization.fromByteArray(result);

        //ASSERT
        assertNotNull(actual);
        assertEquals(expected.getCreationTime(), actual.getCreationTime());
        assertTrue(expected.getOrigin().isFuzzyEqual(actual.getOrigin()));
        assertTrue(expected.getOrientation().isFuzzyEqual(actual.getOrientation()));
        assertEquals(expected.getAbsoluteEndPoints().size(), actual.getAbsoluteEndPoints().size());
        assertEquals(expected.getRelativeEndPoints().size(), actual.getRelativeEndPoints().size());
        assertEquals(expected.getRelativeEndPointsWithHit().size(), actual.getRelativeEndPointsWithHit().size());
        for (int i = 0; i < actual.getRelativeEndPoints().size(); i++) {
            assertTrue(expected.getRelativeEndPoints().get(i).isFuzzyEqual(actual.getRelativeEndPoints().get(i)));
        }
    }
}
