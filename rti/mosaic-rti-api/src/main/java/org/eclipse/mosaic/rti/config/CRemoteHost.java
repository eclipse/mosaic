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

/**
 * This class describes a host that is to be used to deploy/start/stop/undeploy
 * a federate remotely using SSH.
 */
public class CRemoteHost extends CLocalHost {

    /**
     * The user name with which a connection to the host is to be created.
     */
    public String user;

    /**
     * The user password with which a connection to the host is to be created.
     */
    public String password;

    /**
     * The listener port of the SSH server on the remote host.
     */
    public Integer port;

    public CRemoteHost() {
        // nop
    }

    public CRemoteHost(final String address, final int port, final String user, final String password, final String workingDirectory) {
        super(workingDirectory);
        this.address = address;
        this.port = port;
        this.user = user;
        this.password = password;
    }
}