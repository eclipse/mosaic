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

package org.eclipse.mosaic.lib.routing.graphhopper.util;

import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.ObjectIntHashMap;
import com.carrotsearch.hppc.ObjectIntMap;

public class GraphhopperToDatabaseMapper {

    private IntObjectMap<Connection> graphToDbConnectionMap;
    private ObjectIntMap<Connection> dbToGraphConnectionMap;
    private IntObjectMap<Node> graphToDbNodeMap;
    private ObjectIntMap<Node> dbToGraphNodeMap;

    public Connection toConnection(Integer connectionId) {
        return getGraphToDbConnectionMap().get(connectionId);
    }

    public Integer fromConnection(Connection con) {
        if (getDbToGraphConnectionMap().containsKey(con)) {
            return getDbToGraphConnectionMap().get(con);
        }
        return -1;
    }

    public void setConnection(Connection con, Integer conId) {
        getDbToGraphConnectionMap().put(con, conId);
        getGraphToDbConnectionMap().put(conId, con);
    }

    public Node toNode(Integer nodeId) {
        return getGraphToDbNodeMap().get(nodeId);
    }

    public Integer fromNode(Node node) {
        if (getDbToGraphNodeMap().containsKey(node)) {
            return getDbToGraphNodeMap().get(node);
        }
        return -1;
    }

    public void setNode(Node node, Integer nodeId) {
        getDbToGraphNodeMap().put(node, nodeId);
        getGraphToDbNodeMap().put(nodeId, node);
    }

    private ObjectIntMap<Connection> getDbToGraphConnectionMap() {
        if (dbToGraphConnectionMap == null) {
            dbToGraphConnectionMap = new ObjectIntHashMap<>();
        }
        return dbToGraphConnectionMap;
    }

    private IntObjectMap<Connection> getGraphToDbConnectionMap() {
        if (graphToDbConnectionMap == null) {
            graphToDbConnectionMap = new IntObjectHashMap<>();
        }
        return graphToDbConnectionMap;
    }

    private ObjectIntMap<Node> getDbToGraphNodeMap() {
        if (dbToGraphNodeMap == null) {
            dbToGraphNodeMap = new ObjectIntHashMap<>();
        }
        return dbToGraphNodeMap;
    }

    private IntObjectMap<Node> getGraphToDbNodeMap() {
        if (graphToDbNodeMap == null) {
            graphToDbNodeMap = new IntObjectHashMap<>();
        }
        return graphToDbNodeMap;
    }

}
