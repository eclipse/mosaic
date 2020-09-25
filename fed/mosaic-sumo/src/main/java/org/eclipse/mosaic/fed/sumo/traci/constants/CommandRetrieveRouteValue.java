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

package org.eclipse.mosaic.fed.sumo.traci.constants;

public class CommandRetrieveRouteValue {

    /**
     * Command for the tracking the given vehicle.
     */
    public final static int COMMAND = 0xa6;

    /**
     * Vehicle Id's running within the scenario.
     */
    public final static int VAR_ID_LIST = 0x00;

    /**
     * The Id's of the edges the vehicle's route is made of.
     */
    public final static int VAR_EDGES = 0x54;
}
