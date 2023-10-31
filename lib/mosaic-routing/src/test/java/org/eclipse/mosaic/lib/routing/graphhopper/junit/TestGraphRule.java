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

package org.eclipse.mosaic.lib.routing.graphhopper.junit;

import org.eclipse.mosaic.lib.routing.graphhopper.GraphHopperRouting;
import org.eclipse.mosaic.lib.routing.graphhopper.GraphLoader;
import org.eclipse.mosaic.lib.routing.graphhopper.VehicleEncoding;
import org.eclipse.mosaic.lib.routing.graphhopper.VehicleEncodingManager;
import org.eclipse.mosaic.lib.routing.graphhopper.util.GraphhopperToDatabaseMapper;

import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.storage.BaseGraph;
import org.junit.rules.ExternalResource;

public class TestGraphRule extends ExternalResource implements GraphLoader {

    private BaseGraph graph;
    private VehicleEncodingManager encodingManager;

    private final boolean emptyGraph;

    public TestGraphRule() {
        this(false);
    }

    public TestGraphRule(boolean emptyGraph) {
        this.encodingManager = new VehicleEncodingManager(GraphHopperRouting.PROFILES);
        this.emptyGraph = emptyGraph;
    }

    public VehicleEncodingManager getEncodingManager() {
        return encodingManager;
    }

    @Override
    protected void before() throws Throwable {

        graph = new BaseGraph.Builder(encodingManager.getEncodingManager())
                .withTurnCosts(true)
                .build();

        if (!emptyGraph) {
            graph = graph.create(100);
            createTestGraph();
        }

    }

    @Override
    protected void after() {
        graph.close();
        graph = null;
    }

    public BaseGraph getGraph() {
        return graph;
    }

    @Override
    public void initialize(BaseGraph graph, VehicleEncodingManager encodingManager, GraphhopperToDatabaseMapper mapper) {
        this.graph = graph;
        this.encodingManager = encodingManager;
    }

    @Override
    public void loadGraph() {
        graph.create(25);
        createTestGraph();
    }

    private void createTestGraph() {
        graph.getNodeAccess().setNode(0, 0.00, 0.00, 0); //A
        graph.getNodeAccess().setNode(1, 0.01, 0.00, 0); //B
        graph.getNodeAccess().setNode(2, 0.02, 0.00, 0); //C
        graph.getNodeAccess().setNode(3, 0.03, 0.00, 0); //D
        graph.getNodeAccess().setNode(4, 0.00, 0.01, 0); //E
        graph.getNodeAccess().setNode(5, 0.01, 0.01, 0); //F
        graph.getNodeAccess().setNode(6, 0.03, 0.01, 0); //G
        graph.getNodeAccess().setNode(7, 0.00, 0.02, 0); //H
        graph.getNodeAccess().setNode(8, 0.00, 0.03, 0); //I
        graph.getNodeAccess().setNode(9, 0.01, 0.03, 0); //J
        graph.getNodeAccess().setNode(10, 0.03, 0.03, 0); //K
        graph.getNodeAccess().setNode(11, 0.00, 0.04, 0); //L
        graph.getNodeAccess().setNode(12, 0.01, 0.04, 0); //M
        graph.getNodeAccess().setNode(13, 0.02, 0.04, 0); //N


        graph.edge(0, 1).setDistance(1000); //0
        graph.edge(0, 4).setDistance(1000); //1
        graph.edge(1, 2).setDistance(1000); //2
        graph.edge(1, 5).setDistance(1000); //3
        graph.edge(1, 7).setDistance(2500); //4
        graph.edge(2, 3).setDistance(1000); //5
        graph.edge(2, 6).setDistance(1600); //6
        graph.edge(3, 6).setDistance(1000); //7
        graph.edge(4, 7).setDistance(1000); //8
        graph.edge(5, 6).setDistance(2000); //9
        graph.edge(5, 7).setDistance(1600); //10
        graph.edge(5, 10).setDistance(3000); //11
        graph.edge(6, 10).setDistance(2000); //12
        graph.edge(7, 8).setDistance(1000); //13
        graph.edge(7, 9).setDistance(1500); //14
        graph.edge(8, 11).setDistance(1000); //15
        graph.edge(9, 10).setDistance(2000); //16
        graph.edge(9, 11).setDistance(1500); //17
        graph.edge(9, 13).setDistance(1500); //18
        graph.edge(10, 13).setDistance(1500); //19
        graph.edge(11, 12).setDistance(1000); //20
        graph.edge(12, 13).setDistance(1000); //21


        VehicleEncoding enc = encodingManager.getVehicleEncoding("car");
        AllEdgesIterator it = graph.getAllEdges();
        while (it.next()) {
            it.set(enc.access(), true);
            it.set(enc.speed(), 50 / 3.6);
            it.setReverse(enc.access(), true);
            it.setReverse(enc.speed(), 50 / 3.6);
        }
    }

}
