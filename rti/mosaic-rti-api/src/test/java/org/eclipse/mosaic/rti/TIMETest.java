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

package org.eclipse.mosaic.rti;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TIMETest {

    @Test
    public void formatNanosecondAdvanced() {
        assertEquals("0.000,000,000 s", TIME.format(0));
        assertEquals("0.000,000,001 s", TIME.format(TIME.NANO_SECOND));
        assertEquals("0.000,001,000 s", TIME.format(TIME.MICRO_SECOND));
        assertEquals("0.001,000,000 s", TIME.format(TIME.MILLI_SECOND));
        assertEquals("1.000,000,000 s", TIME.format(TIME.SECOND));
        assertEquals("356.000,000,000 s", TIME.format(356 * TIME.SECOND));
        assertEquals("356.010,000,000 s", TIME.format(356 * TIME.SECOND + 10 * TIME.MILLI_SECOND));
        assertEquals("356.000,010,000 s", TIME.format(356 * TIME.SECOND + 10 * TIME.MICRO_SECOND));
        assertEquals("356.000,000,010 s", TIME.format(356 * TIME.SECOND + 10 * TIME.NANO_SECOND));
        assertEquals("3562.000,030,540 s", TIME.format(3562 * TIME.SECOND + 30540 * TIME.NANO_SECOND));
    }

    @Test
    public void testTimeConstants() {
        assertEquals(1, TIME.NANO_SECOND);
        assertEquals(1000 * TIME.NANO_SECOND, TIME.MICRO_SECOND);
        assertEquals(1000 * TIME.MICRO_SECOND, TIME.MILLI_SECOND);
        assertEquals(1000 * TIME.MILLI_SECOND, TIME.SECOND);
    }

}
