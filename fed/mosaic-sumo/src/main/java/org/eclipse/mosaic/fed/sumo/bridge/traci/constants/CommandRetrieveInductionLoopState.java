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

package org.eclipse.mosaic.fed.sumo.bridge.traci.constants;

public class CommandRetrieveInductionLoopState {

    /**
     * Command for a value of a certain variable of the vehicle.
     */
    public final static int COMMAND = 0xa4;

    /**
     * Vehicle Id's running within the scenario.
     */
    public final static int VAR_ID_LIST = 0x00;

    /**
     * Position of the vehicle.
     */
    public final static int VAR_POSITION = 0x42;

    /**
     * The id of the lane the vehicle was at within the last step.
     */
    public final static int VAR_LANE_ID = 0x51;

    /**
     * The number of vehicles that were on the induction loop within the last simulation step.
     */
    public final static int VAR_LAST_STEP_VEHICLE_NUMBER = 0x10;

    /**
     * The mean speed of vehicles that were on the induction loop within the last simulation step.
     */
    public final static int VAR_LAST_STEP_MEAN_SPEED = 0x11;

    /**
     * The percentage of time the detector was occupied by a vehicle in the last simulation step.
     */
    public final static int VAR_LAST_STEP_OCCUPANCY = 0x13;

    /**
     * The mean length of vehicles which were on the detector in the last step.
     */
    public final static int VAR_LAST_STEP_MEAN_VEHICLE_LENGTH = 0x15;

    /**
     * The time since last detection.
     */
    public final static int VAR_LAST_STEP_TIME_SINCE_LAST_DETECTION = 0x16;

    /**
     * Information about vehicles which passed the detector.
     */
    public final static int VAR_LAST_STEP_VEHICLE_DATA = 0x17;
}
