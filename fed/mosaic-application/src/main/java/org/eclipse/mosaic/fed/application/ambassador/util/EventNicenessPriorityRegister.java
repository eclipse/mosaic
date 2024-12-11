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
    public final static long UNIT_REMOVED = 1;
    // vehicle
    public final static long VEHICLE_ADDED = -99_999_900;
    public final static long VEHICLE_UPDATED = -99_999_800;
    public final static long VEHICLE_REMOVED = -99_999_700;
    public final static long LIDAR_UPDATED = -99_999_650;

    // update traffic detectors
    public final static long UPDATE_TRAFFIC_DETECTORS = -99_999_600;

    // update charging station
    public final static long UPDATE_CHARGING_STATION = -99_999_500;
    // update seen traffic signs
    public final static long UPDATE_SEEN_TRAFFIC_SIGN = -99_999_450;
    // update traffic light
    public final static long UPDATE_TRAFFIC_LIGHT = -99_999_400;


    // v2x messages
    public final static long V2X_MESSAGE_ACKNOWLEDGEMENT = -99_999_200;
    public final static long V2X_MESSAGE_RECEPTION = -99_999_100;
    public final static long V2X_FULL_MESSAGE_RECEPTION = -99_999_99;
    // charging status
    public final static long CHARGING_REJECTED = -99_999_000;

    // batteryUpdated
    public final static long BATTERY_UPDATED = -99_998_900;

    // discover charging stations
    public final static long CHARGING_STATIONS_DISCOVERY = -99_998_900;
}
