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

package org.eclipse.mosaic.fed.application.app.etsi;

import org.eclipse.mosaic.fed.application.app.api.os.TrafficLightOperatingSystem;

/**
 * ETSI conform application for traffic lights.
 */
public class TrafficLightCamSendingApp extends AbstractCamSendingApp<TrafficLightOperatingSystem> {

    @Override
    public Data generateEtsiData() {
        final Data data = new Data();
        data.position = getOperatingSystem().getPosition();
        data.projectedPosition = getOperatingSystem().getPosition().toCartesian();
        data.time = getOperatingSystem().getSimulationTime();
        return data;
    }
}
