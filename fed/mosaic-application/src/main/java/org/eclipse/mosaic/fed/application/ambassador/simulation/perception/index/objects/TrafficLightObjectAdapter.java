/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects;

import org.eclipse.mosaic.lib.spatial.SpatialItemAdapter;

public class TrafficLightObjectAdapter implements SpatialItemAdapter<TrafficLightObject> {
    @Override
    public double getCenterX(TrafficLightObject item) {
        return item.getProjectedPosition().getX();
    }

    @Override
    public double getCenterY(TrafficLightObject item) {
        return item.getProjectedPosition().getZ();
    }

    @Override
    public double getCenterZ(TrafficLightObject item) {
        return -item.getProjectedPosition().getY();
    }

    @Override
    public double getMinX(TrafficLightObject item) {
        return getCenterX(item);
    }

    @Override
    public double getMinY(TrafficLightObject item) {
        return getCenterY(item);
    }

    @Override
    public double getMinZ(TrafficLightObject item) {
        return getCenterZ(item);
    }
}