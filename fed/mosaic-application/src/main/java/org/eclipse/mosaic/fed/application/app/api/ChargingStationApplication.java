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

import org.eclipse.mosaic.fed.application.app.api.os.ChargingStationOperatingSystem;
import org.eclipse.mosaic.lib.objects.electricity.ChargingStationData;

/**
 * All applications accessing charging station functionality
 * are to implement this interface.
 */
public interface ChargingStationApplication extends Application, OperatingSystemAccess<ChargingStationOperatingSystem> {

    /**
     * This method is called after{@link ChargingStationData} has been updated.
     * (Requires the ChargingStationAmbassador to be activated and configured properly)
     *
     * @param previousInfo the {@link ChargingStationData} before the update
     * @param updatedInfo  the {@link ChargingStationData} after the update
     */
    void onChargingStationUpdated(ChargingStationData previousInfo, ChargingStationData updatedInfo);

}
