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

package org.eclipse.mosaic.fed.sumo.util;

import org.eclipse.mosaic.lib.objects.UnitNameGenerator;
import org.eclipse.mosaic.lib.util.objects.IdTransformer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Several components of Eclipse MOSAIC expect the identifier of the vehicles to
 * match the following expression: ^veh_[0-9]+$. However, predefined
 * scenarios usually come with custom vehicle ids which do not match this
 * pattern, so we need to transform them into the required format which is
 * accomplished by this class.
 */
public class MosaicConformVehicleIdTransformer implements IdTransformer<String, String> {

    private final static Logger log = LoggerFactory.getLogger(MosaicConformVehicleIdTransformer.class);

    private final BiMap<String, String> vehicleIdMap = HashBiMap.create(1024);

    /**
     * Takes a MOSAIC conform vehicle id (e.g. veh_1) and returns the saved external id in {@link #vehicleIdMap}.
     * If a new vehicle from MOSAIC has to be added to an external simulator we use the same id.
     *
     * @param mosaicVehicleId the MOSAIC conform vehicle id
     * @return the corresponding external id
     */
    @Override
    public String toExternalId(String mosaicVehicleId) {
        String externalVehicleId = vehicleIdMap.inverse().get(mosaicVehicleId);
        if (externalVehicleId == null) {
            vehicleIdMap.inverse().put(mosaicVehicleId, mosaicVehicleId);
            externalVehicleId = mosaicVehicleId; // return incoming id
        }
        return externalVehicleId;
    }

    /**
     * Takes an external vehicle id, creates a MOSAIC-conform vehicle id and adds it to {@link #vehicleIdMap}.
     *
     * @param externalVehicleId the id from the external traffic/vehicle simulator
     * @return the created MOSAIC conform vehicle id
     */
    @Override
    public String fromExternalId(String externalVehicleId) {
        String mosaicVehicleId = vehicleIdMap.get(externalVehicleId);
        if (mosaicVehicleId == null) {
            mosaicVehicleId = UnitNameGenerator.nextVehicleName();
            vehicleIdMap.put(externalVehicleId, mosaicVehicleId);
            log.info("Assigned vehicle MOSAIC-ID: \"{}\" to external vehicle: \"{}\"", mosaicVehicleId, externalVehicleId);
        }
        return mosaicVehicleId;
    }

    @Override
    public void reset() {
        vehicleIdMap.clear();
    }
}