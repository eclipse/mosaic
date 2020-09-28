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

package org.eclipse.mosaic.lib.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class RingBufferTest {

    @Test(expected = IllegalArgumentException.class)
    public void getMoreThanAdded_Exception() {
        RingBuffer<Integer> r = new RingBuffer<>(5);

        r.add(0);
        r.add(1);

        assertEquals(0, r.get(0).intValue());
        assertEquals(1, r.get(1).intValue());

        r.get(2); //exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMoreThanCapacityAllows_Exception() {
        RingBuffer<Integer> r = new RingBuffer<>(5);

        //RUN
        for (int i = 0; i < 10; i++) {
            r.add(i);
        }
        r.get(8); //exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOnEmptyBuffer_Exception() {
        RingBuffer<Integer> r = new RingBuffer<>(5);

        //RUN
        r.get(0); //exception
    }

    @Test
    public void iterateOverEmptyBuffer() {
        RingBuffer<Integer> r = new RingBuffer<>(5);

        for (Integer i : r) {
            fail("Should not iterate");
        }
    }

    @Test
    public void iterateOverFilledBuffer() {
        RingBuffer<Integer> r = new RingBuffer<>(50);

        for (int i = 0; i < 10; i++) {
            r.add(i);
        }

        int expected = 0;
        for (Integer i : r) {
            assertEquals(expected++, i.intValue());
        }
        assertEquals(10, expected);
    }

    @Test
    public void iterateOverFullBuffer() {
        RingBuffer<Integer> r = new RingBuffer<>(5);

        for (int i = 0; i < 10; i++) {
            r.add(i);
        }

        int expected = 5;
        for (Integer i : r) {
            assertEquals(expected++, i.intValue());
        }
        assertEquals(10, expected);
    }

    @Test
    public void addLessThanCapacity() {
        RingBuffer<Integer> r = new RingBuffer<>(15);

        //RUN
        for (int i = 0; i < 10; i++) {
            r.add(i);
        }

        //ASSERT
        for (int i = 0; i < 10; i++) {
            assertEquals(i, r.get(i).intValue());
        }
    }

    @Test
    public void addMoreThanCapacity() {
        RingBuffer<Integer> r = new RingBuffer<>(10);

        //RUN
        for (int i = 0; i < 20; i++) {
            r.add(i);
        }

        //ASSERT
        for (int i = 0; i < 10; i++) {
            assertEquals(i + 10, r.get(i).intValue());
        }
    }

}