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

package org.eclipse.mosaic.fed.application.ambassador;

import org.eclipse.mosaic.fed.application.app.TestApplicationWithSpy;
import org.eclipse.mosaic.fed.application.app.TestChargingStationApplication;
import org.eclipse.mosaic.fed.application.app.TestRoadSideUnitApplication;
import org.eclipse.mosaic.fed.application.app.TestServerApplication;
import org.eclipse.mosaic.fed.application.app.TestTrafficLightApplication;
import org.eclipse.mosaic.fed.application.app.TestTrafficManagementCenterApplication;
import org.eclipse.mosaic.interactions.mapping.AgentRegistration;
import org.eclipse.mosaic.interactions.mapping.ChargingStationRegistration;
import org.eclipse.mosaic.interactions.mapping.RsuRegistration;
import org.eclipse.mosaic.interactions.mapping.ServerRegistration;
import org.eclipse.mosaic.interactions.mapping.TmcRegistration;
import org.eclipse.mosaic.interactions.mapping.TrafficLightRegistration;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.math.SpeedUtils;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLight;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightGroup;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightProgram;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightProgramPhase;
import org.eclipse.mosaic.lib.objects.trafficlight.TrafficLightState;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleDeparture;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.rti.TIME;

import com.google.common.collect.Lists;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class, which provides predefined Unit#Registration.
 */
public class InteractionTestHelper {

    /**
     * Creates a {@link ChargingStationRegistration}-interaction with the given parameters.
     *
     * @param id                 ITS identification of charging station
     * @param startTimeInSeconds time of the interaction
     * @param withApp            flag, that indicates if the charging station is equipped with applications
     * @return the {@link ChargingStationRegistration}-interaction
     */
    static ChargingStationRegistration createChargingStationRegistration(String id, long startTimeInSeconds, boolean withApp) {
        return new ChargingStationRegistration(
                startTimeInSeconds * TIME.SECOND,
                id,
                "group_0",
                getApplications(withApp, TestChargingStationApplication.class),
                GeoPoint.lonLat(0, 0),
                Collections.emptyList()
        );
    }

    /**
     * Creates a {@link TrafficLightRegistration}-interaction with the given parameters.
     *
     * @param id                 identifier of the traffic light
     * @param startTimeInSeconds time of the interaction
     * @param withApp            flag, that indicates if the charging station is equipped with applications
     * @return the {@link TrafficLightRegistration}-interaction
     */
    static TrafficLightRegistration createTrafficLightRegistration(String id, long startTimeInSeconds, boolean withApp) {
        Map<String, TrafficLightProgram> programs = new HashMap<>();

        List<TrafficLightProgramPhase> phases = Lists.newArrayList(
                new TrafficLightProgramPhase(0, 1000, Lists.newArrayList(new TrafficLightState(false, false, false)))
        );
        TrafficLightProgram program = new TrafficLightProgram("0", phases, 0);
        programs.put("0", program);

        List<TrafficLight> trafficLights = Lists.newArrayList(new TrafficLight(0,
                GeoPoint.latLon(0.0, 0.0),
                //just examples, may not match the real lanes
                "32935480_21677261_21668930_21677261_0",
                "32935480_21668930_27537748_21668930_0",
                new TrafficLightState(false, false, false)));

        TrafficLightGroup tlg = new TrafficLightGroup("trafficLightGroupId", programs, trafficLights);
        return new TrafficLightRegistration(
                startTimeInSeconds * TIME.SECOND,
                id,
                "mappingGroup",
                getApplications(withApp, TestTrafficLightApplication.class),
                tlg,
                Collections.emptyList()
        );
    }

    /**
     * Creates a {@link RsuRegistration}-interaction with the given parameters.
     *
     * @param id                 identifier of the rsu
     * @param startTimeInSeconds time of the interaction
     * @param withApp            flag, that indicates if the charging station is equipped with applications
     * @return the {@link RsuRegistration}-interaction
     */
    static RsuRegistration createRsuRegistration(String id, long startTimeInSeconds, boolean withApp) {
        return new RsuRegistration(
                startTimeInSeconds * TIME.SECOND,
                id,
                "group_0",
                getApplications(withApp, TestRoadSideUnitApplication.class),
                GeoPoint.lonLat(0, 0)
        );
    }

