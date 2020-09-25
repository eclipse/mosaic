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

package org.eclipse.mosaic.rti.config;

import org.apache.commons.lang3.SystemUtils;

/**
 * This class describes a host that is to be used to deploy/start/stop/undeploy a federate.
 */
public class CLocalHost {

    /**
     * Enumeration for the operating system types.
     */
    public enum OperatingSystem {
        WINDOWS, LINUX, UNKNOWN;

        public static OperatingSystem getSystemOperatingSystem() {
            if (SystemUtils.IS_OS_WINDOWS) {
                return WINDOWS;
            } else if (SystemUtils.IS_OS_UNIX) {
                return LINUX;
            } else {
                return UNKNOWN;
            }
        }
    }

    /**
     * The id for the host. Default id used for the localhost is "local".
     */
    public String id = "local";

    /**
     * The parent directory on the host for all further tasks.
     * "./tmp" is the default directory which should use as working directory
     * on the localhost.
     */
    public String workingDirectory = "./tmp";

    /**
     * IP (IPv4) or fully-qualified host name of the host (default: localhost).
     */
    public String address = "localhost";

    /**
     * The operating system running on the host.
     */
    public OperatingSystem operatingSystem = OperatingSystem.UNKNOWN;

    public CLocalHost() {
        // nop
    }

    /**
     * Creates a new CLocalHost instance with the given {@param workingDirectory}.
     *
     * @param workingDirectory The parent working directory of the host.
     */
    public CLocalHost(final String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
}

