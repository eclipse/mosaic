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

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FixedOrderSelectorTest {

    @Test
    public void fixedOrderSelection_selectItem80TimesIfWeightIs80perCent() {
        final FixedOrderSelector<TestWeighted<Boolean>> selector = createSelectorWithTwoBooleanObjects(0.8);

        int numberOfSelectedTrueObjects = selectObjectsAndCount(selector, 100);

        assertEquals(numberOfSelectedTrueObjects, 80, 0d);
    }

    @Test
    public void fixedOrderSelection_selectNoItemsWithZeroWeight() {
        final FixedOrderSelector<TestWeighted<Boolean>> selector = createSelectorWithTwoBooleanObjects(0);

        int numberOfSelectedTrueObjects = selectObjectsAndCount(selector, 100);

        assertEquals(numberOfSelectedTrueObjects, 0, 0d);
    }

    @Test
    public void fixedOrderSelection_selectItemOnlyWithFullWeight() {
        final FixedOrderSelector<TestWeighted<Boolean>> selector = createSelectorWithTwoBooleanObjects(1);

        int numberOfSelectedTrueObjects = selectObjectsAndCount(selector, 100);

        assertEquals(numberOfSelectedTrueObjects, 100, 0d);
    }

    @Test
    public void fixedOrderSelection_fixedOrderBehavior() {
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
        return new FixedOrderSelector<>(objects);
    }

    @Test
    public void fixedOrderSelection_differentWeights_sequenceInWeightOrder() {
        List<TestWeighted<?>> values = Lists.newArrayList(
                of("A", 0.1),
                of("B", 0.5),
                of("C", 0.35),
                of("D", 0.05)
        );
        assertEquals("BCABCBDBCBCBACBBCBCBBCABCBDBCBCBACBBCBCBBCABCBDBCB", selectViaFixOrderAsString(values, 50));
    }

    @Test
    public void fixedOrderSelection_equalWeights_sequenceInDefinedOrder() {
        List<TestWeighted<?>> values = Lists.newArrayList(
                of("A", 1),
                of("B", 1),
                of("C", 1),
                of("-", 1)
        );
        assertEquals("ABC-ABC-ABC-ABC-ABC-", selectViaFixOrderAsString(values));
    }

    @Test
    public void fixedOrderSelection_equalAndDifferentWeights_sequenceInDefinedOrderThenByWeight() {
        List<TestWeighted<?>> values = Lists.newArrayList(
                of("A", 0.4),
                of("B", 0.4),
                of("-", 0.2)
        );
        assertEquals("AB-ABAB-ABAB-ABAB-AB", selectViaFixOrderAsString(values));
    }

    private String selectViaFixOrderAsString(List<TestWeighted<?>> values) {
        return selectViaFixOrderAsString(values, 20);
    }

    private String selectViaFixOrderAsString(List<TestWeighted<?>> values, int selections) {

        final FixedOrderSelector<TestWeighted<?>> selector = new FixedOrderSelector<>(values);

        StringBuilder s = new StringBuilder();
        for (int i = 0; i < selections; i++) {
            s.append(selector.nextItem().item);
        }
        return s.toString();
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
