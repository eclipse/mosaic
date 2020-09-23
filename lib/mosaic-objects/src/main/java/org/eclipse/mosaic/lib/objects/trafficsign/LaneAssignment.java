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

package org.eclipse.mosaic.lib.objects.trafficsign;

import org.eclipse.mosaic.lib.enums.VehicleClass;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stores allowed vehicle classes for a specific lane.
 */
public class LaneAssignment implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The lane the lane assignment is valid for.
     */
    private int lane;

    /**
     * The list of vehicle classes allowed on the lane.
     */
    private List<VehicleClass> allowedVehicleClasses = new ArrayList<>();

    /**
     * Creates a LaneAssignment that allows all vehicle classes on the {@param lane}.
     *
     * @param lane edge lane index
     */
    public LaneAssignment(int lane) {
        this.lane = lane;
        allowedVehicleClasses.addAll(Arrays.asList(VehicleClass.values()));
    }

    /**
     * Creates a LaneAssignment for the {@param lane} with a list of allowed vehicle classes.
     *
     * @param lane edge lane index
     * @param allowedVehicleClasses list of allowed vehicle classes
     */
    public LaneAssignment(int lane, List<VehicleClass> allowedVehicleClasses) {
        this.lane = lane;
        this.allowedVehicleClasses.addAll(allowedVehicleClasses);
    }

    /**
     * Returns the edge lane index.
     */
    public int getLane() {
        return lane;
    }

    /**
     * Returns the list of allowed vehicle classes.
     */
    public List<VehicleClass> getAllowedVehicleClasses() {
        return allowedVehicleClasses;
    }

    /**
     * Checks whether a specific vehicle class is allowed on this lane.
     *
     * @param vehicleClass The vehicle class to be checked.
     * @return true if the {@param vehicleClass} is allowed on the lane
     */
    public boolean isVehicleClassAllowed(VehicleClass vehicleClass) {
        return allowedVehicleClasses.contains(vehicleClass);
    }

    /**
     * Sets the list of allowed {@link VehicleClass}es.
     *
     * @param vehicleClasses list of allowed vehicle classes
     */
    public void setAllowedVehicleClasses(List<VehicleClass> vehicleClasses) {
        this.allowedVehicleClasses = vehicleClasses;
    }

    /**
     * Adds a {@link VehicleClass} to the allowed list.
     *
     * @param vehicleClass the vehicle class to be added to the list of allowed vehicle classes
     */
    public void addAllowedVehicleClass(VehicleClass vehicleClass) {
        allowedVehicleClasses.add(vehicleClass);
    }

    /**
     * Removes a {@link VehicleClass} of the allowed list.
     *
     * @param vehicleClass the vehicle class to be removed from the list of allowed vehicle classes
     * @return true, if the vehicle class was allowed before.
     */
    public boolean removeAllowedVehicleClass(VehicleClass vehicleClass) {
        if (!allowedVehicleClasses.contains(vehicleClass)) {
            return false;
        }
        allowedVehicleClasses.remove(vehicleClass);
        return true;
    }

    /**
     * Blocks the lane(s) for every {@link VehicleClass}.
     */
    public void block() {
        allowedVehicleClasses.clear();
        allowedVehicleClasses.addAll(Arrays.asList(VehicleClass.values()));
    }

    /**
     * Clones this object.
     */
    public LaneAssignment clone() throws CloneNotSupportedException {
        return (LaneAssignment) super.clone();
    }

    /**
     * Opens the lane(s) for every {@link VehicleClass}.
     */
    public void open() {
        allowedVehicleClasses.clear();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("lane", lane)
                .append("allowedVehicleClasses", allowedVehicleClasses)
                .toString();
    }
}
