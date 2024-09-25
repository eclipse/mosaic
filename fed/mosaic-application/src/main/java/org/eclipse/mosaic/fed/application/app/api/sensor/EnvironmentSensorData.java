/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.application.app.api.sensor;

import org.eclipse.mosaic.lib.enums.SensorType;

public interface EnvironmentSensorData {

    /**
     * Returns the state (as strength) of the supplied environment {@link SensorType}.
     *
     * @param sensorType The {@link SensorType} type to use.
     * @return Strength of the measured environment sensor data.
     */
    int strengthOf(SensorType sensorType);
}