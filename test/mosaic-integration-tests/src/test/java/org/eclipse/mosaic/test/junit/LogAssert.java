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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of static methods that allow to the assertion of entries in a generated log file.
 */
public class LogAssert {

    /**
     * Fails if pattern isn't contained within the defined {@code logFile}.
     *
     * @param rule       the simulation rule used for the test
     * @param logFile    the log file to search in
     * @param logPattern the pattern to search for
     * @throws Exception thrown if parsing of log file fails
     */
    public static void contains(MosaicSimulationRule rule, String logFile, String logPattern) throws Exception {
        exists(rule, logFile);

        boolean found = Files.lines(rule.getLogDirectory().resolve(logFile), Charsets.UTF_8).anyMatch(line -> line.matches(logPattern));
        if (!found) {
            fail("Could not find pattern \"" + logPattern + "\" in file " + logFile);
        }
    }

    public static int count(MosaicSimulationRule rule, String logFile, String logPattern) throws Exception {
        exists(rule, logFile);

        return (int) Files.lines(rule.getLogDirectory().resolve(logFile), Charsets.UTF_8).filter(line -> line.matches(logPattern)).count();
    }

    public static List<String> getMatches(MosaicSimulationRule rule, String logFile, int captureGroup, String logPattern) throws Exception {
        exists(rule, logFile);
        Pattern pattern = Pattern.compile(logPattern);
        List<String> matches = new ArrayList<>();
        Files.lines(rule.getLogDirectory().resolve(logFile), Charsets.UTF_8).forEach(line -> {
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                matches.add(matcher.group(captureGroup));
            }
        });
        return matches;
    }

    public static void exists(MosaicSimulationRule rule, String logFile) {
        if (!Files.exists(rule.getLogDirectory().resolve(logFile))) {
            fail("Log file \"" + logFile + "\" could not be found.");
        }
    }
}
