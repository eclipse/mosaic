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

import org.eclipse.mosaic.lib.util.NameGenerator;
import org.eclipse.mosaic.lib.util.objects.IdTransformer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Several components of Eclipse MOSAIC expect the identifier of the vehicles to
 * match the following expression: ^veh_[0-9]+$. However, predefined SUMO
 * scenarios usually come with custom vehicle ids which do not match this
 * pattern, so we need to transform them into the required format which is
 * accomplished by this class.
 *
 * @see org.eclipse.mosaic.fed.sumo.ambassador.SumoScenarioAmbassador
 */
public class MosaicConformVehicleIdTransformer implements IdTransformer<String, String> {

    private final static Logger log = LoggerFactory.getLogger(MosaicConformVehicleIdTransformer.class);

    private final BiMap<String, String> mosaicToSumoVehicleIdMap = HashBiMap.create(1024);

    /**
     * Takes a MOSAIC conform vehicle id (e.g. veh_1) and returns the saved SUMO id in {@link #mosaicToSumoVehicleIdMap}.
     * If a new vehicle from MOSAIC has to be added to SUMO we use the same id.
     *
     * @param mosaicVehicleId the MOSAIC conform vehicle id
     * @return the corresponding SUMO id
     */
    @Override
    public String toExternalId(String mosaicVehicleId) {
        String sumoVehicleId = mosaicToSumoVehicleIdMap.inverse().get(mosaicVehicleId);
        if (sumoVehicleId == null) {
            // TODO we don't really need inverse here, since we put the same value twice
            mosaicToSumoVehicleIdMap.inverse().put(mosaicVehicleId, mosaicVehicleId);
            sumoVehicleId = mosaicVehicleId; // return incoming id
        }
        return sumoVehicleId;
    }

    /**
     * Takes a SUMO vehicle id and makes it known to the simulation by creating a
     * MOSAIC conform vehicle id and adding it to {@link #mosaicToSumoVehicleIdMap}.
     *
     * @param sumoVehicleId the id from SUMO
     * @return the created MOSAIC conform vehicle id
     */
    @Override
    public String fromExternalId(String sumoVehicleId) {
        String mosaicVehicleId = mosaicToSumoVehicleIdMap.get(sumoVehicleId);
        if (mosaicVehicleId == null) {
            mosaicVehicleId = NameGenerator.getVehicleName();
            mosaicToSumoVehicleIdMap.put(sumoVehicleId, mosaicVehicleId);
            log.info("Assigned vehicle id {} to vehicle {}", mosaicVehicleId, sumoVehicleId);
        }
        return mosaicVehicleId;
    }
}