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

public class CommandRetrieveJunctionValue {

    /**
     * Command for the value of a certain variable of the junction.
     */
    public final static int COMMAND = 0xa9;

    /**
     * The position of the named junction.
     */
    public final static int VAR_POSITION = 0x42;
}
