/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.sumo.bridge.api.complex;

import java.util.List;

/**
 * This class summarizes the results of induction loop.
 *
 * @see <a href="https://sumo.dlr.de/docs/TraCI/Object_Variable_Subscription.html">Variable Subscription</a>
 */
public class InductionLoopSubscriptionResult extends AbstractSubscriptionResult {

    /**
     * The mean speed of a vehicle in an induction loop.
     */
    public double meanSpeed;

    /**
     * The mean vehicle length of all vehicles in an induction loop.
     */
    public double meanVehicleLength;

    /**
     * The general vehicle information (Vehicle-ID, entry time to induction loop and leave time).
     */
    public List<InductionLoopVehicleData> vehiclesOnInductionLoop;
}
