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

package org.eclipse.mosaic.fed.sumo.bridge.api.complex;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Vehicle data saved by an induction loop.
 * (cf. {@link org.eclipse.mosaic.fed.sumo.bridge.api.complex.InductionLoopSubscriptionResult})
 */
@SuppressWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
public class InductionLoopVehicleData {

    /**
     * Id of the vehicle.
     */
    public String vehicleId;

    /**
     * The entry time to the induction time.
     */
    public long entryTime;

    /**
     * The time at which the vehicle leaves the induction loop.
     */
    public long leaveTime;
}
