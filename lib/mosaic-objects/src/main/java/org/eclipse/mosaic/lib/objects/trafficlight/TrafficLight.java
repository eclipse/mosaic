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

package org.eclipse.mosaic.lib.objects.trafficlight;

import org.eclipse.mosaic.lib.geo.GeoPoint;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Represents a traffic light (signal) within a traffic light group.
 */
public class TrafficLight implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Signal id (index) within the traffic light group.
     */
    private final int id;

    /**
     * Geo position of a traffic light.
     * Might be equal to a junction geo position that is controlled by a traffic light group
     * this traffic light belongs to.
     */
    private final GeoPoint position;

    /**
     * Incoming lane controlled by the signal.
     */
    private final String incomingLane;

    /**
     * Outgoing lane controlled by the signal.
     */
    private final String outgoingLane;

    private TrafficLightState currentState;

    /**
     * Constructor that initializes the main instance variables.
     *
     * @param id           Signal id within the traffic light group
     * @param position     geo position of the traffic light. Can also be position of the according junction when received from TraCI
     * @param incomingLane an incoming lane controlled by the traffic light
     * @param outgoingLane an outgoing lane controlled by the traffic light
     * @param initialState traffic light state
     */
    public TrafficLight(int id, GeoPoint position, String incomingLane, String outgoingLane, TrafficLightState initialState) {
        this.id = id;
        this.position = position;
        this.incomingLane = incomingLane;
        this.outgoingLane = outgoingLane;
        this.currentState = initialState;
    }

    /**
     * Returns the incoming lane controlled by the signal.
     */
    public String getIncomingLane() {
        return incomingLane;
    }

    /**
     * Returns the outgoing lane controlled by the signal.
     */
    public String getOutgoingLane() {
        return outgoingLane;
    }

    /**
     * Return the signal id (index) within the traffic light group.
     */
    public int getId() {
        return id;
    }

    public GeoPoint getPosition() {
        return position;
    }

    public TrafficLightState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(TrafficLightState currentState) {
        this.currentState = currentState;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 79)
                .append(incomingLane)
                .append(outgoingLane)
                .append(id)
                .append(position)
                .append(currentState)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        TrafficLight other = (TrafficLight) obj;
        return new EqualsBuilder()
                .append(this.incomingLane, other.incomingLane)
                .append(this.outgoingLane, other.outgoingLane)
                .append(this.id, other.id)
                .append(this.position, other.position)
                .append(this.currentState, other.currentState)
                .isEquals();
    }

    @Override
    public String toString() {
        return "TrafficLightSignal{"
                + "id=" + id
                + ", currentState=" + currentState
                + ", incomingLane=" + incomingLane
                + ", outgoingLane=" + outgoingLane
                + ", position=" + position
                + '}';
    }

}
