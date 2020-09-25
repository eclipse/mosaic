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

package org.eclipse.mosaic.lib.routing.graphhopper.extended;

import com.graphhopper.util.NumHelper;
import com.graphhopper.util.shapes.GHPoint;

public class ExtendedGHPoint extends GHPoint {

    private final int edgeId;

    public ExtendedGHPoint(double lat, double lon, int edgeId) {
        super(lat, lon);
        this.edgeId = edgeId;
    }

    public int getEdgeId() {
        return edgeId;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 83 * hash + edgeId;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof ExtendedGHPoint)) {
            return false;
        }
        return NumHelper.equals(edgeId, ((ExtendedGHPoint) obj).edgeId);
    }

}
