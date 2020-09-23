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

package org.eclipse.mosaic.rti.api.parameters;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides parameter for federate processes which run in a Java virtual machine.
 */
public class JavaFederateParameters {

    private final int jvmMaxHeapMB;

    private final String customJavaArgument;

    private final List<String> javaClasspathEntries = new ArrayList<>();

    /**
     * Creates a new parameter object providing configuration for Java processes.
     *
     * @param jvmMaxHeapMB the maximum amount of memory to be allocated by the JVM
     * @param customJavaArgument a custom argument to pass to the JVM
     */
    public JavaFederateParameters(int jvmMaxHeapMB, String customJavaArgument) {
        this.jvmMaxHeapMB = jvmMaxHeapMB;
        this.customJavaArgument = customJavaArgument;
    }

    /**
     * Based on the current system, default parameters for memory reservation (-Xmx, -Xms) will be
     * returned within an instance of {@link JavaFederateParameters}.
     *
     * @return instance with default Java parameters
     */
    public static JavaFederateParameters defaultParameters() {
        final int suggestedJavaMemorySizeXmx;
        if ("32".equals(System.getProperty("sun.arch.data.model"))) {
            suggestedJavaMemorySizeXmx = 512;
        } else {
            // meanwhile, 64 bit is much more common.
            suggestedJavaMemorySizeXmx = 4096;
        }
        return new JavaFederateParameters(suggestedJavaMemorySizeXmx, null);
    }

    /**
     * Returns the maximum amount of memory to be allocated by the JVM (in Megabyte).
     *
     * @return amount of memory in Megabyte
     */
    public int getJavaMaxmimumMemoryMb() {
        return jvmMaxHeapMB;
    }

    /**
     * Returns the custom argument to pass to the JVM.
     *
     * @return the custom Java argument
     */
    public String getCustomJavaArgument() {
        return customJavaArgument;
    }

    /**
     * Returns a list of classpath entries to append to the Java call.
     *
     * @return a list of classpath entries
     */
    public List<String> getJavaClasspathEntries() {
        return this.javaClasspathEntries;
    }

    /**
     * Adds a single classpath entry to this parameter set.
     *
     * @param classpathEntry a single classpath entry without quotes
     */
    public JavaFederateParameters addJavaClasspathEntry(String classpathEntry) {
        this.javaClasspathEntries.add(classpathEntry);
        return this;
    }
}
