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

package org.eclipse.mosaic.lib.objects.road;

import java.io.Serializable;

/**
 * Provides way related properties, such as the maximum allowed speed.
 */
public interface IWay extends Serializable {

    /**
     * Returns the id of this way.
     */
    String getId();

    /**
     * Returns the type of this way, such as "motorway".
     */
    String getType();

    /**
     * Returns the maximum allowed speed on this way, in m/s.
     */
    double getMaxSpeedInMs();


    /**
     * Gets the maximum speed allowed on this way in km/h.
     *
     * @return Maximum speed allowed on this way in [km/h].
     */
    default double getMaxSpeedInKmh() {
        return getMaxSpeedInMs() * 3.6d;
    }
}
