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

package examples.emergencybrake;

import org.eclipse.mosaic.rti.TIME;

/**
 * Default configuration for the {@link EmergencyBrakeApp}.
 */
public class CEmergencyBrakeApp {
    // Minimal deceleration in m/s^2 for the emergency brake detection
    public float emergencyBrakeThresh = 0.3f;

    // Minimal duration of a deceleration to be detected as emergency break
    public long minimalBrakeDuration = 1 * TIME.SECOND;

    // The deceleration in m/s^2 with which the vehicle slows down in case an obstacle is detected
    public double deceleration = 5d;

    // The speed in m/s the vehicle is trying to reach during slow down in case an obstacle is detected
    public double targetSpeed = 3.0d;

    // Time in seconds after the slow down the vehicle starts accelerating again in case an obstacle is detected
    public long idlePeriod = 8 * TIME.SECOND;
}