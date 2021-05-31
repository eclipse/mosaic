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

package org.eclipse.mosaic.fed.mapping.config;

import org.eclipse.mosaic.fed.mapping.config.units.CChargingStation;
import org.eclipse.mosaic.fed.mapping.config.units.COriginDestinationMatrixMapper;
import org.eclipse.mosaic.fed.mapping.config.units.CRoadSideUnit;
import org.eclipse.mosaic.fed.mapping.config.units.CServer;
import org.eclipse.mosaic.fed.mapping.config.units.CTrafficLight;
import org.eclipse.mosaic.fed.mapping.config.units.CTrafficManagementCenter;
import org.eclipse.mosaic.fed.mapping.config.units.CVehicle;

import java.util.List;
import java.util.Map;

/**
 * Basic type carrying the complete configuration for the mapping of one
 * scenario. The JSON-String carrying the data will be parsed against the
 * classes in this package.
 */
public class CMappingAmbassador {

    /**
     * Additional options for the mapping.
     */
    public CMappingConfiguration config;

    /**
     * Prototypes being used. Prototypes can complete the definitions of other
     * objects. This can be used to re-use certain configurations.
     */
    public List<CPrototype> prototypes;

    /**
     * List of RSUs.
     */
    public List<CRoadSideUnit> rsus;

    /**
     * List of TMCs.
     */
    public List<CTrafficManagementCenter> tmcs;

    /**
     * List of Servers.
     */
    public List<CServer> servers;

    /**
     * List of the TLTypes. It can be randomly or specifically be mapped onto
     * the traffic lights in the scenario.
     */
    public List<CTrafficLight> trafficLights;

    /**
     * List of the ChargingStationTypes. It can be randomly or specifically be mapped onto
     * the charging stations in the scenario.
     */
    public List<CChargingStation> chargingStations;

    /**
     * List of vehicle spawners.
     */
    public List<CVehicle> vehicles;

    /**
     * List of additional traffic that will be spawned using OD-matrices.
     */
    public List<COriginDestinationMatrixMapper> matrixMappers;

    /**
     * A list of distribution of prototypes to reuse in vehicle spawners.
     */
    public Map<String, List<CPrototype>> typeDistributions;
}