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

package org.eclipse.mosaic.interactions.trafficsigns;

import org.eclipse.mosaic.lib.objects.trafficsign.TrafficSign;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This extension of {@link Interaction} stores a map of all traffic signs which are in sight
 * distance of a specific vehicle and a map of all traffic signs which became invalid for that vehicle.
 */
public final class VehicleSeenTrafficSignsUpdate extends Interaction {

    public final static String TYPE_ID = createTypeIdentifier(VehicleSeenTrafficSignsUpdate.class);

    /**
     * All traffic signs in sight distance of any vehicle.
     */
    private final Map<String, List<TrafficSign>> newSigns = new HashMap<>();

    /**
     * All traffic signs that became invalid for any vehicle.
     */
    private final Map<String, List<TrafficSign>> passedSigns = new HashMap<>();

    /**
     * Creates a new {@link VehicleSeenTrafficSignsUpdate} interaction without any updates.
     * The traffic sign updates need to be added with {@link #addNewSign(String, TrafficSign)}
     * and {@link #addPassedSign(String, TrafficSign)}.
     *
     * @param time Timestamp of this interaction, unit: [ns]
     */
    public VehicleSeenTrafficSignsUpdate(long time) {
        super(time);
    }

    /**
     * Adds a new {@param trafficSign} which is in sight area of vehicle with {@param vehicleId}.
     *
     * @param vehicleId   The vehicle identifier.
     * @param trafficSign The traffic sign.
     */
    public void addNewSign(String vehicleId, TrafficSign trafficSign) {
        if (newSigns.containsKey(vehicleId)) {
            newSigns.get(vehicleId).add(trafficSign);
        } else {
            ArrayList<TrafficSign> list = new ArrayList<>();
            list.add(trafficSign);
            newSigns.put(vehicleId, list);
        }
    }

    /**
     * Adds a {@param trafficSign} which has become invalid for vehicle with {@param vehicleId}.
     *
     * @param vehicleId   The vehicle identifier.
     * @param trafficSign The traffic sign.
     */
    public void addPassedSign(String vehicleId, TrafficSign trafficSign) {
        if (passedSigns.containsKey(vehicleId)) {
            passedSigns.get(vehicleId).add(trafficSign);
        } else {
            ArrayList<TrafficSign> list = new ArrayList<>();
            list.add(trafficSign);
            passedSigns.put(vehicleId, list);
        }
    }

    /**
     * Returns a list of all receiver vehicles.
     *
     * @return list of all receiver vehicles
     */
    public List<String> getAllRecipients() {
        Set<String> recipients = new HashSet<>();
        recipients.addAll(newSigns.keySet());
        recipients.addAll(passedSigns.keySet());
        return new ArrayList<>(recipients);
    }

    /**
     * Returns the list of new traffic signs of which vehicle with {@param vehicleId} is recipient.
     *
     * @param vehicleId vehicle identifier
     * @return list of all traffic signs that just became valid for {@param vehicleId}
     */
    public List<TrafficSign> getNewSigns(String vehicleId) {
        return newSigns.getOrDefault(vehicleId, new ArrayList<>());
    }

    /**
     * Returns the list of traffic signs that became invalid of which vehicle with {@param vehicleId} is recipient.
     *
     * @param vehicleId vehicle identifier
     * @return list of all traffic signs that just became invalid for {@param vehicleId}
     */
    public List<TrafficSign> getPassedSigns(String vehicleId) {
        return passedSigns.getOrDefault(vehicleId, new ArrayList<>());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 71)
                .append(newSigns)
                .append(passedSigns)
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

        VehicleSeenTrafficSignsUpdate other = (VehicleSeenTrafficSignsUpdate) obj;
        return new EqualsBuilder()
                .append(this.newSigns, other.newSigns)
                .append(this.passedSigns, other.passedSigns)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("newSigns", newSigns)
                .append("passedSigns", passedSigns)
                .toString();
    }
}
