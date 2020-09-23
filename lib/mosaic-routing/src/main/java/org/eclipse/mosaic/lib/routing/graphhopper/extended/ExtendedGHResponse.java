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

package org.eclipse.mosaic.lib.routing.graphhopper.extended;

import com.graphhopper.GHResponse;
import com.graphhopper.routing.Path;

import java.util.List;
import java.util.Vector;

/**
 * Extends the response of a routing request by providing the calculated path and several child
 * response containing alternative routes.
 */
public class ExtendedGHResponse extends GHResponse {

    private Path path;
    private List<ExtendedGHResponse> responses;

    public ExtendedGHResponse setPath(Path path) {
        this.path = path;
        this.responses = new Vector<>();
        return this;
    }

    public Path getPath() {
        return path;
    }

    public List<ExtendedGHResponse> getAdditionalRoutes() {
        return responses;
    }

    public void addRouteResponse(ExtendedGHResponse rsp) {
        this.responses.add(rsp);
    }

}
