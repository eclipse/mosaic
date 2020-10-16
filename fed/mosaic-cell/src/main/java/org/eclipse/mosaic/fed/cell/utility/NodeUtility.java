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

package org.eclipse.mosaic.fed.cell.utility;

import org.eclipse.mosaic.fed.cell.data.SimulationData;
import org.eclipse.mosaic.lib.objects.communication.CellConfiguration;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains static methods to make the handling of the configuration of the nodes easier.
 */
public class NodeUtility {

    private static final SimulationData simData = SimulationData.INSTANCE;

    /**
     * Get the cell configuration of a node.
     *
     * @param nodeId the id of the node whose cell configuration is requested
     * @return the cell configuration of the node with the id nodeId
     * @throws InternalFederateException if cellConfiguration of Node wasn't set
     */
    public static CellConfiguration getCellConfigurationOfNodeByName(String nodeId) throws InternalFederateException {
        return simData.getCellConfigurationOfNode(nodeId);
    }

    /**
     * Get the list of the cell configurations of all nodes that are registered within the simulation.
     *
     * @return list of all cell configuration of all registered nodes.
     */
    public static List<CellConfiguration> getAllCellConfigurations() throws InternalFederateException {
        List<CellConfiguration> configs = new ArrayList<>();
        for (String name : simData.getAllNodesInSimulation()) {
            configs.add(simData.getCellConfigurationOfNode(name));
        }
        return configs;
    }
}
