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

package org.eclipse.mosaic.fed.sumo.ambassador;

import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;

/**
 * This class is used to hold and query data of externally simulated vehicles
 * it is used by the {@link AbstractSumoAmbassador} to synchronize vehicles between
 * simulations.
 */
class ExternalVehicleState {

    /**
     * Flag to indicate whether the {@link VehicleData} of the last update is added.
     * Every {@link ExternalVehicleState} is initialized as not added and will be
     * added during simulation.
     */
    private boolean added = false;

    private VehicleData lastMovementInfo = null;

    /**
     * Info about the last vehicle movement.
     *
     * @param lastMovementInfo The last vehicle info.
     */
    public void setLastMovementInfo(VehicleData lastMovementInfo) {
        this.lastMovementInfo = lastMovementInfo;
    }

    /**
     * Sets the boolean value which indicates whether the last vehicle movement is added.
     *
     * @param added Value to indicate the status of the update.
     */
    public void setAdded(boolean added) {
        this.added = added;
    }

    /**
     * Gets the info about last vehicle movement.
     *
     * @return Last vehicle movement.
     */
    public VehicleData getLastMovementInfo() {
        return lastMovementInfo;
    }

    /**
     * Checks whether the last vehicle movement is added.
     *
     * @return True if the last vehicle movement is added.
     */
    public boolean isAdded() {
        return added;
    }
}
