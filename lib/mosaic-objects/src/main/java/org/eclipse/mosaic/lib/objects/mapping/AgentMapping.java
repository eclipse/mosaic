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

package org.eclipse.mosaic.lib.objects.mapping;

import java.util.List;

public class AgentMapping extends UnitMapping {

    private static final long serialVersionUID = 1L;

    private final double walkingSpeed;

    /**
     * Contains configuration of an agent.
     * @param name name of the unit
     * @param group group that the unit belongs to
     * @param applications a list of applications to be mapped onto the unit
     * @param walkingSpeed the speed this agent has when walking, in m/s
     */
    public AgentMapping(final String name, final String group, final List<String> applications, double walkingSpeed) {
        super(name, group, applications);
        this.walkingSpeed = walkingSpeed;
    }

    /**
     * Returns the walking speed of the agent in m/s
     */
    public double getWalkingSpeed() {
        return walkingSpeed;
    }
}