    /**
     * Creates a {@link VehicleRegistration}-interaction with the given parameters
     * for a regular car.
     *
     * @param id                 identifier of the vehicle
     * @param startTimeInSeconds time of the interaction
     * @param testClass          application class
     * @return the {@link VehicleRegistration}-interaction
     */
    public static VehicleRegistration createVehicleRegistrationInteraction(
            String id,
            long startTimeInSeconds,
            Class<? extends TestApplicationWithSpy<?>> testClass
    ) {
        VehicleDeparture departure = new VehicleDeparture.Builder(
                "0"
        ).create();
        return new VehicleRegistration(
                startTimeInSeconds * TIME.SECOND,
                id,
                "group_0",
                getApplications(testClass != null, testClass),
                departure,
                new VehicleType(id)
        );
    }

    /**
     * Creates a {@link AgentRegistration}-interaction with the given parameters
     * for an agent.
     *
     * @param id                 identifier of the agent
     * @param startTimeInSeconds time of the interaction
     * @param testClass          application class
     * @return the {@link VehicleRegistration}-interaction
     */
    public static AgentRegistration createAgentRegistrationInteraction(
            String id,
            long startTimeInSeconds,
            Class<? extends TestApplicationWithSpy<?>> testClass
    ) {
        return new AgentRegistration(
                startTimeInSeconds * TIME.SECOND,
                id,
                "group_0",
                GeoPoint.ORIGO,
                GeoPoint.ORIGO,
                getApplications(testClass != null, testClass),
                SpeedUtils.kmh2ms(3)
        );
    }

    /**
     * Creates a {@link VehicleRegistration}-interaction with the given parameters
     * for an electric car.
     *
     * @param id                 identifier of the vehicle
     * @param startTimeInSeconds time of the interaction
     * @param testClass          application class
     * @return the {@link VehicleRegistration}-interaction
     */
    static VehicleRegistration createVehicleRegistration_ElectricVehicle(
            String id,
            long startTimeInSeconds,
            Class<? extends TestApplicationWithSpy<?>> testClass
    ) {
        VehicleDeparture departure = new VehicleDeparture.Builder(
                "0"
        ).create();
        VehicleType vehType = Mockito.mock(VehicleType.class);
        Mockito.when(vehType.getName()).thenReturn(id);
        Mockito.when(vehType.getVehicleClass()).thenReturn(VehicleClass.ElectricVehicle);
        return new VehicleRegistration(
                startTimeInSeconds * TIME.SECOND,
                id,
                "group_0",
                getApplications(testClass != null, testClass),
                departure,
                vehType
        );
    }

    /**
     * Creates a {@link TmcRegistration}-interaction with the given parameters.
     *
     * @param id                 identifier of the traffic management center
     * @param startTimeInSeconds time of the interaction
     * @param withApp            flag, that indicates if the charging station is equipped with applications
     * @param inductionLoopIds   identifiers of induction loops subscribed to the tmc
     * @return the {@link TmcRegistration}-interaction
     */
    static TmcRegistration createTmcRegistrationWithInductionLoops(
            String id,
            long startTimeInSeconds,
            boolean withApp,
            String... inductionLoopIds
    ) {
        return new TmcRegistration(
                startTimeInSeconds * TIME.SECOND,
                id,
                "group_0",
                getApplications(withApp, TestTrafficManagementCenterApplication.class),
                Lists.newArrayList(inductionLoopIds),
                Lists.newArrayList()
        );
    }

    /**
     * Creates a {@link TmcRegistration}-interaction with the given parameters.
     *
     * @param id                 identifier of the traffic management center
     * @param startTimeInSeconds time of the interaction
     * @param withApp            flag, that indicates if the charging station is equipped with applications
     * @param laneAreaIds        identifiers of lane area detectors subscribed to the tmc
     * @return the {@link TmcRegistration}-interaction
     */
    static TmcRegistration createTmcRegistrationWithLaneAreaDetectors(
            String id,
            long startTimeInSeconds,
            boolean withApp,
            String... laneAreaIds
    ) {
        return new TmcRegistration(
                startTimeInSeconds * TIME.SECOND,
                id,
                "group_0",
                getApplications(withApp, TestTrafficManagementCenterApplication.class),
                Lists.newArrayList(),
                Lists.newArrayList(laneAreaIds)
        );
    }

    static ServerRegistration createServerRegistration(String name, long startTimeInSeconds, boolean withApp) {
        return new ServerRegistration(
                startTimeInSeconds * TIME.SECOND,
                name,
                "group_0",
                getApplications(withApp, TestServerApplication.class)
        );
    }

    private static List<String> getApplications(boolean withApp, Class<?> appClass) {
        if (withApp) {
            return Collections.singletonList(appClass.getCanonicalName());
        } else {
            return Collections.emptyList();
        }
    }


}
