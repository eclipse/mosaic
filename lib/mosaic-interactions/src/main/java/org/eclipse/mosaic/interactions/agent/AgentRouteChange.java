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

import org.eclipse.mosaic.lib.objects.agent.AgentRoute;
import org.eclipse.mosaic.rti.api.Interaction;

public class AgentRouteChange extends Interaction {

    private static final long serialVersionUID = 1L;

    public final static String TYPE_ID = createTypeIdentifier(AgentRouteChange.class);

    private final String agentId;
    private final AgentRoute route;

    public AgentRouteChange(long time, String agentId, AgentRoute route) {
        super(time);
        this.route = route;
        this.agentId = agentId;
    }

    public String getAgentId() {
        return agentId;
    }

    public AgentRoute getRoute() {
        return this.route;
    }
}