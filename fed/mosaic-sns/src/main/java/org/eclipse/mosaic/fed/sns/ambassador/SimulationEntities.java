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

package org.eclipse.mosaic.fed.sns.ambassador;

import org.eclipse.mosaic.lib.geo.CartesianPoint;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents all simulation entities in the simulation that are capable to communicate via Ad-hoc network.
 */
enum SimulationEntities {
    INSTANCE;

    /**
     * There are two kinds of nodes: those who are online (and can send and receive messages).
     */
    private final HashMap<String, SimulationNode> onlineNodes = new HashMap<>();

    /**
     * Offline nodes (may get switched on).
     */
    private final HashMap<String, CartesianPoint> offlineNodes = new HashMap<>();

    /**
     * Gets all nodes currently known as online (initialized, Wifi enabled) in the simulation.
     *
     * @return A set of all nodes.
     */
    public Map<String, SimulationNode> getAllOnlineNodes() {
        return onlineNodes;
    }

    /**
     * Gets information (position, communication radius) for one individual node currently known as online.
     *
     * @return A set of all nodes.
     */
    public SimulationNode getOnlineNode(String nodeName) {
        return onlineNodes.get(nodeName);
    }

    public boolean isNodeSimulated(String nodeName) {
        return onlineNodes.containsKey(nodeName) || offlineNodes.containsKey(nodeName);
    }

    public boolean isNodeOnline(String nodeName) {
        return onlineNodes.containsKey(nodeName);
    }

    public boolean isNodeOffline(String nodeName) {
        return offlineNodes.containsKey(nodeName);
    }

    /**
     * Creates a node with enabled wifi using the given parameters.
     *
     * @param nodeName identifier for the node
     * @param position position of the node
     * @param radius   transmission radius of the node
     */
    public void createOnlineNode(String nodeName, CartesianPoint position, double radius) {
        if (nodeName != null && position != null) {
            SimulationNode nodeData = new SimulationNode();
            nodeData.setPosition(position);
            nodeData.setRadius(radius);
            onlineNodes.put(nodeName, nodeData);
        } else {
            throw new RuntimeException("Unable to move node=" + nodeName + " to position=" + position);
        }
    }

    /**
     * Updates the position of a node with enabled wifi.
     *
     * @param nodeName identifier of the node to be updated
     * @param position the updated position
     */
    public void updateOnlineNode(String nodeName, CartesianPoint position) {
        if (nodeName != null && position != null) {
            onlineNodes.get(nodeName).setPosition(position);
        } else {
            throw new RuntimeException("Unable to update node=" + nodeName + " to position=" + position);
        }
    }

    /**
     * Updates the position of node with disabled wifi.
     *
     * @param nodeName identifier of the node to be updated
     * @param position the updated position
     */
    public void createOrUpdateOfflineNode(String nodeName, CartesianPoint position) {
        if (nodeName != null && position != null) {
            offlineNodes.put(nodeName, position);
        } else {
            throw new RuntimeException("Unable to create/update node=" + nodeName + " to position=" + position);
        }
    }

    /**
     * Removes the node (due to simplicity and speed from both maps - on/offlineNodes).
     */
    public void removeNode(String nodeId) {
        onlineNodes.remove(nodeId);
        offlineNodes.remove(nodeId);
    }

    /**
     * Enables the wifi for a node and sets the specified radius.
     *
     * @param nodeName            identifier of the node to have it's wifi enabled
     * @param communicationRadius communication radius for the node
     */
    public void enableWifi(String nodeName, double communicationRadius) {
        if (isNodeOffline(nodeName)) {
            SimulationNode nodeData = new SimulationNode();
            nodeData.setPosition(offlineNodes.get(nodeName));
            nodeData.setRadius(communicationRadius);
            onlineNodes.put(nodeName, nodeData);
            offlineNodes.remove(nodeName);
        } else {
            onlineNodes.get(nodeName).setRadius(communicationRadius);
        }
    }

    /**
     * Disables wifi capabilities for a node.
     *
     * @param nodeName name of the node
     */
    public void disableWifi(String nodeName) {
        if (isNodeOnline(nodeName)) {
            offlineNodes.put(nodeName, onlineNodes.get(nodeName).getPosition());
            onlineNodes.remove(nodeName);
        }
    }

    /**
     * Clears all used maps and thereby resets the state of the entities.
     */
    public void reset() {
        onlineNodes.clear();
        offlineNodes.clear();
    }
}
