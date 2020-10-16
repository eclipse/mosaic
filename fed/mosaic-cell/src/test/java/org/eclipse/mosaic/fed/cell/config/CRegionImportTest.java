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

package org.eclipse.mosaic.fed.cell.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.fed.cell.config.model.CMobileNetworkProperties;
import org.eclipse.mosaic.fed.cell.config.util.ConfigurationReader;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.UtmPoint;
import org.eclipse.mosaic.lib.geo.UtmZone;
import org.eclipse.mosaic.lib.junit.GeoProjectionRule;
import org.eclipse.mosaic.lib.model.delay.ConstantDelay;
import org.eclipse.mosaic.lib.model.delay.GammaRandomDelay;
import org.eclipse.mosaic.lib.model.delay.SimpleRandomDelay;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.junit.Rule;
import org.junit.Test;

import java.io.File;

/**
 * Test validating proper reading of region configuration.
 */
public class CRegionImportTest {

    @Rule
    public GeoProjectionRule transformationRule = new GeoProjectionRule(
            UtmPoint.eastNorth(UtmZone.from(GeoPoint.lonLat(13.3856, 52.5415)), 388405.53, 5820063.64)
    );

    private final static String REGION_CONF_PATH =
            "src" + File.separator + "test" + File.separator
                    + "resources" + File.separator + "configs" + File.separator + "sample_regions.json";
    private final static String REGION_CONF_PATH_INVALID =
            "src" + File.separator + "test" + File.separator
                    + "resources" + File.separator + "configs" + File.separator + "sample_regions_invalid.json";

    private static final double EPSILON = 1e-5;

    @SuppressWarnings("Redundant local variable")
    private CRegion getRegionConfig() throws InternalFederateException {
        // Read the region configuration file
        return ConfigurationReader.importRegionConfig(REGION_CONF_PATH);
    }

    @Test(expected = InternalFederateException.class)
    public void checkInvalidRegionConfig() throws InternalFederateException {
        ConfigurationReader.importRegionConfig(REGION_CONF_PATH_INVALID);
    }

    @Test
    public void checkRegionConfigAsExpected() throws InternalFederateException {
        CRegion regionConfig = getRegionConfig();
        assertNotNull(regionConfig.regions);
        assertEquals(3, regionConfig.regions.size());
        final CMobileNetworkProperties region = regionConfig.regions.get(0);
        assertEquals(52.6, region.area.getA().getLatitude(), EPSILON);
        assertEquals(13.6, region.area.getA().getLongitude(), EPSILON);
        assertEquals(52.5, region.area.getB().getLatitude(), EPSILON);
        assertEquals(13.7, region.area.getB().getLongitude(), EPSILON);
        assertEquals("Some region", region.id);
        final CMobileNetworkProperties region2 = regionConfig.regions.get(1);
        assertNotNull(region2);
        assertEquals("Another region", region2.id);

        if (region.uplink.delay instanceof ConstantDelay) {
            final ConstantDelay uplinkDelay = (ConstantDelay) region.uplink.delay;

            assertNotNull(uplinkDelay);
            assertEquals(200 * TIME.MILLI_SECOND, uplinkDelay.delay, EPSILON);
        }

        assertEquals(0.5, region.uplink.transmission.lossProbability, EPSILON);
        assertEquals(2, region.uplink.transmission.maxRetries);

        assertNotNull(region.downlink.multicast.delay);
        assertTrue(region.downlink.multicast.delay instanceof GammaRandomDelay);
        final GammaRandomDelay downlinkMulticastDelay = (GammaRandomDelay) region.downlink.multicast.delay;
        assertEquals(100 * TIME.MILLI_SECOND, downlinkMulticastDelay.minDelay, EPSILON);
        assertEquals(200 * TIME.MILLI_SECOND, downlinkMulticastDelay.expDelay, EPSILON);

        assertEquals(0.5, region.downlink.multicast.transmission.lossProbability, EPSILON);

        assertNotNull(region.downlink.unicast.delay);
        assertTrue(region.downlink.unicast.delay instanceof SimpleRandomDelay);
        final SimpleRandomDelay downlinkUnicastDelay = (SimpleRandomDelay) region.downlink.unicast.delay;
        assertEquals(5, downlinkUnicastDelay.steps, EPSILON);
        assertEquals(100 * TIME.MILLI_SECOND, downlinkUnicastDelay.minDelay, EPSILON);
        assertEquals(200 * TIME.MILLI_SECOND, downlinkUnicastDelay.maxDelay, EPSILON);

        assertEquals(0.1, region.downlink.unicast.transmission.lossProbability, EPSILON);
        assertEquals(2, region.downlink.unicast.transmission.maxRetries);
    }
}
