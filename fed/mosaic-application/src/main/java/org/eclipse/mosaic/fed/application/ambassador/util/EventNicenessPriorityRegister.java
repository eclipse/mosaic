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

package org.eclipse.mosaic.fed.application.ambassador.util;

/**
 * top = highest priority. bottom lowest priority.
 */
public class EventNicenessPriorityRegister {
    // vehicle
    public final static long vehicleAdded = -99_999_900;
    public final static long vehicleUpdated = -99_999_800;
    public final static long vehicleRemoved = -99_999_700;

    // update traffic detectors
    public final static long updateTrafficDetectors = -99_999_600;

    // update charging station
    public final static long updateChargingStation = -99_999_500;
    // update seen traffic signs
    public final static long updateSeenTrafficSign = -99_999_450;
    // update traffic light
    public final static long updateTrafficLight = -99_999_400;


    // v2x messages
    public final static long v2xMessageAcknowledgement = -99_999_200;
    public final static long v2xMessageReception = -99_999_100;
    public final static long v2xFullMessageReception = -99_999_99;
    // charging status
    public final static long chargingRejected = -99_999_000;

    // batteryUpdated
    public final static long batteryUpdated = -99_998_900;
}
