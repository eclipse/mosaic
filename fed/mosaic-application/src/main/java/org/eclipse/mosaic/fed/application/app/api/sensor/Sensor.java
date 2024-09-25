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

import java.util.function.Consumer;

public interface Sensor<T> {

    /**
     * Enables this specific sensor. All sensors are disabled by default.
     */
    void enable();

    /**
     * Disables this specific sensor.
     */
    void disable();

    /**
     * Returns the most recent sensor data.
     */
    T getSensorData();

    /**
     * Registers a {@link Consumer} which is called with the most recent sensor data object
     * as soon as it is published to this sensor.
     */
    void reactOnSensorDataUpdate(Consumer<T> callback);
}