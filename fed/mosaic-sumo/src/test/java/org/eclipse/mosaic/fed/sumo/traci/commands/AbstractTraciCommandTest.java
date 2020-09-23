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

package org.eclipse.mosaic.fed.sumo.traci.commands;

import org.eclipse.mosaic.fed.sumo.traci.junit.SumoTraciRule;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.UtmPoint;
import org.eclipse.mosaic.lib.geo.UtmZone;
import org.eclipse.mosaic.lib.junit.GeoProjectionRule;
import org.eclipse.mosaic.lib.transform.GeoProjection;
import org.eclipse.mosaic.lib.transform.UtmGeoCalculator;
import org.eclipse.mosaic.rti.TIME;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;

public class AbstractTraciCommandTest {

    @Rule
    public SumoTraciRule traci = new SumoTraciRule(FileUtils.toFile(getClass().getResource("/sumo-test-scenario/scenario.sumocfg")));

    @Rule
    public GeoProjectionRule coordinateTransformationRule = new GeoProjectionRule(
            UtmPoint.eastNorth(UtmZone.from(GeoPoint.lonLat(13.0, 52.0)), 385281.94, 5817994.50)
    );

    protected SimulationSimulateStep simulateStep = new SimulationSimulateStep();

    @Before
    public void simulateBefore() throws Exception {
        GeoProjection.getInstance().setGeoCalculator(new UtmGeoCalculator());
        simulateStep.execute(traci.getTraciConnection(), 5 * TIME.SECOND);
    }
}
