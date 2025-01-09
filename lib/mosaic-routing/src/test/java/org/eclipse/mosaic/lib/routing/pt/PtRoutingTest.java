/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.routing.pt;

import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.junit.GeoProjectionRule;
import org.eclipse.mosaic.lib.routing.config.CPublicTransportRouting;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;

public class PtRoutingTest {

    @Rule
    public GeoProjectionRule transformationRule = new GeoProjectionRule(GeoPoint.latLon(36.9, -116.7));

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private PtRouting ptRouting;
    private CPublicTransportRouting routingConfiguration;
    private File configDir;

    @Before
    public void setup() throws IOException {
        routingConfiguration = new CPublicTransportRouting();
        routingConfiguration.enabled = true;
        routingConfiguration.scheduleDateTime = "2010-01-01T00:00:00";
        routingConfiguration.timeZone = "PST";
        routingConfiguration.osmFile = "pt.osm";
        routingConfiguration.gtfsFile = "pt-gtfs.zip";

        configDir = folder.newFolder("pt");
        final File osmFileCopy = folder.newFile("pt/" + routingConfiguration.osmFile);
        final File gtfsFileCopy = folder.newFile("pt/" + routingConfiguration.gtfsFile);

        FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/pt/pt.osm"), osmFileCopy);
        FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/pt/pt-gtfs.zip"), gtfsFileCopy);

        ptRouting = new PtRouting();
    }

    @Test(expected = IllegalStateException.class)
    public void initialize_skipInitialization() {
        routingConfiguration.enabled = false;
        ptRouting.initialize(routingConfiguration, configDir);

        // ASSERT
        ptRouting.findPtRoute(new PtRoutingRequest(0, GeoPoint.ORIGO, GeoPoint.ORIGO));
    }

    @Test
    public void findRoute_findPublicTransportRoute() {
        ptRouting.initialize(routingConfiguration, configDir);

        PtRoutingResponse response = ptRouting.findPtRoute(new PtRoutingRequest(
                LocalTime.of(8, 57).toNanoOfDay(),
                GeoPoint.latLon(36.900760, -116.766464),
                GeoPoint.latLon(36.907353, -116.761829),
                new PtRoutingParameters().walkingSpeedKmh(3)
        ));

        // ASSERT
        MultiModalRoute route = response.bestRoute();
        assertEquals(3, route.getLegs().size());
        assertEquals(MultiModalLeg.Type.WALKING, route.getLegs().get(0).getLegType());
        assertEquals(MultiModalLeg.Type.PUBLIC_TRANSPORT, route.getLegs().get(1).getLegType());
        assertEquals(MultiModalLeg.Type.WALKING, route.getLegs().get(2).getLegType());
    }

    @Test
    public void findRoute_IWalkFasterThanTheBus() {
        ptRouting.initialize(routingConfiguration, configDir);

        PtRoutingResponse response = ptRouting.findPtRoute(new PtRoutingRequest(
                LocalTime.of(8, 50).toNanoOfDay(),
                GeoPoint.latLon(36.900760, -116.766464),
                GeoPoint.latLon(36.907353, -116.761829),
                new PtRoutingParameters().walkingSpeedKmh(5)
        ));

        // ASSERT
        MultiModalRoute route = response.bestRoute();
        assertEquals(1, route.getLegs().size());
        assertEquals(MultiModalLeg.Type.WALKING, route.getLegs().get(0).getLegType());
    }

}
