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

package org.eclipse.mosaic.test.junit;

import static org.junit.Assert.fail;

import com.google.common.base.Charsets;

import java.nio.file.Files;

public class LogAssert {

    public static void contains(MosaicSimulationRule rule, String logFile, String logPattern) throws Exception {
        exists(rule, logFile);

        boolean found = Files.lines(rule.getLogDirectory().resolve(logFile), Charsets.ISO_8859_1).anyMatch(
                line -> line.matches(logPattern)
        );
        if (!found) {
            fail("Could not find pattern \"" + logPattern + "\" in file " + logFile);
        }
    }

    public static int count(MosaicSimulationRule rule, String logFile, String logPattern) throws Exception {
        exists(rule, logFile);

        return (int) Files.lines(rule.getLogDirectory().resolve(logFile), Charsets.ISO_8859_1).filter(
                line -> line.matches(logPattern)
        ).count();
    }

    public static void exists(MosaicSimulationRule rule, String logFile) {
        if (!Files.exists(rule.getLogDirectory().resolve(logFile))) {
            fail("Log file \"" + logFile + "\" could not be found.");
        }
    }
}
