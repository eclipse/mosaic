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

package org.eclipse.mosaic.interactions.mapping;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.mapping.AgentMapping;
import org.eclipse.mosaic.rti.api.Interaction;

import java.util.List;

/**
 * Registers a new agent as defined in the mapping configuration.
 */
public class AgentRegistration extends Interaction {

    private static final long serialVersionUID = 1L;

    public final static String TYPE_ID = createTypeIdentifier(AgentRegistration.class);

    private final GeoPoint origin;
    private final GeoPoint destination;
    private final AgentMapping agentMapping;

    public AgentRegistration(long time, String name, String group, GeoPoint origin, GeoPoint destination, final List<String> applications, double walkingSpeed) {
        super(time);
        this.origin = origin;
        this.destination = destination;
        this.agentMapping = new AgentMapping(name, group, applications, walkingSpeed);
    }

    public GeoPoint getOrigin() {
        return origin;
    }

    public GeoPoint getDestination() {
        return destination;
    }

    public AgentMapping getMapping() {
        return agentMapping;
    }

}
