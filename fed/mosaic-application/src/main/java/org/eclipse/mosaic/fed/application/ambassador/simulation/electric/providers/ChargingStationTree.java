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

package org.eclipse.mosaic.fed.application.ambassador.simulation.electric.providers;

import org.eclipse.mosaic.fed.application.ambassador.simulation.electric.objects.ChargingStationObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.SpatialObjectAdapter;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.spatial.KdTree;
import org.eclipse.mosaic.lib.spatial.SpatialTreeTraverser;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ChargingStationIndex} using a KD-Tree to store charging stations.
 */
public class ChargingStationTree extends ChargingStationIndex {
    private final int bucketSize;

    private KdTree<ChargingStationObject> chargingStationTree;

    private SpatialTreeTraverser.InRadius<ChargingStationObject> treeTraverser;

    public ChargingStationTree(int bucketSize) {
        this.bucketSize = bucketSize;
    }

    @Override
    public void initialize() {
        // initialization at first update
    }

    @Override
    public List<ChargingStationObject>getChargingStationsInCircle(GeoCircle circle) {
        treeTraverser.setup(circle.getCenter().toVector3d(), circle.getRadius());
        treeTraverser.traverse(chargingStationTree);
        return treeTraverser.getResult();
    }

    @Override
    public void onChargingStationUpdate() {
        if (chargingStationTree == null) { // initialize before first update is called
            List<ChargingStationObject> allChargingStations = new ArrayList<>(indexedChargingStations.values());
            chargingStationTree = new KdTree<>(new SpatialObjectAdapter<>(), allChargingStations, bucketSize);
            treeTraverser = new SpatialTreeTraverser.InRadius<>();
        }
    }

    @Override
    public int getNumberOfChargingStations() {
        return chargingStationTree.getRoot().size();
    }
}
