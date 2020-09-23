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

package org.eclipse.mosaic.fed.mapping.ambassador.weighting;

import static org.eclipse.mosaic.fed.mapping.ambassador.weighting.DeterministicSelectorTest.TestWeighted;
import static org.eclipse.mosaic.fed.mapping.ambassador.weighting.DeterministicSelectorTest.of;

import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class StochasticSelectorTest {

    private RandomNumberGenerator randomNumberGenerator;

    @Before
    public void setup() {
        randomNumberGenerator = new DefaultRandomNumberGenerator(390879289l);
    }

    @Test
    public void stochasticSelection_selectItem80perCentIfWeightIs80perCent() {
        final StochasticSelector<TestWeighted<Boolean>> selector = createSelectorWithTwoBooleanObjects(0.8);

        int numberOfSelectedTrueObjects = selectObjectsAndCount(selector, 10000);

        Assert.assertEquals(numberOfSelectedTrueObjects / 10000d, 0.8d, 0.02d);
    }

    @Test
    public void stochasticSelection_selectNoItemsWithZeroWeight() {
        final StochasticSelector<TestWeighted<Boolean>> selector = createSelectorWithTwoBooleanObjects(0);

        int numberOfSelectedTrueObjects = selectObjectsAndCount(selector, 100);

        Assert.assertEquals(numberOfSelectedTrueObjects, 0);
    }

    @Test
    public void stochasticSelection_selectItemOnlyWithFullWeight() {
        final StochasticSelector<TestWeighted<Boolean>> selector = createSelectorWithTwoBooleanObjects(1);

        int numberOfSelectedTrueObjects = selectObjectsAndCount(selector, 100);

        Assert.assertEquals(numberOfSelectedTrueObjects, 100);
    }

    @Test
    public void stochasticSelection_stochasticSelection() {
        StochasticSelector<TestWeighted<Boolean>> selector = createSelectorWithTwoBooleanObjects(0.8);

        List<Boolean> selectionOrder = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            selectionOrder.add(selector.nextItem().item);
        }

        boolean isEquals = true;

        //make sure that same order of selection is NOT given with a new selector
        selector = createSelectorWithTwoBooleanObjects(0.8);
        for (int i = 0; i < 100; i++) {
            isEquals &= selector.nextItem().item.equals(selectionOrder.get(i));
        }
        Assert.assertFalse(isEquals);
    }

    private int selectObjectsAndCount(final StochasticSelector<TestWeighted<Boolean>> selector, int runs) {
        int numberOfSelectedTrueObjects = 0;
        for (int i = 0; i < runs; i++) {
            if (selector.nextItem().item) {
                numberOfSelectedTrueObjects++;
            }
        }
        return numberOfSelectedTrueObjects;
    }


    private StochasticSelector<TestWeighted<Boolean>> createSelectorWithTwoBooleanObjects(double weightOfTrueObject) {
        ArrayList<TestWeighted<Boolean>> objects = Lists.newArrayList(
                of(true, weightOfTrueObject),
                of(false, 1 - weightOfTrueObject)
        );

        return new StochasticSelector<>(objects, randomNumberGenerator);
    }

}
