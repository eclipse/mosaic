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

import org.eclipse.mosaic.fed.cell.config.model.CMobileNetworkProperties;
import org.eclipse.mosaic.fed.cell.config.model.CNetworkProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Storage class for the region configuration (regions.json).
 * It contains the properties of the {@link CMobileNetworkProperties}, which extend the
 * {@link CNetworkProperties} with regional
 * information. These properties can be used to emulate the mobile network. If configured
 * very granular, these regions can reflect cells as used in the real world. Though for most
 * use-cases it is sufficient to approximate the behaviour by defining larger regions.
 */
public final class CRegion {

    // Configured regions
    public List<CMobileNetworkProperties> regions = new ArrayList<>();

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        for (CMobileNetworkProperties region : regions) {
            builder.append(region.toString()).append("\t\t");
        }
        return builder.toString();
    }
}
