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

package org.eclipse.mosaic.lib.objects;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class UnitNameComparatorTest {

    @Test
    public void correctOrder() {

        //SETUP
        final UnitNameComparator comparator = new UnitNameComparator();

        final UnitData rsu0 = new TestUnitData("rsu_0");
        final UnitData rsu10 = new TestUnitData("rsu_10");
        final UnitData rsu105 = new TestUnitData("rsu_105");
        final UnitData veh20 = new TestUnitData("veh_20");
        final UnitData veh2 = new TestUnitData("veh_2");
        final UnitData veh0 = new TestUnitData("veh_0");


        // RUN
        List<UnitData> orderedList = Arrays.asList(rsu0, rsu105, veh20, veh0, rsu10, veh20, veh2);
        orderedList.sort(comparator);

        // ASSERT
        assertArrayEquals(new UnitData[]{
                rsu0, rsu10, rsu105, veh0, veh2, veh20, veh20
        }, orderedList.toArray(new UnitData[0]));
    }

    private static class TestUnitData extends UnitData {

        private TestUnitData(String name) {
            super(0, name, null);
        }
    }

}