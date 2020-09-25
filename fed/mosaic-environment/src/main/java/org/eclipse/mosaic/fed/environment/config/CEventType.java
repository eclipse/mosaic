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

package org.eclipse.mosaic.fed.environment.config;

import org.eclipse.mosaic.lib.enums.SensorType;

import java.io.Serializable;

public class CEventType implements Serializable {

    /**
     * This represents the type of sensor value this event
     * is emitting (e.g. Ice, Snow, or an arbitrary Obstacle)
     */
    public SensorType sensorType;

    /**
     * This is a value used for assigning a value to the event,
     * it can be used as the strength of an event, or the
     * amount of free parking spots in a parking lot, etc.
     */
    public int value = 1;
}
