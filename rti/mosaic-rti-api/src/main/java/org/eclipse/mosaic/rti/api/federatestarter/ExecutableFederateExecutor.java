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

package org.eclipse.mosaic.rti.api.federatestarter;

import org.eclipse.mosaic.rti.api.FederateExecutor;
import org.eclipse.mosaic.rti.api.parameters.FederateDescriptor;
import org.eclipse.mosaic.rti.config.CLocalHost;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A {@link FederateExecutor} implementation which starts a federate (e.g. executable) by calling a command.
 */
public class ExecutableFederateExecutor implements FederateExecutor {

    private static Logger log = LoggerFactory.getLogger(ExecutableFederateExecutor.class);

    private final FederateDescriptor descriptor;
    private final String command;
    private final List<String> args;

    protected Process currentLocalProcess = null;
    protected int currentRemoteProcessId = -1;

    public ExecutableFederateExecutor(FederateDescriptor descriptor, String command, String... args) {
        this(descriptor, command, Arrays.asList(args));
    }

    public ExecutableFederateExecutor(FederateDescriptor descriptor, String command, List<String> args) {
        this.descriptor = descriptor;
        this.command = command;
        this.args = args;
    }

    @Override
    public Process startLocalFederate(File workingDir) throws FederateStarterException {
        log.debug("start: (command={} {})", command, StringUtils.join(args, " "));
        try {
            List<String> commandWithArgs = Lists.newArrayList(command);
            commandWithArgs.addAll(args);
            currentLocalProcess = new ProcessBuilder(commandWithArgs).directory(workingDir).start();
            return currentLocalProcess;
        } catch (IOException e) {
            throw new FederateStarterException(e);
        }
    }

    @Override
    public void stopLocalFederate() {
        if (currentLocalProcess != null) {
            try {
                currentLocalProcess.waitFor(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("Something went wrong when stopping a process", e);
            } finally {
                currentLocalProcess.destroy();
            }
        }
    }

    @Override
    public int startRemoteFederate(CLocalHost host, PrintStream sshStreamOut, InputStream sshStreamIn) throws FederateStarterException {
        sshStreamOut.println("cd " + host.workingDirectory + "/" + descriptor.getId());

        sshStreamOut.println("nohup " + command + " " + StringUtils.join(args, " ") + " &");
        try {
            currentRemoteProcessId = this.readProcessId(sshStreamIn);

            sshStreamOut.println("tail -f nohup.out");
            return currentRemoteProcessId;
        } catch (IOException e) {
            throw new FederateStarterException(e);
        }
    }

    @Override
    public void stopRemoteFederate(PrintStream sshStreamOut) {
        if (currentRemoteProcessId >= 0) {
            sshStreamOut.println("kill " + currentRemoteProcessId);
        }
        currentRemoteProcessId = -1;
    }

    /**
     * Reads the process id after a process is started on a remote host.
     *
     * @param in input stream connected to the remote host
     * @return process id
     */
    private int readProcessId(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line;
        int pid = -1;
        while ((line = reader.readLine()) != null) {
            if (line.length() > 0) {
                log.debug(line);
                if (line.matches("\\s*\\[\\d+\\]\\s\\d+\\s*")) {
                    pid = Integer.parseInt(line.split("\\s")[1]);
                    log.info("Started with pid=" + pid + ".");
                    break;
                }
            }
        }
        return pid;
    }

    @Override
    public String toString() {
        return "Command Executor [start command: " + command + " " + StringUtils.join(args, " ") + "]";
    }
}
