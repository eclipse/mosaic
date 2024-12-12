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

package org.eclipse.mosaic.lib.routing.graphhopper;

import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.routing.EdgeProperties;
import org.eclipse.mosaic.lib.routing.RoutingCostFunction;
import org.eclipse.mosaic.lib.routing.graphhopper.util.GraphhopperToDatabaseMapper;
import org.eclipse.mosaic.lib.routing.graphhopper.util.VehicleEncoding;
import org.eclipse.mosaic.lib.routing.graphhopper.util.WayTypeEncoder;

import com.google.common.collect.Iterables;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import org.apache.commons.lang3.Validate;

import java.util.Optional;

/**
 * Provides properties from the current {@link EdgeIteratorState} or
 * its belonging {@link Connection} to be used by an {@link RoutingCostFunction}.
 */
public class GraphHopperEdgeProperties implements EdgeProperties {

    private final VehicleEncoding encoding;
    private final WayTypeEncoder wayTypeEncoder;
    private final GraphhopperToDatabaseMapper graphMapper;

    private EdgeIteratorState currentEdgeIterator;
    private boolean reverseRequests;

    GraphHopperEdgeProperties(VehicleEncoding encoding, WayTypeEncoder wayTypeEncoder, GraphhopperToDatabaseMapper graphMapper) {
        this.encoding = encoding;
        this.wayTypeEncoder = wayTypeEncoder;
        this.graphMapper = graphMapper;
    }


    void setCurrentEdgeIterator(EdgeIteratorState currentEdgeIterator, boolean reverseRequests) {
        this.currentEdgeIterator = currentEdgeIterator;
        this.reverseRequests = reverseRequests;
    }

    @Override
    public double getSpeed() {
        Validate.notNull(currentEdgeIterator, "Edge iterator is null");
        return reverseRequests
                ? currentEdgeIterator.getReverse(encoding.speed()) / 3.6
                : currentEdgeIterator.get(encoding.speed()) / 3.6;
    }

    @Override
    public double getLength() {
        Validate.notNull(currentEdgeIterator, "Edge iterator is null");
        return currentEdgeIterator.getDistance();
    }

    @Override
    public Iterable<GeoPoint> getGeometry() {
        Validate.notNull(currentEdgeIterator, "Edge iterator is null");
        return Iterables.transform(
                currentEdgeIterator.fetchWayGeometry(FetchMode.ALL), // fetches all pillar nodes inclusive the base and adjacent tower node
                ghPoint3D -> GeoPoint.latLon(ghPoint3D.getLat(), ghPoint3D.getLon(), ghPoint3D.getEle())
        );
    }

    @Override
    public String getConnectionId() {
        return getConnection().map(Connection::getId).orElse(null);
    }

    @Override
    public String getWayType() {
        return WayTypeEncoder.decode(getWayTypeEncoded());
    }

    public int getWayTypeEncoded() {
        return currentEdgeIterator.get(wayTypeEncoder);
    }

    private Optional<Connection> getConnection() {
        Validate.notNull(currentEdgeIterator, "Edge iterator is null");
        return Optional.ofNullable(graphMapper.toConnection(currentEdgeIterator.getEdge()));

    }
}
