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

package org.eclipse.mosaic.lib.routing.graphhopper.extended;

import org.eclipse.mosaic.lib.routing.graphhopper.algorithm.RoutingAlgorithmFactory;

import com.graphhopper.GHRequest;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.shapes.GHPoint;

/**
 * Extends GHRequest by providing instances of weighting and algorithms preparation instead of
 * defining string constants. Additionally the number of alternative routes can be set in this
 * request.
 *
 */
public class ExtendedGHRequest extends GHRequest {

    protected WeightingFactory weightingFactory;
    protected int alternatives;
    protected RoutingAlgorithmFactory routingAlgorithmFactory;

    public ExtendedGHRequest(GHPoint from, GHPoint to) {
        super(from, to);
        this.routingAlgorithmFactory = RoutingAlgorithmFactory.CHOICE_ROUTING_DIJKSTRA;
    }

    public ExtendedGHRequest setWeightingFactory(WeightingFactory w) {
        this.weightingFactory = w;
        return this;
    }

    public WeightingFactory getWeightingFactory() {
        return weightingFactory;
    }

    public Weighting getWeightingInstance(Graph graph) {
        return weightingFactory.create(graph);
    }

    public int getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(int alternatives) {
        this.alternatives = alternatives;

    }

    public RoutingAlgorithmFactory getAlgorithmFactory() {
        return routingAlgorithmFactory;
    }

    public void setRoutingAlgorithmFactory(RoutingAlgorithmFactory routingAlgorithmFactory) {
        this.routingAlgorithmFactory = routingAlgorithmFactory;
    }
    
    public interface WeightingFactory {
        Weighting create(Graph graph);
    }
}
