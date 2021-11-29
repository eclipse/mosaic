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

import static org.junit.Assert.fail;

import org.eclipse.mosaic.lib.math.Matrix3d;
import org.eclipse.mosaic.lib.math.Vector3d;

import org.junit.Test;

public class RotationMatrixTest {

    @Test
    public void transposeRotationMatrix() {
        final RotationMatrix rotation = new RotationMatrix();
        rotation.rotate(37, new Vector3d(0,1,0));

        RotationMatrix inverse = new RotationMatrix();
        rotation.inverse(inverse);
        RotationMatrix transposed = new RotationMatrix();
        rotation.transpose(transposed);

        assertEquals(inverse, transposed);
    }

    private static void assertEquals(Matrix3d expected, Matrix3d actual) {
        if(!expected.isFuzzyEqual(actual)) {
            fail(String.format("Matrix not fuzzy equal.%nExpected: %s%nActual: %s", expected, actual));
        }
    }
}
