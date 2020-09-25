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

package org.eclipse.mosaic.fed.application.ambassador.util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Test class for the {@link ClassNameParserTest}.
 */
@SuppressWarnings("WeakerAccess")
public class ClassNameParserTestClass {

    final int intParamValue;
    final boolean booleanParamValue;
    final double doubleParamValue;
    final String stringParamValue;

    @SuppressWarnings("unused")
    public ClassNameParserTestClass() {
        this(null, 0, 0d, false);
    }

    public ClassNameParserTestClass(String stringParamValue) {
        this(stringParamValue, 0, 0d, false);
    }

    public ClassNameParserTestClass(int intParamValue) {
        this(null, intParamValue, 0d, false);
    }

    public ClassNameParserTestClass(double doubleParamValue) {
        this(null, 0, doubleParamValue, false);
    }

    public ClassNameParserTestClass(boolean booleanParamValue) {
        this(null, 0, 0d, booleanParamValue);
    }

    public ClassNameParserTestClass(String stringParamValue, int intParamValue, double doubleParamValue, boolean booleanParamValue) {
        this.stringParamValue = stringParamValue;
        this.intParamValue = intParamValue;
        this.doubleParamValue = doubleParamValue;
        this.booleanParamValue = booleanParamValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClassNameParserTestClass that = (ClassNameParserTestClass) o;
        return new EqualsBuilder()
                .append(intParamValue, that.intParamValue)
                .append(booleanParamValue, that.booleanParamValue)
                .append(doubleParamValue, that.doubleParamValue)
                .append(stringParamValue, that.stringParamValue)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(intParamValue)
                .append(booleanParamValue)
                .append(doubleParamValue)
                .append(stringParamValue)
                .toHashCode();
    }
}
