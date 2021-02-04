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

package org.eclipse.mosaic.fed.mapping.ambassador.spawning;

import org.eclipse.mosaic.fed.mapping.ambassador.weighting.Weighted;
import org.eclipse.mosaic.fed.mapping.config.units.CTrafficLight;

import org.apache.commons.lang3.ObjectUtils;

/**
 * Class responsible for configuring Traffic Lights to be added to the simulation.
 */
public class TrafficLightSpawner extends UnitSpawner implements Weighted {
    /**
     * The traffic light name.
     */
    private String tlName;
    /**
     * The weight of the traffic light (used for spawning selection).
     */
    private double weight;

    /**
     * Constructor for {@link TrafficLightSpawner}.
     * Calls super constructor and sets additional parameters inferred from
     * json configuration.
     *
     * @param trafficLightConfiguration {@link CTrafficLight} created from json configuration
     */
    public TrafficLightSpawner(CTrafficLight trafficLightConfiguration) {
        super(trafficLightConfiguration.applications, trafficLightConfiguration.name, trafficLightConfiguration.group);
        this.tlName = trafficLightConfiguration.tlGroupId;
        if (this.tlName == null && trafficLightConfiguration.weight == null) {
            this.weight = 1d;
        } else {
            this.weight = ObjectUtils.defaultIfNull(trafficLightConfiguration.weight, 0d);
        }
    }

    public double getWeight() {
        return weight;
    }

    public String getTlName() {
        return tlName;
    }
}
