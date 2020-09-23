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

package org.eclipse.mosaic.lib.enums;

public enum SpeedMode {

    DEFAULT, // == CAUTIOUS
    CAUTIOUS, // regards safe speed, max decel, right of way at intersections, brakes hard at red light (31 in SUMO)
    NORMAL, // does not brakes hard for avoiding red lights (15 in SUMO)
    AGGRESSIVE, // does not regard safe speed, right of way at intersections, and does not brakes hard for avoiding red lights (6 in SUMO)
    SPEEDER // DEFAULT, but without respect to speed limits
}
