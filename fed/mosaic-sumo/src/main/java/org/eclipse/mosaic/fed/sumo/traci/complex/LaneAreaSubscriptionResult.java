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

package org.eclipse.mosaic.fed.sumo.traci.complex;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.util.List;

/**
 * This class summarizes the results of the lane area.
 *
 * @see <a href="https://sumo.dlr.de/docs/TraCI/Object_Variable_Subscription.html">Variable Subscription</a>
 */
public class LaneAreaSubscriptionResult extends AbstractSubscriptionResult {

    /**
     * The length of the lane area.
     */
    public double length;

    /**
     * How much vehicles entered into lane area.
     */
    public int vehicleCount;

    @SuppressWarnings(value = {"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"}, justification = "SUMO provides incorrect values for mean speed")
    public double meanSpeed;

    /**
     * The number of halting vehicles in a lane area.
     */
    public int haltingVehicles;

    /**
     * List of vehicles.
     */
    public List<String> vehicles;
}
