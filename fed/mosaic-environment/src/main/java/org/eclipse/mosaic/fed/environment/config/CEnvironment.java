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

package org.eclipse.mosaic.fed.environment.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Environment simulator configuration. Here you can define which events are evaluated by the simulator.
 * Entities entering the areas of the events, are notified by the
 * {@link org.eclipse.mosaic.interactions.environment.EnvironmentSensorUpdates} interaction.
 */
public class CEnvironment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * List of the events.
     */
    public List<CEvent> events = new ArrayList<>();

}

