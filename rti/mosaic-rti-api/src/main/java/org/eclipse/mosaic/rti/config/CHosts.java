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

import java.util.ArrayList;
import java.util.List;

/**
 * Specify the hosts, where the federates should run.
 */
public class CHosts {

    /**
     * List of the local hosts.
     */
    public List<CLocalHost> localHosts = new ArrayList<>();

    /**
     * Lists of the remote hosts.
     */
    public List<CRemoteHost> remoteHosts = new ArrayList<>();

    public final void addDefaultLocalHost() {
        CLocalHost defaultHost = new CLocalHost();
        defaultHost.operatingSystem = CLocalHost.OperatingSystem.getSystemOperatingSystem();
        localHosts.add(defaultHost);
    }

    /**
     * Get the first local host corresponds to the given id.
     *
     * @param id The host id.
     * @return CLocalHost
     */
    public final CLocalHost getLocalHostById(String id) {
        for (CLocalHost host : localHosts) {
            if (host.id.equals(id)) {
                return host;
            }
        }
        return null;
    }

    /**
     * Get the first remote host corresponds to the given id.
     *
     * @param id The host id.
     * @return CRemoteHost
     */
    public final CRemoteHost getRemoteHostById(String id) {
        for (CRemoteHost host : remoteHosts) {
            if (host.id.equals(id)) {
                return host;
            }
        }
        return null;
    }

    /**
     * Get the first local or remote host corresponds to the given id.
     *
     * @param id The host id.
     * @return CLocalHost or CRemoteHost
     */
    public final CLocalHost getHostById(String id) {
        CLocalHost host;

        //Check the local hosts
        host = getLocalHostById(id);
        if (host != null) {
            return host;
        }

        //Check the remote hosts
        host = getRemoteHostById(id);

        return host;
    }
}

