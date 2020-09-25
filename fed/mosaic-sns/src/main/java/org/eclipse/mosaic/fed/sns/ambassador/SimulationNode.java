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

package org.eclipse.mosaic.fed.sns.ambassador;

import org.eclipse.mosaic.lib.geo.GeoPoint;

/**
 * Aspects of an active simulation entity in SNS (position, communication radius).
 * Note: setters are package private to limit manipulation from outside.
 */
public class SimulationNode {
    /**
     * Position of a node.
     */
    private GeoPoint position;

    /**
     * Transmission radius of a node.
     */
    private double radius;

    public GeoPoint getPosition() {
        return position;
    }

    public double getRadius() {
        return radius;
    }

    void setPosition(GeoPoint position) {
        this.position = position;
    }

    void setRadius(double radius) {
        this.radius = radius;
    }
}
