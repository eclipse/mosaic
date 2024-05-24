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

package org.eclipse.mosaic.lib.objects.vehicle.sensor;

import org.eclipse.mosaic.lib.util.gson.PolymorphismTypeAdapterFactory;

import com.google.gson.annotations.JsonAdapter;

import java.io.Serializable;

public class LidarData implements Serializable {
    @JsonAdapter(PolymorphismTypeAdapterFactory.class)
    private final Object lidarData;

    private final long time;
    private final String name;
    /**
     * Creates a new {@link LidarData}.
     *
     * @param time      time of the last update
     * @param name      name of the unit
     * @param lidarData Object that contains the LidarFrame data
     */
    public LidarData(long time, String name, Object lidarData) {
        this.lidarData = lidarData;
        this.time = time;
        this.name = name;
    }

    /**
     * Returns additional vehicle data produced by the traffic or vehicle simulator.
     * Can be of any arbitrary type, and is <code>null</code> if the producer has not been
     * implemented in a way to add this additional data.
     */
    public Object getLidarData() {
        return this.lidarData;
    }

    public long getTime() {
        return this.time;
    }

    public String getName() {
        return this.name;
    }


    /**
     * A builder for creating {@link LidarData} objects
     */
    public static class Builder {
        private final long time;
        private final String name;
        private Object lidarData;

        /**
         * Init the builder with the current simulation time [ns] and name of the vehicle.
         */
        public Builder(long time, String name) {
            this.time = time;
            this.name = name;
        }

        /**
         * Set position related values for the vehicle info.
         */
        public LidarData.Builder lidarData(Object lidarData) {
            this.lidarData = lidarData;
            return this;
        }

        /**
         * Returns the final {@link LidarData} based on the properties given before.
         */
        public LidarData create() {
            return new LidarData(
                    time, name,lidarData);
        }
    }
}
