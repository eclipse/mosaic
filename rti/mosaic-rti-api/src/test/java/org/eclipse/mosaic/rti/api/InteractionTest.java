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

package org.eclipse.mosaic.rti.api;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

/**
 * Test for the {@link Interaction} class.
 */
public class InteractionTest {

    @Test
    public void getTypeId() {
        assertEquals("TestInteraction", new TestInteraction().getTypeId());
        assertEquals("InteractionTest.InlineInteraction", new InlineInteraction().getTypeId());
    }

    @Test
    public void compareTo() {
        InlineInteraction a = new InlineInteraction();
        InlineInteraction b = new InlineInteraction();
        InlineInteraction c = new InlineInteraction(a.getTime() - 10, a.getId());
        InlineInteraction d = new InlineInteraction(a.getTime() - 10, b.getId());

        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(1, b.compareTo(a));

        assertEquals(0, c.compareTo(c));
        assertEquals(-1, c.compareTo(d));
        assertEquals(1, d.compareTo(c));

        assertEquals(1, a.compareTo(c));
        assertEquals(-1, c.compareTo(a));
    }

    private static class InlineInteraction extends Interaction {

        private static final long serialVersionUID = 1L;

        private InlineInteraction() {
            super(0);
        }

        private InlineInteraction(long time, int id) {
            super(time, id);
        }
    }

}