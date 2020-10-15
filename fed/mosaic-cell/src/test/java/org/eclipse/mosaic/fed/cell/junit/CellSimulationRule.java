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

package org.eclipse.mosaic.fed.cell.junit;

import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.fed.cell.data.SimulationData;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

public class CellSimulationRule extends ExternalResource {

    private static final Logger log = LoggerFactory.getLogger(CellSimulationRule.class);

    @Override
    protected void before() {
        assertTrue(
                "Invalid state, SimulationData.INSTANCE is not empty. Forget to cleanup?",
                SimulationData.INSTANCE.getAllNodesInSimulation().isEmpty()
        );
    }

    @Override
    protected void after() {
        for (String node : new HashSet<>(SimulationData.INSTANCE.getAllNodesInSimulation())) {
            log.info("Remove node {} after test", node);
            SimulationData.INSTANCE.removeNode(node);
        }
    }
}
