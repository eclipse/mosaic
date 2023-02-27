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

package org.eclipse.mosaic.fed.application.ambassador;

import org.eclipse.mosaic.fed.application.ambassador.navigation.CentralNavigationComponent;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.CentralPerceptionComponent;
import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.util.junit.TestUtils;
import org.eclipse.mosaic.lib.util.scheduling.EventManager;
import org.eclipse.mosaic.rti.api.Interactable;

import org.junit.rules.ExternalResource;

/**
 * This rule can be included into tests, if they require a SimulationKernel singleton,
 * which members are reset automatically after each test.
 */
public class SimulationKernelRule extends ExternalResource {

    private EventManager eventManager;
    private Interactable interactable;
    private CentralNavigationComponent navigation;
    private CentralPerceptionComponent perception;

    public SimulationKernelRule(
            final EventManager eventManager,
            final Interactable interactable,
            final CentralNavigationComponent navigation,
            final CentralPerceptionComponent perception
    ) {
        this.eventManager = eventManager;
        this.interactable = interactable;
        this.navigation = navigation;
        this.perception = perception;
    }

    public SimulationKernelRule() {
        this(null, null, null, null);
    }

    public void setSimulationTime(long time) {
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "currentSimulationTime", time);
    }

    @Override
    protected void before() {
        setSimulationTime(0);

        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "classLoader", null);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "interactable", interactable);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "eventManager", eventManager);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "navigation", navigation);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "centralPerceptionComponent", perception);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "configuration", null);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "configurationPath", null);
    }

    @Override
    protected void after() {
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "eventManager", null);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "interactable", null);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "navigation", null);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "centralPerceptionComponent", null);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "classLoader", null);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "randomNumberGenerator", null);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "configuration", null);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "configurationPath", null);

        UnitSimulator.UnitSimulator.removeAllSimulationUnits();
    }

    public void setRandomNumberGenerator() {
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "randomNumberGenerator", new DefaultRandomNumberGenerator(289381268L));
    }

}
