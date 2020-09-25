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

package org.eclipse.mosaic.fed.application.app.api;

import org.eclipse.mosaic.interactions.application.ApplicationInteraction;
import org.eclipse.mosaic.lib.objects.traffic.SumoTraciResult;

/**
 * Provides MOSAIC specific features.
 */
public interface MosaicApplication extends Application {

    /**
     * This method is called if a SUMO TrACI response is received.
     *
     * @param sumoTraciResult the response container.
     */
    void onSumoTraciResponded(SumoTraciResult sumoTraciResult);

    /**
     * This method is called when an {@link Application Interaction} is received.
     *
     * @param applicationInteraction the specific interaction.
     */
    void onInteractionReceived(ApplicationInteraction applicationInteraction);
}
