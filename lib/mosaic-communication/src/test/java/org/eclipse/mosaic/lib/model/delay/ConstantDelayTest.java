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
 */

package org.eclipse.mosaic.lib.model.delay;

import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;

import org.junit.Assert;
import org.junit.Test;

public class ConstantDelayTest {
    private final ConstantDelay constantDelay = new ConstantDelay();

    private final RandomNumberGenerator randomNumberGenerator = new DefaultRandomNumberGenerator(0);

    @Test
    public void constantDelay_0() {
        //SETUP
        constantDelay.delay = 0;

        //RUN
        long delayInNs = constantDelay.generateDelay(randomNumberGenerator, -1);

        //ASSERT
        Assert.assertEquals(0, delayInNs);
    }

    @Test
    public void constantDelay_20() {
        //SETUP
        constantDelay.delay = 20;

        //RUN
        long delayInNs = constantDelay.generateDelay(randomNumberGenerator, -1);

        //ASSERT
        Assert.assertEquals(20, delayInNs);
    }

}
