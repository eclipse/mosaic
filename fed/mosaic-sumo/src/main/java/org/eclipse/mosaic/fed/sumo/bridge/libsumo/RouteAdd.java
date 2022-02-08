/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.sumo.bridge.libsumo;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;

import org.eclipse.sumo.libsumo.Route;
import org.eclipse.sumo.libsumo.StringVector;

import java.util.List;

public class RouteAdd implements org.eclipse.mosaic.fed.sumo.bridge.api.RouteAdd {

    public void execute(Bridge bridge, String routeId, List<String> edges) {
        Route.add(routeId, new StringVector(edges));
    }

}
