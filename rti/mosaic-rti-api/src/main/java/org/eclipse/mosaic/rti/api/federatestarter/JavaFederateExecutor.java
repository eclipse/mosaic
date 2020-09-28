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

package org.eclipse.mosaic.rti.api.federatestarter;

import org.eclipse.mosaic.rti.api.FederateExecutor;
import org.eclipse.mosaic.rti.api.parameters.FederateDescriptor;
import org.eclipse.mosaic.rti.config.CLocalHost;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link FederateExecutor} which starts the federate in a Java process (e.g. Phabmacs).
 */
public class JavaFederateExecutor implements FederateExecutor {

    private final String mainClass;
    private final String programArguments;
    private final FederateDescriptor handle;

    private ExecutableFederateExecutor delegateExecFederateStarter = null;

    public JavaFederateExecutor(FederateDescriptor handle, String mainClass, String programArguments) {
        this.mainClass = mainClass;
        this.programArguments = programArguments;
        this.handle = handle;
    }

    @Override
    public Process startLocalFederate(File workingDir) throws FederateStarterException {
        if (delegateExecFederateStarter != null) {
            throw new FederateStarterException("Federate has been already started");
        }

        final String fileSeparator = File.separator;
        final String pathSeparator = File.pathSeparator;

        final String classPath = createClasspath(workingDir, fileSeparator, pathSeparator);

        String currentJrePath = SystemUtils.getJavaHome().getPath();
        StringBuilder cmdBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(currentJrePath)) { //use the same JRE in which MOSAIC is running
            cmdBuilder.append(currentJrePath).append(fileSeparator).append("bin").append(fileSeparator);
        }
        cmdBuilder.append("java");

        final List<String> args = Lists.newArrayList();
        args.add("-Xmx" + handle.getJavaFederateParameters().getJavaMaxmimumMemoryMb() + "m");

        if (StringUtils.isNotBlank(handle.getJavaFederateParameters().getCustomJavaArgument())) {
            args.addAll(Arrays.asList(handle.getJavaFederateParameters().getCustomJavaArgument().split(" ")));
        }

        args.add("-cp");
        args.add(classPath);
        args.add(mainClass);
        if (StringUtils.isNotBlank(programArguments)) {
            args.addAll(Arrays.asList(programArguments.split(" ")));
        }

        delegateExecFederateStarter = new ExecutableFederateExecutor(this.handle, cmdBuilder.toString(), args);
        try {
            return delegateExecFederateStarter.startLocalFederate(workingDir);
        } catch (FederateStarterException e) {
            delegateExecFederateStarter = null;
            throw e;
        }
    }

    @Override
    public void stopLocalFederate() {
        if (delegateExecFederateStarter != null) {
            delegateExecFederateStarter.stopLocalFederate();
            delegateExecFederateStarter = null;
        }
    }

    @Override
    public int startRemoteFederate(CLocalHost host, PrintStream sshStream, InputStream sshStreamIn) throws FederateStarterException {
        if (delegateExecFederateStarter != null) {
            throw new FederateStarterException("Federate has been already started");
        }

        final File workingDir = handle.getBinariesDir();
        final String sep = File.separator;
        final String fileSep = host.operatingSystem == CLocalHost.OperatingSystem.WINDOWS ? ";" : ":";

        List<String> args = Lists.newArrayList("-cp", createClasspath(workingDir, sep, fileSep), mainClass);
        args.addAll(Arrays.asList(programArguments.split(" ")));

        delegateExecFederateStarter = new ExecutableFederateExecutor(this.handle, "java", args);
        try {
            return delegateExecFederateStarter.startRemoteFederate(host, sshStream, sshStreamIn);
        } catch (FederateStarterException e) {
            delegateExecFederateStarter = null;
            throw e;
        }
    }

    @Override
    public void stopRemoteFederate(PrintStream sshStreamOut) throws FederateStarterException {
        if (delegateExecFederateStarter != null) {
            delegateExecFederateStarter.stopRemoteFederate(sshStreamOut);
            delegateExecFederateStarter = null;
        }
    }

    private String createClasspath(File workingDir, String fileSeparator, String pathSeparator) {
        String jarName;
        final StringBuilder classPath = new StringBuilder(".");
        for (String classpathEntry : handle.getJavaFederateParameters().getJavaClasspathEntries()) {
            classPath.append(pathSeparator);
            classPath.append(classpathEntry);
        }

        // find java jar which should be started
        File[] files = workingDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().contains(".jar")) {
                    jarName = file.getName();
                    classPath.append(pathSeparator);
                    classPath.append(jarName);
                } else if (file.isDirectory() && file.getName().equals("lib")) { // find libs and add them to the class path
                    File[] libFiles = file.listFiles();
                    if (libFiles != null) {
                        for (File libFile : libFiles) {
                            if (libFile.getName().contains(".jar")) {
                                classPath.append(pathSeparator);
                                classPath.append("lib");
                                classPath.append(fileSeparator);
                                classPath.append(libFile.getName());
                            }
                        }
                    }
                }
            }
        }
        return classPath.toString();
    }


    @Override
    public String toString() {
        return "Java Executor [main: " + mainClass + ", programArguments: " + programArguments + "]";
    }

}
