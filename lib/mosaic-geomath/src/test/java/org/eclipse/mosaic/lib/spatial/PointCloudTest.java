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

package org.eclipse.mosaic.lib.spatial;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.math.VectorUtils;

import org.hamcrest.core.IsEqual;
import org.junit.Test;

import java.util.List;

public class PointCloudTest {

    @Test
    public void relativeToAbsolute() {
        // SETUP
        PointCloud.Point p1 = new PointCloud.Point(new Vector3d(4.0, 5.0, 0.0), 0f, (byte) 0);
        PointCloud.Point p2 = new PointCloud.Point(new Vector3d(-1.0, 2.0, 4.0), 0f, (byte) 1);

        PointCloud pc = new PointCloud(0,
                new Vector3d(3, 1, 0), new RotationMatrix().rotate(90, VectorUtils.UP),
                List.of(p1, p2), PointCloud.PointReference.RELATIVE
        );

        // RUN
        List<PointCloud.Point> absolutePoints = pc.getAbsoluteEndPointsWithHit();

        // ASSERT
        PointCloud.Point p2absolute = new PointCloud.Point(new Vector3d(7, 3, 1.0), 0f, (byte) 1);
        assertThat(absolutePoints.size(), is(1));
        assertThat(absolutePoints.get(0), is(fuzzyEqualTo(p2absolute)));
    }

    @Test
    public void absoluteToRelative() {
        // SETUP
        PointCloud.Point p1 = new PointCloud.Point(new Vector3d(4.0, 5.0, 0.0), 0f, (byte) 0);
        PointCloud.Point p2 = new PointCloud.Point(new Vector3d(-1.0, 2.0, 4.0), 0f, (byte) 1);

        PointCloud pc = new PointCloud(0,
                new Vector3d(3, 1, 0), new RotationMatrix().rotate(90, VectorUtils.UP),
                List.of(p1, p2), PointCloud.PointReference.ABSOLUTE
        );

        // RUN
        PointCloud.Point p2relative = new PointCloud.Point(new Vector3d(-4, 1, -4), 0f, (byte) 1);

        // ASSERT
        List<PointCloud.Point> relativePoints = pc.getRelativeEndPointsWithHit();
        assertThat(relativePoints.size(), is(1));
        assertThat(relativePoints.get(0), is(fuzzyEqualTo(p2relative)));
    }

    private static <T extends Vector3d> IsEqual<T> fuzzyEqualTo(T base) {
        return new IsEqual<>(base) {
            @Override
            public boolean matches(Object actualValue) {
                if (actualValue instanceof Vector3d) {
                    return base.isFuzzyEqual((Vector3d) actualValue);
                }
                return super.matches(actualValue);
            }
        };
    }


}
