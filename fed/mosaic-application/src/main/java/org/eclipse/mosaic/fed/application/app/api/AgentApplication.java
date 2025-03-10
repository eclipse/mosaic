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

package org.eclipse.mosaic.fed.application.app.api;

import org.eclipse.mosaic.lib.objects.agent.AgentData;
import org.eclipse.mosaic.lib.objects.agent.AgentRoute;

/**
 * All agent applications which want to react on position updates
 * of its agent unit must implement this interface.
 */
public interface AgentApplication extends Application {

    /**
     * Is called whenever the {@link AgentData} was updated.
     *
     * @param previousAgentData the previous agent data
     * @param updatedAgentData  the updated agent data
     */
    void onAgentUpdated(AgentData previousAgentData, AgentData updatedAgentData);

    /**
     * Is called after onAgentUpdated, if a new leg has been started and/or the previous leg has been finished
     *
     * @param finishedLeg the finished leg of the route, may be {@code null}
     * @param nextLeg the new leg of the route, may be {@code null}
     */
    void onLegChanged(AgentRoute.Leg finishedLeg, AgentRoute.Leg nextLeg);
}
