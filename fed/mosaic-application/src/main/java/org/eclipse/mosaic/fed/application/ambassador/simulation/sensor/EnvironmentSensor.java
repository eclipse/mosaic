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

package org.eclipse.mosaic.fed.application.ambassador.simulation.sensor;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.app.api.sensor.EnvironmentSensorData;
import org.eclipse.mosaic.fed.application.app.api.sensor.Sensor;
import org.eclipse.mosaic.lib.enums.SensorType;
import org.eclipse.mosaic.lib.objects.environment.EnvironmentEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class EnvironmentSensor implements Sensor<EnvironmentSensorData> {

    static class EnvironmentData implements EnvironmentSensorData {

        private final Map<SensorType, EnvironmentEvent> environmentEvents = new HashMap<>();

        @Override
        public int strengthOf(SensorType sensorType) {
            EnvironmentEvent event = environmentEvents.get(sensorType);
            if (event != null && event.from <= SimulationKernel.SimulationKernel.getCurrentSimulationTime()
                    && event.until >= SimulationKernel.SimulationKernel.getCurrentSimulationTime()) {
                return event.strength;
            }
            return 0;
        }
    }

    private final EnvironmentData currentData = new EnvironmentData();
    private final Collection<Consumer<EnvironmentSensorData>> callbacks = new ArrayList<>();

    private boolean enabled = false;

    @Override
    public void enable() {
        this.enabled = true;
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public EnvironmentSensorData getSensorData() {
        return currentData;
    }

    @Override
    public void reactOnSensorDataUpdate(Consumer<EnvironmentSensorData> consumer) {
        callbacks.add(consumer);
    }

    /**
     * Adds a new {@link EnvironmentEvent} for the given {@link SensorType}.
     */
    public final void addEnvironmentEvent(SensorType type, EnvironmentEvent environmentEvent) {
        if (isEnabled()) {
            currentData.environmentEvents.put(type, environmentEvent);
            callbacks.forEach(c -> c.accept(currentData));
        }
    }

    /**
     * The events are mapped into a map on the type. With multiple events to a
     * same type, the last event is always taken. However, it should be part of
     * good form to delete the event you no longer need to save some memory.
     */
    public final void cleanPastEnvironmentEvents() {
        final Set<SensorType> toRemove = new HashSet<>();

        for (Map.Entry<SensorType, EnvironmentEvent> entrySet : currentData.environmentEvents.entrySet()) {
            SensorType type = entrySet.getKey();
            EnvironmentEvent environmentEvent = entrySet.getValue();
            if (environmentEvent.until < SimulationKernel.SimulationKernel.getCurrentSimulationTime()) {
                toRemove.add(type);
            }
        }
        toRemove.forEach(currentData.environmentEvents::remove);
    }
}
