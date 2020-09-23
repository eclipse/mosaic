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

public class CommandChangeTrafficLightState {

    /**
     * Change the state of a traffic light.
     */
    public final static int COMMAND = 0xc2;

    /**
     * Traffic light states.
     */
    public final static int VAR_STATE = 0x20;

    /**
     * Index of the phase.
     */
    public final static int VAR_PHASE_INDEX = 0x22;

    /**
     * Id of the traffic light program.
     */
    public final static int VAR_PROGRAM_ID = 0x23;

    /**
     * Phase duration.
     */
    public final static int VAR_PHASE_DURATION = 0x24;

    /**
     * Complete program definition.
     */
    public final static int VAR_COMPLETE_PROGRAM = 0x2c;
}

