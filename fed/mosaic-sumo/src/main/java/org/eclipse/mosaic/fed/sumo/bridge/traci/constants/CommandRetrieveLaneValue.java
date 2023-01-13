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

public class CommandRetrieveLaneValue {

    /**
     * Command for the Lane value retrieval.
     */
    public final static int COMMAND = 0xa3;

    /**
     * Command to retrieve the length of a lane.
     */
    public final static int VAR_LENGTH = 0x44;

    /**
     * Command to retrieve the shape of a lane.
     */
    public final static int VAR_SHAPE = 0x4e;
}
