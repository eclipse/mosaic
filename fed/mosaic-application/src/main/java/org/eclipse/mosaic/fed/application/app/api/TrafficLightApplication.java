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

package org.eclipse.mosaic.fed.application.app.api;

import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroupInfo;

/**
 * All applications accessing traffic light functionality
 * are to implement this interface.
 */
public interface TrafficLightApplication extends Application {

    /**
     * Is called whenever the properties of the mapped traffic light group has changed.
     *
     * @param previousState the previous state
     * @param updatedState  the updated state
     */
    void onTrafficLightGroupUpdated(TrafficLightGroupInfo previousState, TrafficLightGroupInfo updatedState);
}
