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

import org.eclipse.mosaic.lib.routing.graphhopper.GraphLoader;
import org.eclipse.mosaic.lib.routing.graphhopper.util.GraphhopperToDatabaseMapper;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.storage.TurnCostExtension;
import org.junit.rules.ExternalResource;

public class TestGraphRule extends ExternalResource implements GraphLoader {

    private GraphHopperStorage graph;
    private EncodingManager encodingManager;

    private TurnCostExtension turnCostStorage;
    private boolean emptyGraph;

    public TestGraphRule(EncodingManager encodingManager) {
        this(encodingManager, false);
    }

    public TestGraphRule(EncodingManager encodingManager, boolean emptyGraph) {
        this.encodingManager = encodingManager;
        this.emptyGraph = emptyGraph;
    }

    @Override
    protected void before() throws Throwable {
        turnCostStorage = new TurnCostExtension() {
            @Override
            public boolean isRequireEdgeField() {
                return true;
            }

            @Override
            public int getDefaultEdgeFieldValue() {
                return 0;
            }
        };

        final RAMDirectory dir = new RAMDirectory();
        graph = new GraphHopperStorage(dir, encodingManager, true, turnCostStorage);

        if (!emptyGraph) {
            graph = graph.create(100);
            createTestGraph();
        }

    }

    @Override
    protected void after() {
        graph.close();
        graph = null;
        turnCostStorage = null;
    }

    public GraphHopperStorage getGraph() {
        return graph;
    }

    public TurnCostExtension getTurnCostStorage() {
        return turnCostStorage;
    }

    @Override
    public void initialize(GraphHopperStorage graph, EncodingManager encodingManager, GraphhopperToDatabaseMapper mapper) {
        this.graph = graph;
        this.turnCostStorage = (TurnCostExtension) graph.getExtension();
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

        graph.edge(0, 1, 1000, true); //0
        graph.edge(0, 4, 1000, true); //1
        graph.edge(1, 2, 1000, true); //2
        graph.edge(1, 5, 1000, true); //3
        graph.edge(1, 7, 2500, true); //4
        graph.edge(2, 3, 1000, true); //5
        graph.edge(2, 6, 1600, true); //6
        graph.edge(3, 6, 1000, true); //7
        graph.edge(4, 7, 1000, true); //8
        graph.edge(5, 6, 2000, true); //9
        graph.edge(5, 7, 1600, true); //10
        graph.edge(5, 10, 3000, true); //11
        graph.edge(6, 10, 2000, true); //12
        graph.edge(7, 8, 1000, true); //13
        graph.edge(7, 9, 1500, true); //14
        graph.edge(8, 11, 1000, true); //15
        graph.edge(9, 10, 2000, true); //16
        graph.edge(9, 11, 1500, true); //17
        graph.edge(9, 13, 1500, true); //18
        graph.edge(10, 13, 1500, true); //19
        graph.edge(11, 12, 1000, true); //20
        graph.edge(12, 13, 1000, true); //21
    }

}
