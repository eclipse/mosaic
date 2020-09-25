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
import static org.junit.Assert.assertNotNull;

import org.eclipse.mosaic.lib.util.junit.TestFileRule;

import org.apache.commons.configuration2.XMLConfiguration;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

public class XmlUtilsTest {

    @Rule
    public TestFileRule testFileRule = new TestFileRule().with("/test.xml");

    @Test
    public void xpathTest() throws IOException {
        XMLConfiguration xml = XmlUtils.readXmlFromFile(testFileRule.get("test.xml"));

        // note: root tag ("main") is always ignored when reading to XMLConfiguration
        String valueFromXpath = XmlUtils.getValueFromXpath(xml, "/attribute/@value", null);

        assertNotNull(valueFromXpath);
        assertEquals("hello world", valueFromXpath);
    }

}