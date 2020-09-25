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

package org.eclipse.mosaic.rti.api.parameters;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.File;

public class AmbassadorParameter {

    /**
     * Identifier of this ambassador.
     */
    public final String ambassadorId;

    /**
     * Path and file name of federate configuration.
     * Each federate ambassador needs references to its specific configuration file as well as the
     * configuration directory (usually /scenarios/SCENARIO_NAME/FEDERATE_ID).
     */
    public final File configuration;

    public AmbassadorParameter(String ambassadorId, File configuration) {
        this.ambassadorId = ambassadorId;
        this.configuration = configuration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ambassadorId", ambassadorId)
                .append("configuration", configuration)
                .toString();
    }
}
