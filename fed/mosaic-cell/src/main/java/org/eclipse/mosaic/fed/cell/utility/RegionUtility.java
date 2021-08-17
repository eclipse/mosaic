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

import org.eclipse.mosaic.fed.cell.config.CNetwork;
import org.eclipse.mosaic.fed.cell.config.model.CMobileNetworkProperties;
import org.eclipse.mosaic.fed.cell.config.model.CNetworkProperties;
import org.eclipse.mosaic.fed.cell.data.ConfigurationData;
import org.eclipse.mosaic.fed.cell.data.SimulationData;
import org.eclipse.mosaic.lib.geo.*;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * This class contains static methods to make the region handling easier.
 */
public class RegionUtility {

    private static final Logger log = LoggerFactory.getLogger(RegionUtility.class);

    private static RegionsIndex regionsIndex;

    public static void initializeRegionsIndex(final Collection<CMobileNetworkProperties> regions) {
        regionsIndex = new RegionsIndex(regions);
    }

    /**
     * Gets all nodes in the given region.
     *
     * @param region the region.
     * @return list of all nodes.
     */
    public static List<String> getNodesForRegion(CNetworkProperties region) {
        final List<String> nodesForRegion = new ArrayList<>();
        final Set<String> allNodes = SimulationData.INSTANCE.getAllNodesInSimulation();
        for (String node : allNodes) {
            final String regionForNode = RegionUtility.getRegionForNode(node).id;
            if (regionForNode.equals(region.id)) {
                nodesForRegion.add(node);
            }
        }

        return nodesForRegion;
    }

    /**
     * Convenience function for getting the region of a specified node.
     *
     * @param node the node.
     * @return the region the node is in.
     */
    public static CNetworkProperties getRegionForNode(String node) {
        if (node == null) {

            log.warn("nodeID is null, returning default region");
            return ConfigurationData.INSTANCE.getNetworkConfig().globalNetwork;
        }

        CNetworkProperties region = SimulationData.INSTANCE.getRegionOfNode(node);

        if (region == null) {
            region = getRegionForPosition(SimulationData.INSTANCE.getPositionOfNode(node));
            SimulationData.INSTANCE.setRegionOfNode(node, region);
        }
        log.trace("Getting region for node {} at region {}", node, region.id);
        return region;
    }

    /**
     * Get the region Id for a given Node.
     *
     * @param nodeId Node Id to find the region.
     * @return The region Id of the node.
     * @throws InternalFederateException Exception in case node Id is null.
     */
    public static String getRegionIdForNode(String nodeId) throws InternalFederateException {
        Validate.notNull(nodeId, "Could not get the region id because the nodeId is null");
        final CNetworkProperties nodeRegion = SimulationData.INSTANCE.getRegionOfNode(nodeId);

        if (nodeRegion == null) {
            throw new InternalFederateException("Could not find the region id for node " + nodeId);
        } else {
            log.debug("getRegionIdForNode for node {}", nodeId);
            return nodeRegion.id;
        }
    }

    /**
     * Get the base region for a given cartesian position.
     *
     * @param position Position to find region for.
     * @return Base region.
     */
    public static CNetworkProperties getRegionForPosition(CartesianPoint position) {
        if (position == null) {
            return ConfigurationData.INSTANCE.getNetworkConfig().globalNetwork;
        }

        CNetworkProperties region = regionsIndex.getRegion(position);
        if (region == null) {
            region = ConfigurationData.INSTANCE.getNetworkConfig().globalNetwork;
        }
        return region;
    }

    /**
     * Get all nodes for a destination area (of a geocast message).
     *
     * @param geoArea destination geoArea (GeoCircle or GeoRectangle).
     * @return list of all nodes within the destination area.
     */
    public static List<String> getNodesForDestinationArea(GeoArea geoArea) {
        final CartesianArea area = geoArea.toCartesian();
        List<String> nodes = new ArrayList<>();
        for (String n : SimulationData.INSTANCE.getAllNodesInSimulation()) {
            CartesianPoint nodePos = SimulationData.INSTANCE.getPositionOfNode(n);
            if (nodePos != null && area.contains(nodePos)) {
                nodes.add(n);
            }
        }

        return nodes;
    }

    /**
     * Get all regions for a destination area (of a geocast message).
     *
     * @param geoArea The destination geoArea.
     * @return List of all regions that intersect the destination area.
     */
    public static List<CNetworkProperties> getRegionsForDestinationArea(GeoArea geoArea) {

        List<CNetworkProperties> regions = new ArrayList<>();

        if (geoArea instanceof GeoCircle) {
            for (CMobileNetworkProperties region : ConfigurationData.INSTANCE.getRegionConfig().regions) {
                if (circlePolygonCollision(((GeoCircle) geoArea).toCartesian(), region.getCapoArea())){
                    regions.add(region);
                }
            }
        } else {
            CartesianPolygon destPolygon = ((GeoPolygon) geoArea).toCartesian();
            for (CMobileNetworkProperties region : ConfigurationData.INSTANCE.getRegionConfig().regions) {
                if (region.getCapoArea().isCollidingWithPolygon(destPolygon)) {
                    regions.add(region);
                }
            }
        }

        return regions;
    }

    /**
     * Collision detection for the collision of a circle with a polygon.
     *
     * @param destinationArea The circle.
     * @param regionalArea The polygon.
     * @return true if the circle and the polygon collide.
     */
    private static boolean circlePolygonCollision(CartesianCircle destinationArea, CartesianPolygon regionalArea) {
        // Check if arbitrary point of one area is contained within the other
        if (regionalArea.contains(destinationArea.getCenter()) || destinationArea.contains(regionalArea.getVertices().get(0))) {
            return true;
        }
        // Check if any edge of the regionalArea intersects the circular destinationArea
        CartesianPoint lastPoint = regionalArea.getVertices().get(regionalArea.getVertices().size()-1);
        CartesianPoint circleCenter = destinationArea.getCenter();
        for (CartesianPoint point : regionalArea.getVertices()) {
            double dx = lastPoint.getX() - point.getX();
            double dy = lastPoint.getY() - point.getY();
            double distanceFromCircleCenter =
                    Math.abs(dx * (point.getY() - circleCenter.getY()) - (point.getX() - circleCenter.getX()) * dy)
                            / Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
            if (distanceFromCircleCenter < destinationArea.getRadius()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get a whole region handle for a region name string.
     *
     * @param regionName the string.
     * @return the handle.
     */
    public static CNetworkProperties getRegionByName(String regionName) {
        for (CNetworkProperties region : getAllRegions(true, true)) {
            if (regionName.equals(region.id)) {
                return region;
            }
        }
        return null;
    }

    /**
     * Get the list of all configured regions, optionally including the "globalNetwork".
     *
     * @param includeGlobal include the "globalNetwork", otherwise deliver only AreaRegions.
     * @param includeServers include server "regions"
     * @return list of all regions.
     */
    public static List<CNetworkProperties> getAllRegions(boolean includeGlobal, boolean includeServers) {
        List<CNetworkProperties> regions = new ArrayList<>();
        if (ConfigurationData.INSTANCE.getRegionConfig() != null) {
            regions.addAll(ConfigurationData.INSTANCE.getRegionConfig().regions);
        }

        CNetwork networkConfig = ConfigurationData.INSTANCE.getNetworkConfig();
        if (networkConfig != null) {
            if (includeGlobal) {
                regions.add(networkConfig.globalNetwork);
            }
            if (includeServers && networkConfig.servers != null) {
                regions.addAll(networkConfig.servers);
            }
        }
        return regions;
    }
}