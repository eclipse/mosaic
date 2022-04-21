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

package org.eclipse.mosaic.fed.sumo.ambassador;

import org.apache.commons.lang3.StringUtils;

/**
 * Interface containing string constants that are used ofter when logging
 * information. Using constants improves the performance.
 */
abstract class LogStatements {

    static String VEHICLE_SLOWDOWN_REQ = "Receive VehicleSlowDown request";

    static String VEHICLE_SPEED_CHANGE_REQ = "Receive VehicleSpeedChange request";

    static String VEHICLE_ROUTE_CHANGE_REQ = "Receive VehicleRouteChange request";

    static String VEHICLE_LANE_CHANGE_REQ = "Receive VehicleLaneChange request";

    static String VEHICLE_PARAM_CHANGE_REQ = "Receive VehicleParameterChange request";

    static String VEHICLE_SIGHT_DISTANCE_REQ = "Receive VehicleSightDistanceConfiguration request";

    static String TRAFFIC_LIGHTS_STATE_CHANGE_REQ = "Receive TrafficLightStateChange request";

    static String TRAFFIC_LIGHT_SUBSCRIPTION = "TrafficLightSubscription interaction received";

    static String UNKNOWN_INTERACTION = "Unknown Interaction received: ";

    static String SIM_TRAFFIC = "Simulate traffic until {}";

    static String SUMO_TRACI_BYTE_ARRAY_MESSAGE = "SUMO TraCI byte array message received";

    static String VEHICLE_STOP_REQ = "Receive VehicleStop request";

    static String VEHICLE_RESUME_REQ = "Receive VehicleResume request";

    static String INDUCTION_LOOP_DETECTOR_SUBSCRIPTION = "InductionLoopDetectorSubscription Interaction received";

    static String LANE_AREA_DETECTOR_SUBSCRIPTION = "LaneAreaDetectorSubscription Interaction received";

    static String LANE_PROPERTY_CHANGE = "LanePropertyChange Interaction received";

    static String MISSING_SUMO_CONFIG = "The SUMO Ambassador cannot be started without a valid configuration file (*.sumocfg is missing).";

    /**
     * Prints SUMO start message to the console.
     */
    static void printStartSumoGuiInfo() {
        System.out.println(StringUtils.repeat("=", 70));
        System.out.println("| SUMO will now be started in GUI Mode." + StringUtils.repeat(" ", 30) + "|");
        System.out.println("| Please make sure you have started Eclipse MOSAIC with \"-w 0\"" + StringUtils.repeat(" ", 7) + "|");
        System.out.println("| Start the simulation manually in SUMO-GUI." + StringUtils.repeat(" ", 25) + "|");
        System.out.println(StringUtils.repeat("=", 70));
    }
}
