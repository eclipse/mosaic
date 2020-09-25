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
    OFF, // no lane changes except strategic (514 in SUMO)
    CAUTIOUS, //strategic, cooperative, speed changes, keep right, no speed adaption when changing lanes (917 in SUMO)
    COOPERATIVE, //strategic, cooperative, speed changes, keep right, avoid collisions (597 in SUMO)
    AGGRESSIVE, //strategic, no cooperative, speed changes, stay left, do not respect other drivers (17 in SUMO)
    PASSIVE //strategic, no cooperative, speed changes, stay right, do not respect other drivers (661 in SUMO)
}