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

package org.eclipse.mosaic.fed.sumo.traci.junit;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assume.assumeTrue;

import org.eclipse.mosaic.fed.sumo.config.CSumo;
import org.eclipse.mosaic.fed.sumo.traci.CommandRegister;
import org.eclipse.mosaic.fed.sumo.traci.SumoVersion;
import org.eclipse.mosaic.fed.sumo.traci.TraciClient;
import org.eclipse.mosaic.fed.sumo.traci.TraciConnection;
import org.eclipse.mosaic.lib.util.ProcessLoggingThread;
import org.eclipse.mosaic.lib.util.SocketUtils;
import org.eclipse.mosaic.rti.api.federatestarter.ExecutableFederateExecutor;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SumoTraciRule implements TestRule {
    // set this value to true to open the simulation with SUMO-GUI, you can also set the Environment variable GUI_DEBUG to true
    private final static boolean GUI_DEBUG = Boolean.parseBoolean(StringUtils.defaultIfBlank(System.getenv("MOSAIC_SUMO_GUI_DEBUG"), "false"));

    private static final Pattern PORT_PATTERN = Pattern.compile(".*Starting server on port ([0-9]+).*");

    private static final SumoVersion MINIMUM_VERSION_TESTED = SumoVersion.SUMO_1_0_x;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final File scenarioConfig;
    private final CSumo sumoConfig;

    private TraciClient traci;
    private Process sumoProcess;

    private ProcessLoggingThread outputLoggingThread;

    private ProcessLoggingThread errorLoggingThread;

    public SumoTraciRule(final File scenarioConfig) {
        this(scenarioConfig, new CSumo());
    }

    public SumoTraciRule(final File scenarioConfig, final CSumo sumoConfig) {
        this.scenarioConfig = scenarioConfig;
        this.sumoConfig = sumoConfig;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    SinceTraci sinceTraci = description.getAnnotation(SinceTraci.class);
                    if (sinceTraci != null) {
                        SumoVersion currentVersion = SumoTraciRule.this.traci.getCurrentVersion();
                        assumeTrue(
                                String.format(
                                        "This unit test expects SUMO version %s to be installed. Skipping.",
                                        sinceTraci.value()
                                ),
                                currentVersion.getApiVersion() >= sinceTraci.value().getApiVersion()
                        );
                    }

                    SinceSumo sinceSumo = description.getAnnotation(SinceSumo.class);
                    if (sinceSumo != null) {
                        SumoVersion currentVersion = SumoTraciRule.this.traci.getCurrentVersion();
                        assumeTrue(
                                String.format(
                                        "This unit test expects SUMO version %s to be installed. Skipping.",
                                        sinceSumo.value()
                                ),
                                currentVersion.isGreaterOrEqualThan(sinceSumo.value())
                        );
                    }
                    base.evaluate();
                } finally {
                    after();
                }
            }
        };
    }

    private void before() throws Throwable {
        TraciClient.VEHICLE_ID_TRANSFORMER.reset();
        final String sumoCmd;
        if (GUI_DEBUG) {
            sumoCmd = "sumo-gui";
        } else {
            sumoCmd = "sumo";
        }

        int port = SocketUtils.findFreePort();
        // startup sumo
        final List<String> startArgs = Lists.newArrayList("-c", scenarioConfig.getName(),
                "-v", "--remote-port", Integer.toString(port),
                "--step-length", String.format(Locale.ENGLISH, "%.2f", (double) sumoConfig.updateInterval / 1000d));
        startArgs.addAll(Arrays.asList(StringUtils.split(sumoConfig.additionalSumoParameters.trim(), " ")));

        sumoProcess = new ExecutableFederateExecutor(
                null,
                getSumoExecutable(sumoCmd), startArgs).startLocalFederate(scenarioConfig.getParentFile()
        );

        if (!GUI_DEBUG) {
            // make sure to read until server is started, otherwise test will fail!
            final BufferedReader sumoInputReader =
                    new BufferedReader(new InputStreamReader(sumoProcess.getInputStream(), StandardCharsets.UTF_8));
            String line;
            Matcher matcher;
            while (((line = sumoInputReader.readLine()) != null)) {
                matcher = PORT_PATTERN.matcher(line);
                if (matcher.find()) {
                    port = Integer.parseInt(matcher.group(1));
                    break;
                }
            }
        }
        assertNotEquals(0, port);

        redirectOutputToLog(); // this is necessary, otherwise TraCI will hang due to full output buffer

        log.info("Connect to SUMO on port {}", port);
        final Socket socket = new Socket("localhost", port);
        socket.setPerformancePreferences(0, 100, 10);
        socket.setTcpNoDelay(true);

        final CommandRegister commandRegister = new CommandRegister();
        this.traci = new TraciClient(sumoConfig, socket, commandRegister);

        assumeTrue(String.format("Unit-Tests expect SUMO version %s to be installed", MINIMUM_VERSION_TESTED.getSumoVersion()),
                this.traci.getCurrentVersion().getApiVersion() >= MINIMUM_VERSION_TESTED.getApiVersion());
    }

    private String getSumoExecutable(String executable) {
        String sumoHome = System.getenv("SUMO_HOME");
        if (StringUtils.isNotBlank(sumoHome)) {
            return sumoHome + File.separator + "bin" + File.separator + executable;
        }
        return executable;
    }

    private void redirectOutputToLog() {
        log.info("Start logging threads");
        outputLoggingThread = new ProcessLoggingThread(log, sumoProcess.getInputStream(), "SumoAmbassador", ProcessLoggingThread.Level.Debug);
        outputLoggingThread.start();

        errorLoggingThread = new ProcessLoggingThread(log, sumoProcess.getErrorStream(), "SumoAmbassador", ProcessLoggingThread.Level.Error);
        errorLoggingThread.start();
    }

    public final TraciClient getTraciClient() {
        return traci;
    }

    private void after() {
        TraciClient.VEHICLE_ID_TRANSFORMER.reset();
        try {
            log.info("Close Traci Connection");
            traci.close();
        } catch (Exception e) {
            log.error("Could not close TraCI connection properly", e);
        }

        try {
            log.info("Close SUMO process");
            sumoProcess.waitFor();
            sumoProcess.destroyForcibly().waitFor();
            sumoProcess = null;
        } catch (Exception e) {
            log.error("Could not SUMO process", e);
        }

        try {
            log.info("Close Output Logging Thread");
            outputLoggingThread.close();
            outputLoggingThread = null;
        } catch (Exception e) {
            log.error("Could not close Logging Thread", e);
        }

        try {
            log.info("Close Error Logging Thread");
            errorLoggingThread.close();
            errorLoggingThread = null;
        } catch (Exception e) {
            log.error("Could not close Logging Thread", e);
        }

        try {
            Thread.sleep(500);
        } catch (Exception e) {
            //
        }

    }

    public TraciConnection getTraciConnection() {
        return traci;
    }
}
