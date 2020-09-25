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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;

/**
 * Test suite for {@link ClassNameParser}.
 */
public class ClassNameParserTest {

    private final String baseClassName = ClassNameParserTestClass.class.getCanonicalName();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Logger logger;


    @Test(expected = RuntimeException.class)
    public void fullQualifiedName_notAssignable() {
        new ClassNameParser(logger).createInstanceFromClassName(baseClassName, String.class);
    }

    @Test
    public void fullQualifiedName_assignableFromSameClass() {
        final ClassNameParserTestClass o = new ClassNameParser(logger).createInstanceFromClassName(baseClassName, ClassNameParserTestClass.class);
        assertNotNull(o);
    }

    @Test
    public void fullQualifiedName_assignableFromObject() {
        final Object o = new ClassNameParser(logger).createInstanceFromClassName(baseClassName, Object.class);

        assertNotNull(o);
        assertTrue(o instanceof ClassNameParserTestClass);
    }

    @Test
    public void fullQualifiedName() {
        final Object o = new ClassNameParser(logger).createInstanceFromClassName(baseClassName);

        assertNotNull(o);
        assertTrue(o instanceof ClassNameParserTestClass);
    }

    @Test(expected = RuntimeException.class)
    public void fullQualifiedName_wrongConstructor() {
        new ClassNameParser(logger).createInstanceFromClassName(baseClassName + "(\"value\", true)");
    }

    @Test
    public void fullQualifiedName_oneStringParam() {
        final Object o = new ClassNameParser(logger).createInstanceFromClassName(baseClassName + "(\"value\")");

        assertNotNull(o);
        assertTrue(o instanceof ClassNameParserTestClass);
        assertEquals(new ClassNameParserTestClass("value"), o);
    }

    @Test
    public void fullQualifiedName_oneStringParamWithSingleQuotes() {
        final Object o = new ClassNameParser(logger).createInstanceFromClassName(baseClassName + "('value')");

        assertNotNull(o);
        assertTrue(o instanceof ClassNameParserTestClass);
        assertEquals(new ClassNameParserTestClass("value"), o);
    }

    @Test
    public void fullQualifiedName_oneIntParam() {
        final Object o = new ClassNameParser(logger).createInstanceFromClassName(baseClassName + "(42)");

        assertNotNull(o);
        assertTrue(o instanceof ClassNameParserTestClass);
        assertEquals(new ClassNameParserTestClass(42), o);
    }

    @Test
    public void fullQualifiedName_oneDoubleParam() {
        final Object o = new ClassNameParser(logger).createInstanceFromClassName(baseClassName + "(4.2)");

        assertNotNull(o);
        assertTrue(o instanceof ClassNameParserTestClass);
        assertEquals(new ClassNameParserTestClass(4.2), o);
    }

    @Test
    public void fullQualifiedName_oneBooleanParam() {
        final Object o = new ClassNameParser(logger).createInstanceFromClassName(baseClassName + "(true)");

        assertNotNull(o);
        assertTrue(o instanceof ClassNameParserTestClass);
        assertEquals(new ClassNameParserTestClass(true), o);
    }

    @Test
    public void fullQualifiedName_parameterList() {
        final Object o = new ClassNameParser(logger).createInstanceFromClassName(baseClassName + "( \"value\", 42, 4.2, true )");

        assertNotNull(o);
        assertTrue(o instanceof ClassNameParserTestClass);
        assertEquals(new ClassNameParserTestClass("value", 42, 4.2, true), o);
    }

}