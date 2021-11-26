/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import static org.junit.Assert.fail;

import org.eclipse.mosaic.lib.math.Matrix3d;
import org.eclipse.mosaic.lib.math.Vector3d;

import org.junit.Assert;
import org.junit.Test;

public class TransformationMatrixTest {

    @Test
    public void translate() {
        TransformationMatrix transformation = new TransformationMatrix();
        transformation.translate(1, 2, 3);

        Vector3d x = new Vector3d(0, 0, 0);
        transformation.transform(x);
        Assert.assertEquals(1.0, x.x, 0.00001);
        Assert.assertEquals(2.0, x.y, 0.00001);
        Assert.assertEquals(3.0, x.z, 0.00001);

        Assert.assertEquals(1.0, transformation.get(0, 3), 0.00001);
        Assert.assertEquals(2.0, transformation.get(1, 3), 0.00001);
        Assert.assertEquals(3.0, transformation.get(2, 3), 0.00001);
    }

    @Test
    public void rotate() {
        final RotationMatrix expected = new RotationMatrix()
                .rotate(37, new Vector3d(1,0,0));

        final TransformationMatrix transformationMatrix = new TransformationMatrix();
        transformationMatrix
                .rotate(45, new Vector3d(1,0,0))
                .rotate(90, new Vector3d(0,1,0))
                .rotate(270, new Vector3d(0,1,0))
                .rotate(-8, new Vector3d(1,0,0));


        final RotationMatrix actual = transformationMatrix.getRotation();
        assertEquals(expected, actual);
        Assert.assertEquals(1, actual.get(0, 0), 0.0001);
        Assert.assertEquals(sin(toRadians(37)), actual.get(2, 1), 0.0001);
        Assert.assertEquals(-sin(toRadians(37)), actual.get(1, 2), 0.0001);
        Assert.assertEquals(cos(toRadians(37)), actual.get(1, 1), 0.0001);
        Assert.assertEquals(cos(toRadians(37)), actual.get(2, 2), 0.0001);
    }

    private static void assertEquals(Matrix3d expected, Matrix3d actual) {
        if(!expected.isFuzzyEqual(actual)) {
            fail(String.format("Matrix not fuzzy equal.%nExpected: %s%nActual: %s", expected, actual));
        }
    }

}
