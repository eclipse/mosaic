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

import static org.eclipse.mosaic.fed.cell.config.model.CNetworkProperties.GLOBAL_NETWORK_ID;
import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.fed.cell.config.model.CMobileNetworkProperties;
import org.eclipse.mosaic.fed.cell.config.model.CNetworkProperties;
import org.eclipse.mosaic.fed.cell.data.ConfigurationData;
import org.eclipse.mosaic.fed.cell.data.SimulationData;
import org.eclipse.mosaic.fed.cell.junit.CellConfigurationRule;
import org.eclipse.mosaic.fed.cell.junit.CellSimulationRule;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.geo.GeoPolygon;
import org.eclipse.mosaic.lib.geo.MutableGeoPoint;
import org.eclipse.mosaic.lib.geo.UtmPoint;
import org.eclipse.mosaic.lib.geo.UtmZone;
import org.eclipse.mosaic.lib.junit.GeoProjectionRule;
import org.eclipse.mosaic.lib.transform.GeoProjection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that tests methods of the RegionUtility of the cell.
 */
public class RegionUtilityTest {

    private final CellConfigurationRule configRule = new CellConfigurationRule()
            .withNetworkConfig("configs/sample_network.json")
            .withRegionConfig("configs/sample_regions.json");

    private final GeoProjectionRule transformationRule = new GeoProjectionRule(
            UtmPoint.eastNorth(UtmZone.from(GeoPoint.lonLat(13, 52)), 388405.53, 5820063.64)
    );

    @Rule
    public RuleChain ruleChain = RuleChain.outerRule(transformationRule).around(configRule);

    @Rule
    public CellSimulationRule simulationRule = new CellSimulationRule();

    /**
     * Tests if the right region is returned for an area.
     */
    @Test
    public void testGetRegionForArea() {
        CMobileNetworkProperties sampleRegion = ConfigurationData.INSTANCE.getRegionConfig().regions.get(2);

        // Test cirlce-polygon collision
        GeoCircle geoC1 = new GeoCircle(new MutableGeoPoint(52.56,13.33), 10000);
        GeoCircle geoC2 = new GeoCircle(new MutableGeoPoint(52.40,13.33), 100);
        List<CNetworkProperties> regions = RegionUtility.getRegionsForDestinationArea(geoC1);
        assertEquals(1, regions.size());
        assertEquals(sampleRegion.id, regions.get(0).id);
        regions = RegionUtility.getRegionsForDestinationArea(geoC2);
        assertEquals(0, regions.size());

        // Test polygon-polygon collision
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(new MutableGeoPoint(52.53,13.31));
        geoPoints.add(new MutableGeoPoint(52.56, 13.34));
        geoPoints.add(new MutableGeoPoint(53.56,13.31));
        GeoPolygon geoP1 = new GeoPolygon(geoPoints);
        regions = RegionUtility.getRegionsForDestinationArea(geoP1);
        assertEquals(1, regions.size());
        assertEquals(sampleRegion.id, regions.get(0).id);
    }

