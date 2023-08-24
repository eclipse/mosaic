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

package org.eclipse.mosaic.fed.mapping.ambassador.weighting;

import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FixedOrderSelectorTest {

    private RandomNumberGenerator rng;

    @Before
    public void setup() {
        rng = new DefaultRandomNumberGenerator(1098989123L);
    }

    @Test
    public void fixedorderSelection_selectItem80TimesIfWeightIs80perCent() {
        final FixedOrderSelector<TestWeighted<Boolean>> selector = createSelectorWithTwoBooleanObjects(0.8);

        int numberOfSelectedTrueObjects = selectObjectsAndCount(selector, 100);

        assertEquals(numberOfSelectedTrueObjects, 80, 0d);
    }

    @Test
    public void fixedorderSelection_selectNoItemsWithZeroWeight() {
        final FixedOrderSelector<TestWeighted<Boolean>> selector = createSelectorWithTwoBooleanObjects(0);

        int numberOfSelectedTrueObjects = selectObjectsAndCount(selector, 100);

        assertEquals(numberOfSelectedTrueObjects, 0, 0d);
    }

    @Test
    public void fixedorderSelection_selectItemOnlyWithFullWeight() {
        final FixedOrderSelector<TestWeighted<Boolean>> selector = createSelectorWithTwoBooleanObjects(1);

        int numberOfSelectedTrueObjects = selectObjectsAndCount(selector, 100);

        assertEquals(numberOfSelectedTrueObjects, 100, 0d);
    }

    @Test
    public void fixedorderSelection_fixedorderBehavior() {
        FixedOrderSelector<TestWeighted<Boolean>> selector = createSelectorWithTwoBooleanObjects(0.8);

        List<Boolean> selectionOrder = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            selectionOrder.add(selector.nextItem().item);
        }

        // make sure that same order of selection is given with a new selector
        selector = createSelectorWithTwoBooleanObjects(0.8);
        for (int i = 0; i < 100; i++) {
            assertEquals(selector.nextItem().item, selectionOrder.get(i));
        }
    }

    private int selectObjectsAndCount(final FixedOrderSelector<TestWeighted<Boolean>> selector, int runs) {
        int numberOfSelectedTrueObjects = 0;
        for (int i = 0; i < runs; i++) {
            if (selector.nextItem().item) {
                numberOfSelectedTrueObjects++;
            }
        }
        return numberOfSelectedTrueObjects;
    }


    private FixedOrderSelector<TestWeighted<Boolean>> createSelectorWithTwoBooleanObjects(double weightOfTrueObject) {
        List<TestWeighted<Boolean>> objects = Lists.newArrayList(
                of(true, weightOfTrueObject),
                of(false, 1 - weightOfTrueObject)
        );
        return new FixedOrderSelector<>(objects, rng);
    }

    @Test
    public void fixedorderSelection_complexConfigurationWithStartIndex() {
        List<TestWeighted<?>> values = Lists.newArrayList(
                of("A", 0.1),
                of("B", 0.5),
                of("C", 0.35),
                of("D", 0.05)
        );
        final FixedOrderSelector<TestWeighted<?>> selector = new FixedOrderSelector<>(values, rng);

        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            s.append(selector.nextItem().item);
        }

        assertEquals("BCABCBDBCBCBACBBCBCBBCABCBDBCBCBACBBCBCBBCABCBDBCB", s.toString());
    }


    private String getSequenceAsString(int seed) {
        RandomNumberGenerator _rng = new DefaultRandomNumberGenerator(seed);

        List<TestWeighted<?>> values = Lists.newArrayList(
                of("A", 0.1),
                of("B", 0.1),
                of("-", 0.8)
        );

        final FixedOrderSelector<TestWeighted<?>> selector = new FixedOrderSelector<>(values, _rng);

        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            s.append(selector.nextItem().item);
        }

        return s.toString();
    }

    /**
     * Tests if different starting values (determined by rng seed) result in different periodic sequences.
     */
    @Test
    public void fixedorderSelection_startValueDeterminesPeriodicSequence() {
        assertEquals(getSequenceAsString(4096), "A---B-----A---B-----");
        assertEquals(getSequenceAsString(4286), "B---A-----B---A-----");
        assertEquals(getSequenceAsString(0), "-A--B------A--B-----");

    }

    static <O> TestWeighted<O> of(O o, double weight) {
        TestWeighted<O> item = new TestWeighted<O>();
        item.item = o;
        item.weight = weight;
        return item;
    }

    static class TestWeighted<T> implements Weighted {
        T item;
        private double weight;

        @Override
        public double getWeight() {
            return weight;
        }
    }

}
