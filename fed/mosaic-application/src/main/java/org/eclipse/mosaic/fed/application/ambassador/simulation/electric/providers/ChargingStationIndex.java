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
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.electricity.ChargingStationData;
import org.eclipse.mosaic.lib.spatial.KdTree;
import org.eclipse.mosaic.lib.spatial.SpatialTreeTraverser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChargingStationIndex {

    private final int bucketSize;

    /**
     * Stores {@link ChargingStationObject}s for fast removal and position update.
     */
    final Map<String, ChargingStationObject> indexedChargingStations = new HashMap<>();

    private KdTree<ChargingStationObject> chargingStationTree;

    private SpatialTreeTraverser.InRadius<ChargingStationObject> treeTraverser;

    public ChargingStationIndex(int bucketSize) {
        this.bucketSize = bucketSize;
    }

    public void addChargingStation(String id, GeoPoint position) {
        indexedChargingStations.computeIfAbsent(id, ChargingStationObject::new)
                .setPosition(position.toCartesian());
    }

    /**
     * Perform action before an update of the {@link ChargingStationIndex} takes place.
     */
    public void updateChargingStation(ChargingStationData chargingStationData) {
        initSearchTree();
        indexedChargingStations.get(chargingStationData.getName())
                .setChargingStationData(chargingStationData);
    }

    private void initSearchTree() {
        if (chargingStationTree != null) {
            return;
        }

        List<ChargingStationObject> allChargingStations = new ArrayList<>(indexedChargingStations.values());
        chargingStationTree = new KdTree<>(new SpatialObjectAdapter<>(), allChargingStations, bucketSize);
        treeTraverser = new SpatialTreeTraverser.InRadius<>();
    }

    public List<ChargingStationObject> getChargingStationsInCircle(GeoCircle circle) {
        treeTraverser.setup(circle.getCenter().toVector3d(), circle.getRadius());
        treeTraverser.traverse(chargingStationTree);
        return treeTraverser.getResult();
    }

    public int getNumberOfChargingStations() {
        return chargingStationTree.getRoot().size();
    }

}