    /**
     * Tests if the right region is returned for a node.
     */
    @Test
    public void testGetRegionForNode() {
        double longitude1 = 13.6;
        double latitude1 = 52.6;
        double longitudeDelta1 = 0.01;
        double latitudeDelta1 = -0.01;

        double longitude2 = 13.62;
        double latitude2 = 52.59;
        double longitudeDelta2 = 0.02;
        double latitudeDelta2 = -0.02;

        double longitude3 = 13.32;
        double latitude3 = 52.55;
        double longitudeDelta3 = 0.02;
        double latitudeDelta3 = -0.02;


        GeoPoint nodeOnBoundary1 = GeoPoint.lonLat(longitude1, latitude1);
        GeoPoint nodeWithinBoundary1 = GeoPoint.lonLat(longitude1 + longitudeDelta1, latitude1 + latitudeDelta1);
        GeoPoint nodeOutsideBoundary1 = GeoPoint.lonLat(longitude1 - longitudeDelta1, latitude1 - latitudeDelta1);

        GeoPoint nodeOnBoundary2 = GeoPoint.lonLat(longitude2, latitude2);
        GeoPoint nodeWithinBoundary2 = GeoPoint.lonLat(longitude2 + longitudeDelta2, latitude2 + latitudeDelta2);
        GeoPoint nodeOutsideBoundary2 = GeoPoint.lonLat(longitude2 - longitudeDelta2, latitude2 - latitudeDelta2);

        GeoPoint nodeOnBoundary3 = GeoPoint.lonLat(longitude3, latitude3);
        GeoPoint nodeWithinBoundary3 = GeoPoint.lonLat(longitude3 + longitudeDelta3, latitude3 + latitudeDelta3);
        GeoPoint nodeOutsideBoundary3 = GeoPoint.lonLat(longitude3 - longitudeDelta3, latitude3 - latitudeDelta3);

        SimulationData.INSTANCE.setPositionOfNode("nodeOnBoundary1", GeoProjection.getInstance().geographicToCartesian(nodeOnBoundary1));
        SimulationData.INSTANCE.setPositionOfNode(
                "nodeWithinBoundary1",
                GeoProjection.getInstance().geographicToCartesian(nodeWithinBoundary1)
        );
        SimulationData.INSTANCE.setPositionOfNode(
                "nodeOutsideBoundary1",
                GeoProjection.getInstance().geographicToCartesian(nodeOutsideBoundary1)
        );

        SimulationData.INSTANCE.setPositionOfNode("nodeOnBoundary2", GeoProjection.getInstance().geographicToCartesian(nodeOnBoundary2));
        SimulationData.INSTANCE.setPositionOfNode(
                "nodeWithinBoundary2",
                GeoProjection.getInstance().geographicToCartesian(nodeWithinBoundary2)
        );
        SimulationData.INSTANCE.setPositionOfNode(
                "nodeOutsideBoundary2",
                GeoProjection.getInstance().geographicToCartesian(nodeOutsideBoundary2)
        );

        SimulationData.INSTANCE.setPositionOfNode("nodeOnBoundary3", GeoProjection.getInstance().geographicToCartesian(nodeOnBoundary3));
        SimulationData.INSTANCE.setPositionOfNode(
                "nodeWithinBoundary3",
                GeoProjection.getInstance().geographicToCartesian(nodeWithinBoundary3)
        );
        SimulationData.INSTANCE.setPositionOfNode(
                "nodeOutsideBoundary3",
                GeoProjection.getInstance().geographicToCartesian(nodeOutsideBoundary3)
        );

        // ASSERT
        CMobileNetworkProperties sampleRegion1 = ConfigurationData.INSTANCE.getRegionConfig().regions.get(0);
        CMobileNetworkProperties sampleRegion2 = ConfigurationData.INSTANCE.getRegionConfig().regions.get(1);
        CMobileNetworkProperties sampleRegion3 = ConfigurationData.INSTANCE.getRegionConfig().regions.get(2);

        assertEquals(sampleRegion1.id, RegionUtility.getRegionForNode("nodeOnBoundary1").id);
        // assertEquals(sampleRegion1.id, RegionUtility.getRegionForNode("nodeWithinBoundary1").id); // FIXME
        assertEquals(GLOBAL_NETWORK_ID, RegionUtility.getRegionForNode("nodeOutsideBoundary1").id);

        assertEquals(sampleRegion2.id, RegionUtility.getRegionForNode("nodeOnBoundary2").id);
        assertEquals(sampleRegion2.id, RegionUtility.getRegionForNode("nodeWithinBoundary2").id);
        assertEquals(GLOBAL_NETWORK_ID, RegionUtility.getRegionForNode("nodeOutsideBoundary2").id);

        assertEquals(sampleRegion3.id, RegionUtility.getRegionForNode("nodeOnBoundary3").id);
        assertEquals(sampleRegion3.id, RegionUtility.getRegionForNode("nodeWithinBoundary3").id);
        assertEquals(GLOBAL_NETWORK_ID, RegionUtility.getRegionForNode("nodeOutsideBoundary3").id);
    }
}
