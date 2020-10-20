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

package org.eclipse.mosaic.starter.cli;

import org.eclipse.mosaic.lib.util.cli.CommandLineOption;
import org.eclipse.mosaic.lib.util.cli.CommandLineParser;

import java.io.Serializable;

/**
 * This class holds the values of parameters parsed by the {@link CommandLineParser}. Also, all
 * parameters are described here using the {@link CommandLineOption} annotation.
 */
public class MosaicParameters implements Serializable {

    private static final long serialVersionUID = 837650766755740823L;

    @CommandLineOption(shortOption = "c", longOption = "config", argName = "PATH", description = "Path to MOSAIC scenario configuration file (scenario_config.json). Can be used instead of \"-s\" parameter. (mandatory).", group = "config")
    public String configurationPath = null;

    @CommandLineOption(shortOption = "s", longOption = "scenario", argName = "NAME", description = "The name of the MOSAIC scenario. Can be used instead of \"-c\" parameter. (mandatory)", group = "config")
    public String scenarioName = null;

    @CommandLineOption(shortOption = "w", longOption = "watchdog-interval", argName = "SECONDS", description = "Kill MOSAIC process after n seconds if a federate is not responding. 0 disables the watchdog. (default: 30)")
    public String watchdogInterval = null;

    @CommandLineOption(shortOption = "r", longOption = "random-seed", argName = "SEED", description = "Overrides the random seed which is given in the scenario configuration file")
    public Long randomSeed;

    @CommandLineOption(shortOption = "v", longOption = "start-visualizer", description = "Starts the web socket visualizer.")
    public boolean startVisualizer = false;

    @CommandLineOption(shortOption = "b", longOption = "realtime-brake", argName = "REALTIMEFACTOR", description = "Set value for real time brake.")
    public String realtimeBrake = null;

    @CommandLineOption(shortOption = "o", longOption = "log-level", argName = "LOGLEVEL", description = "Overrides the log level to new value (e.g. DEBUG)")
    public String logLevel = null;

    @CommandLineOption(longOption = "runtime", argName = "PATH", description = "Path to MOSAIC RTI configuration file (default: etc/runtime.json)")
    public String runtimeConfiguration = null;

    @CommandLineOption(longOption = "hosts", argName = "PATH", description = "Path to host configuration file (default: etc/hosts.json)")
    public String hostsConfiguration = null;

    @CommandLineOption(shortOption = "l", longOption = "logger", argName = "PATH", description = "Path to logback configuration file (default: etc/logback.xml)")
    public String loggerConfiguration = null;

    @CommandLineOption(shortOption = "e", longOption = "external-watchdog", argName = "PORTNUMBER", description = "Specific external watchdog port number")
    public String externalWatchDog = null;

}

