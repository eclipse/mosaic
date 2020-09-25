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

package org.eclipse.mosaic.lib.database.route;

import org.eclipse.mosaic.lib.database.road.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * This is a complete route that can be driven by a vehicle.
 */
public class Route {

    /**
     * The internal id for this route.
     */
    private final String id;

    /**
     * The list of edges the vehicles using this route have to drive.
     * Each entry has the form connectionid_startnode
     */
    private final List<Edge> edgeList = new ArrayList<>();

    /**
     * Default constructor.
     *
     * @param id Id of the route.
     */
    public Route(@Nonnull String id) {
        this.id = Objects.requireNonNull(id);
    }


    /**
     * The id of this route.
     *
     * @return Id of the route.
     */
    @Nonnull
    public String getId() {
        return id;
    }

    /**
     * This is the list of {@link Edge}s that form the route.
     * The entries have the form connectionid_startnode
     *
     * @return list of edges
     */
    @Nonnull
    public List<Edge> getRoute() {
        return Collections.unmodifiableList(edgeList);
    }

    /**
     * This extracts a list of edge IDs.
     *
     * @return Extracted list of edge Ids.
     */
    @Nonnull
    public List<String> getEdgeIdList() {
        return edgeList.stream().map(Edge::getId).collect(Collectors.toList());
    }

    /**
     * Add an {@link Edge} to the route. Mind the order of edges.
     *
     * @param edge Edge to add.
     */
    public void addEdge(@Nonnull Edge edge) {
        if (edgeList.size() > 0 && edgeList.get(edgeList.size() - 1).getId().equals(edge.getId())) {
            edgeList.remove(edgeList.size() - 1);
        }
        edgeList.add(Objects.requireNonNull(edge));
    }

    /**
     * This extracts a list of {@link Node}s that vehicles using this {@link Route} are passing.
     *
     * @return Extracted nodes.
     */
    @Nonnull
    public List<Node> getNodeList() {
        if (edgeList.isEmpty()) {
            return new ArrayList<>();
        } else {
            List<Node> nodes = edgeList.stream().map(Edge::getFromNode).collect(Collectors.toList());
            nodes.add(edgeList.get(edgeList.size() - 1).getToNode());
            return nodes;
        }
    }

    /**
     * This extracts a list of all node IDs this {@link Route} passes.
     * So ALL the edges nodes, even those between intersections where a route could change!
     *
     * @return Extracted list of all node IDs.
     */
    @Nonnull
    public List<String> getNodeIdList() {
        return Collections.unmodifiableList(getNodeList().stream().map(Node::getId).collect(Collectors.toList()));
    }

    /**
     * This extracts a list of connection IDs. Multiple adjacent edges belonging to
     * the same connection will result in only one occurrence of the connection !
     *
     * @return Extracted list of connection Ids.
     */
    @Nonnull
    public List<String> getConnectionIdList() {
        final LinkedList<String> result = new LinkedList<>();
        for (Edge edge : edgeList) {
            if (result.isEmpty() || !result.getLast().equals(edge.getConnection().getId())) {
                result.add(edge.getConnection().getId());
            }
        }
        return result;
    }

}
