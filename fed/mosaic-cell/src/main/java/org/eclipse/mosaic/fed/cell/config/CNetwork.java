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

package org.eclipse.mosaic.fed.cell.config;

import org.eclipse.mosaic.fed.cell.config.model.CNetworkProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Storage class for convenient access to the network configuration (network.json).
 * The network.json is used to define the properties of the {@link #globalNetwork},
 * which is a {@link CNetworkProperties} and has no geographic extensions.
 */
public final class CNetwork {

    /**
     * global network definition.
     */
    public CNetworkProperties globalNetwork;

    /**
     * List of configured servers.
     */
    public List<CNetworkProperties> servers = new ArrayList<>();

    @Override
    public String toString() {
        return globalNetwork.toString();
    }
}
