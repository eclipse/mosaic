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
 */

package org.eclipse.mosaic.lib.routing;

/**
 * Class containing information for a routing request.
 * Gathers information in one object.
 */
public class RoutingRequest {

    private final RoutingPosition source;
    private final RoutingPosition target;
    private final RoutingParameters routingParameters;

    /**
     * Creates a routing request with information about the current and the target position with specified routing parameters.
     *
     * @param source            Current position as {@link RoutingPosition}.
     * @param target            Target position as {@link RoutingPosition}.
     * @param routingParameters Routing parameters for route calculation.
     */
    public RoutingRequest(RoutingPosition source, RoutingPosition target, RoutingParameters routingParameters) {
        this.source = source;
        this.target = target;
        this.routingParameters = routingParameters;
    }

    /**
     * Creates a routing request with information about the current and the target position with default routing parameters.
     *
     * @param source Current position as {@link RoutingPosition}.
     * @param target Target position as {@link RoutingPosition}.
     */
    public RoutingRequest(RoutingPosition source, RoutingPosition target) {
        this(source, target, new RoutingParameters());
    }

    public RoutingPosition getSource() {
        return source;
    }

    public RoutingPosition getTarget() {
        return target;
    }

    public RoutingParameters getRoutingParameters() {
        return routingParameters;
    }
}
