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

}
