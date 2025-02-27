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

import org.eclipse.mosaic.rti.api.Interaction;

/**
 * Interaction to request the removal of an agent.
 */
public class AgentRemoval extends Interaction {

    private static final long serialVersionUID = 1L;

    public final static String TYPE_ID = createTypeIdentifier(AgentRemoval.class);

    private final String agentId;

    /**
     * Creates a new request to remove an agent.
     *
     * @param time    the time at which the agent should be removed.
     * @param agentId the id of the agent to be removed.
     */
    public AgentRemoval(long time, String agentId) {
        super(time);
        this.agentId = agentId;
    }

    /**
     * Returns the id of the agent which should be removed.
     */
    public String getAgentId() {
        return agentId;
    }

}