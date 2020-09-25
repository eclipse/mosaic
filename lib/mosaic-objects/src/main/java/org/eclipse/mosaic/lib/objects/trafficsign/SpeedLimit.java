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

package org.eclipse.mosaic.lib.objects.trafficsign;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Tells the speed limit for a specific lane.
 */
public class SpeedLimit implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The lane the speed limit is valid for.
     */
    private int lane;

    /**
     * The speed limit of the lane.
     */
    private double speedLimit;

    private Double initialSpeedLimit = null;

    /**
     * Creates a SpeedLimit including a specific lane and its speed limit.
     *
     * @param lane Lane index
     * @param speedLimit speed limit in m/s
     */
    public SpeedLimit(int lane, double speedLimit) {
        this.lane = lane;
        this.speedLimit = speedLimit;
    }

    /**
     * Returns the lane of the speed limit.
     */
    public int getLane() {
        return lane;
    }

    /**
     * Returns the speed limit of the edge.
     */
    public double getSpeedLimit() {
        return speedLimit;
    }

    /**
     * Sets the speed limit for the edge.
     */
    public void setSpeedLimit(double speedLimit) {
        if (speedLimit < 0 && initialSpeedLimit != null) {
            this.speedLimit = initialSpeedLimit;
        } else {
            this.speedLimit = speedLimit;
        }
    }

    public void updateInitialSpeedLimit() {
        this.initialSpeedLimit = speedLimit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("lane", lane)
                .append("speedLimit", speedLimit)
                .toString();
    }

    /**
     * Clones the SpeedLimit object.
     */
    public SpeedLimit clone() throws CloneNotSupportedException {
        return (SpeedLimit) super.clone();
    }
}
