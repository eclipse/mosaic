/*
 * Copyright (c) 2023 Fraunhofer FOKUS and others. All rights reserved.
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

public class CommandRetrieveVehicleTypeState {

    public final static int COMMAND = 0xa5;

    public final static SumoVar VAR_MIN_GAP = SumoVar.var(0x4c);

    public final static SumoVar VAR_MAX_SPEED = SumoVar.var(0x41);

    public final static SumoVar VAR_ACCEL = SumoVar.var(0x46);

    public final static SumoVar VAR_DECEL = SumoVar.var(0x47);

    public final static SumoVar VAR_TAU = SumoVar.var(0x48);

    public final static SumoVar VAR_SIGMA = SumoVar.var(0x5d);

    public final static SumoVar VAR_SPEED_FACTOR = SumoVar.var(0x5e);

    public final static SumoVar VAR_VCLASS = SumoVar.var(0x49);

    public final static SumoVar VAR_LENGTH = SumoVar.var(0x44);

    public final static SumoVar VAR_WIDTH = SumoVar.var(0x4d);

    public final static SumoVar VAR_HEIGHT = SumoVar.var(0xbc);
}

