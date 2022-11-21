/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects;

import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.MutableCartesianPoint;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightState;

public class TrafficLightObject extends Vector3d implements SpatialObject {

    /**
     * Manually created traffic light id using group-id_tl-index.
     */
    private final String id;

    private final MutableCartesianPoint cartesianPoint = new MutableCartesianPoint();

    private boolean mapped = false;

    private String trafficLightGroupId;

    private TrafficLightState trafficLightState;

    private String incomingLane;

    private String outgoingLane;

    public TrafficLightObject(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public CartesianPoint getProjectedPosition() {
        return cartesianPoint;
    }

    @Override
    public Vector3d getPosition() {
        return this;
    }

    public void setPosition(double x, double y, double z) {
        this.set(x, y, z);
        cartesianPoint.set(this.toCartesian());
    }

    public TrafficLightObject setPosition(CartesianPoint position) {
        if (!isMapped()) {
            this.cartesianPoint.set(position);
            position.toVector3d(this);
        }
        return this;
    }

    public boolean isMapped() {
        return mapped;
    }

    public void setMapped() {
        mapped = true;
    }

    public String getTrafficLightGroupId() {
        return trafficLightGroupId;
    }

    public TrafficLightObject setTrafficLightGroupId(String trafficLightGroupId) {
        this.trafficLightGroupId = trafficLightGroupId;
        return this;
    }

    public String getIncomingLane() {
        return incomingLane;
    }

    public TrafficLightObject setIncomingLane(String incomingLane) {
        this.incomingLane = incomingLane;
        return this;
    }

    public String getOutgoingLane() {
        return outgoingLane;
    }

    public TrafficLightObject setOutgoingLane(String outgoingLane) {
        this.outgoingLane = outgoingLane;
        return this;
    }

    public TrafficLightState getTrafficLightState() {
        return trafficLightState;
    }

    public TrafficLightObject setTrafficLightState(TrafficLightState trafficLightState) {
        this.trafficLightState = trafficLightState;
        return this;
    }
}