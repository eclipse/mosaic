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

/**
 * A basic sensor module which provides single integer values for a given {@link SensorType}.
 */
public interface BasicSensorModule {

    /**
     * Enables this basic sensor module.
     */
    void enable();

    /**
     * @return {@code true}, if this module has been enabled.
     */
    boolean isEnabled();

    /**
     * Disables this basic sensor module. {@link #getStrengthOf(SensorType)} will always return 0.
     */
    void disable();

    /**
     * Returns the state (as strength) of the supplied environment {@link SensorType}.
     *
     * @param sensorType The {@link SensorType} type to use.
     * @return Strength of the measured environment sensor data.
     */
    int getStrengthOf(SensorType sensorType);
}
