/*
 * Copyright (c) 2025 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.interactions.agent;

import org.eclipse.mosaic.lib.objects.agent.AgentData;
import org.eclipse.mosaic.rti.api.Interaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides updates for agents.
 */
public class AgentUpdates extends Interaction {

    private static final long serialVersionUID = 1L;

    public final static String TYPE_ID = createTypeIdentifier(AgentUpdates.class);

    private final List<AgentData> updated = new ArrayList<>();

    private final List<String> removed = new ArrayList<>();

    public AgentUpdates(long time, List<AgentData> updates, List<String> removed) {
        super(time);
        this.updated.addAll(updates);
        this.removed.addAll(removed);
    }


    /**
     * Returns {@link AgentData} for all registered agents.
     */
    public List<AgentData> getUpdated() {
        return Collections.unmodifiableList(updated);
    }

    /**
     * Returns a list of agents (ids), which should be removed from the simulation.
     */
    public List<String> getRemoved() {
        return Collections.unmodifiableList(removed);
    }
}
