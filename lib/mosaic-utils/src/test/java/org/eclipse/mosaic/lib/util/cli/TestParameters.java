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

package org.eclipse.mosaic.lib.util.cli;

import java.io.Serializable;

public class TestParameters implements Serializable {
    private static final long serialVersionUID = 1L;

    @CommandLineOption(shortOption = "c", longOption = "config", argName = "PATH", description = "...", group = "config")
    public String configurationPath = null;

    @CommandLineOption(shortOption = "s", longOption = "scenario", argName = "NAME", description = "...", group = "config")
    public String scenarioName = null;

    @CommandLineOption(shortOption = "w", longOption = "watchdog-interval", argName = "SECONDS", description = "...")
    public String watchdoginterval = null;

    @CommandLineOption(shortOption = "v", longOption = "start-visualizer", description = "...")
    public boolean startVisualizer = false;

    @CommandLineOption(shortOption = "u", longOption = "user", argName = "USERID", description = "...", isRequired = true)
    public String userid = null;

}