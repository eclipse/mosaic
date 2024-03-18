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

package org.eclipse.mosaic.lib.enums;

public enum LaneChangeMode {
    DEFAULT, // == COOPERATIVE
    OFF, // no lane changes at all, only by explicit calls of changeLane
    FOLLOW_ROUTE, // no lane changes except strategic
    CAUTIOUS, //strategic, cooperative, speed changes, keep right, no speed adaption when changing lanes
    COOPERATIVE, //strategic, cooperative, speed changes, keep right, avoid collisions
    AGGRESSIVE, //strategic, no cooperative, speed changes, stay left, do not respect other drivers
    PASSIVE //strategic, no cooperative, speed changes, stay right, do not respect other drivers
}