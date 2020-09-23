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

package org.eclipse.mosaic.fed.sumo.traci.constants;

public class CommandRetrieveLaneAreaState {

    /**
     * Command for a value of a certain variable of the vehicle.
     */
    public final static int COMMAND = 0xa4;
    // public final static int VAR_COUNT = 0x01;

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
     * The number of halting vehicles in the last simulation step.
     */
    public final static int VAR_LAST_STEP_HALTING_VEHICLE_NUMBER = 0x14;
    // public final static int VAR_LAST_STEP_LENGTH_OF_JAM_IN_NUMBER_OF_VEHICLES = 0x18;
    // public final static int VAR_LAST_STEP_LENGTH_OF_JAM_IN_METERS = 0x19;

    /**
     * The length of the vehicles.
     */
    public final static int VAR_LENGTH = 0x44;

    /**
     * The Id of the lane the vehicle was at within the last step.
     */
    public final static int VAR_LANE_ID = 0x51;
    // public final static int VAR_POSITION = 0x42;
    // public final static int VAR_ID_LIST = 0x00;

    /**
     * The Id's of the vehicles stopped in the last simulation step.
     */
    public final static int VAR_LAST_STEP_VEHICLE_IDS = 0x12;
}
