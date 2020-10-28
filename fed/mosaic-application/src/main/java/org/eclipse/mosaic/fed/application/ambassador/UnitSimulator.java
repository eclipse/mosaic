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

import org.eclipse.mosaic.fed.application.ambassador.eventresources.RemoveVehicles;
import org.eclipse.mosaic.fed.application.ambassador.eventresources.StartApplications;
import org.eclipse.mosaic.fed.application.ambassador.simulation.AbstractSimulationUnit;
import org.eclipse.mosaic.fed.application.ambassador.simulation.ChargingStationUnit;
import org.eclipse.mosaic.fed.application.ambassador.simulation.ElectricVehicleUnit;
import org.eclipse.mosaic.fed.application.ambassador.simulation.RoadSideUnit;
import org.eclipse.mosaic.fed.application.ambassador.simulation.ServerUnit;
import org.eclipse.mosaic.fed.application.ambassador.simulation.TrafficLightGroupUnit;
import org.eclipse.mosaic.fed.application.ambassador.simulation.TrafficManagementCenterUnit;
import org.eclipse.mosaic.fed.application.ambassador.simulation.VehicleUnit;
import org.eclipse.mosaic.interactions.environment.EnvironmentSensorActivation;
import org.eclipse.mosaic.interactions.mapping.ChargingStationRegistration;
import org.eclipse.mosaic.interactions.mapping.RsuRegistration;
import org.eclipse.mosaic.interactions.mapping.ServerRegistration;
import org.eclipse.mosaic.interactions.mapping.TmcRegistration;
import org.eclipse.mosaic.interactions.mapping.TrafficLightRegistration;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.traffic.SumoTraciResult;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.lib.util.scheduling.EventProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The {@link UnitSimulator} is a singleton responsible for registering/removing {@link AbstractSimulationUnit}s to/from the
 * simulation, enabling the units' sensors and loading the units' applications. Note: The {@link UnitSimulator} is only
 * concerned about units equipped with applications.
 */
public enum UnitSimulator implements EventProcessor {

    UnitSimulator;

    /**
     * The log instance.
     */
    public final static Logger log = LoggerFactory.getLogger(UnitSimulator.class);

    /**
     * Map containing all the ids with the corresponding charging stations.
     */
    private final Map<String, ChargingStationUnit> chargingStations = new HashMap<>();

    /**
     * Map containing all the ids with the corresponding road side units.
     */
    private final Map<String, RoadSideUnit> roadSideUnits = new HashMap<>();

    /**
     * Map containing all the ids with the corresponding traffic lights.
     */
    private final Map<String, TrafficLightGroupUnit> trafficLights = new HashMap<>();

    /**
     * Map containing all the ids with the corresponding TMCs (Traffic Management Centers).
     */
    private final Map<String, TrafficManagementCenterUnit> tmcs = new HashMap<>();

    /**
     * Map containing all the ids with the corresponding Servers.
     */
    private final Map<String, ServerUnit> servers = new HashMap<>();

    /**
     * Map containing all the ids with the corresponding vehicles.
     */
    private final Map<String, VehicleUnit> vehicles = new HashMap<>();

    /**
     * Map containing all the ids with the corresponding {@link AbstractSimulationUnit}.
     */
    private final Map<String, AbstractSimulationUnit> allUnits = new HashMap<>();

    /**
     * Returns the map containing all the ids with the corresponding charging stations.
     *
     * @return the map containing all the ids with the corresponding charging stations.
     */
    public Map<String, ChargingStationUnit> getChargingStations() {
        return chargingStations;
    }

    /**
     * Returns the map containing all the ids with the corresponding road side units.
     *
     * @return the map containing all the ids with the corresponding road side units.
     */
    public Map<String, RoadSideUnit> getRoadSideUnits() {
        return roadSideUnits;
    }

    /**
     * Returns the map containing all the ids with the corresponding traffic lights.
     *
     * @return the map containing all the ids with the corresponding traffic lights.
     */
    public Map<String, TrafficLightGroupUnit> getTrafficLights() {
        return trafficLights;
    }

    /**
     * Returns the map containing all the ids with the corresponding vehicles.
     *
     * @return the map containing all the ids with the corresponding vehicles.
     */
    public Map<String, VehicleUnit> getVehicles() {
        return vehicles;
    }

    /**
     * Returns the map containing all the ids with the corresponding TMCs.
     *
     * @return the map containing all the ids with the corresponding TMCs.
     */
    public Map<String, TrafficManagementCenterUnit> getTmcs() {
        return tmcs;
    }

    /**
     * Returns the map containing all the ids with the corresponding servers.
     *
     * @return the map containing all the ids with the corresponding servers.
     */
    public Map<String, ServerUnit> getServers() {
        return servers;
    }

    /**
     * Returns the map containing all the ids with the corresponding {@link AbstractSimulationUnit}.
     *
     * @return the map containing all the ids with the corresponding {@link AbstractSimulationUnit}.
     */
    public Map<String, AbstractSimulationUnit> getAllUnits() {
        return allUnits;
    }

    /**
     * Use this method to put the unit in the specific maps.
     *
     * @param unit The unit to add.
     */
    private void addSimulationUnit(final AbstractSimulationUnit unit) {
        if (allUnits.containsKey(unit.getId())) {
            log.error("allUnits already contain the id: " + unit.getId());
            throw new RuntimeException(ErrorRegister.UNIT_SIMULATOR_IdAlreadyAssigned.toString());
        }
        // first, put the simulation unit in the general map, where all simulated units are recorded
        allUnits.put(unit.getId(), unit);

        // second, put the simulation unit in the specific map
        if (unit instanceof ChargingStationUnit) {
            chargingStations.put(unit.getId(), (ChargingStationUnit) unit);
        } else if (unit instanceof RoadSideUnit) {
            roadSideUnits.put(unit.getId(), (RoadSideUnit) unit);
        } else if (unit instanceof TrafficLightGroupUnit) {
            trafficLights.put(unit.getId(), (TrafficLightGroupUnit) unit);
        } else if (unit instanceof TrafficManagementCenterUnit) {
            tmcs.put(unit.getId(), (TrafficManagementCenterUnit) unit);
        } else if (unit instanceof ServerUnit) {
            servers.put(unit.getId(), (ServerUnit) unit);
        } else if (unit instanceof VehicleUnit) {
            vehicles.put(unit.getId(), (VehicleUnit) unit);
        } else {
            throw new RuntimeException(ErrorRegister.UNIT_SIMULATOR_UnknownSimulationUnitToPutInMap.toString());
        }
    }

    /**
     * Use this method to remove the unit in the specific maps.
     *
     * @param unit The unit to remove.
     */
    private void removeSimulationUnit(final AbstractSimulationUnit unit) {
        // first, remove the simulation unit from the general
        allUnits.remove(unit.getId());

        // second, remove the simulation unit from the specific map
        if (unit instanceof ChargingStationUnit) {
            chargingStations.remove(unit.getId());
        } else if (unit instanceof RoadSideUnit) {
            roadSideUnits.remove(unit.getId());
        } else if (unit instanceof TrafficLightGroupUnit) {
            trafficLights.remove(unit.getId());
        } else if (unit instanceof TrafficManagementCenterUnit) {
            tmcs.remove(unit.getId());
        } else if (unit instanceof ServerUnit) {
            servers.remove(unit.getId());
        } else if (unit instanceof VehicleUnit) {
            vehicles.remove(unit.getId());
        } else {
            throw new RuntimeException(ErrorRegister.UNIT_SIMULATOR_UnknownSimulationUnitToRemoveFromMap.toString());
        }
    }

    /**
     * This function is used to clear all simulation units.
     * First the {@link #allUnits} map will be cleared,
     * and after that all the separate maps are cleared.
     */
    public void removeAllSimulationUnits() {
        for (Map.Entry<String, AbstractSimulationUnit> entry : allUnits.entrySet()) {
            AbstractSimulationUnit unit = entry.getValue();
            // tear down the simulation unit
            unit.tearDown();
        }
        // clear all maps
        chargingStations.clear();
        roadSideUnits.clear();
        trafficLights.clear();
        vehicles.clear();
        tmcs.clear();
        servers.clear();
        allUnits.clear();
    }

    /**
     * Returns a unit from the given id.
     * Always check the return value against {@code null}.
     *
     * @param id The id of the unit.
     * @return A unit for the given id. {@code null} if no unit is found for the given id.
     */
    public AbstractSimulationUnit getUnitFromId(final String id) {
        return allUnits.get(id);
    }

    /**
     * Returns a unit for the given id. This method throws an RuntimeException if the given unit was not found.
     *
     * @param id The id of the unit.
     * @return A unit for the given id. {@code null} if no unit is found with this id.
     */
    public AbstractSimulationUnit getUnitFromIdNonNull(final String id) {
        return Objects.requireNonNull(allUnits.get(id), ErrorRegister.UNIT_SIMULATOR_IdFromUnitIsNotInMap.toString());
    }

    /**
     * Registers a Road Side Unit (RSU).
     * The RSU is only registered if it is equipped with an application.
     *
     * @param rsuRegistration road side unit
     */
    public void registerRsu(RsuRegistration rsuRegistration) {
        if (!rsuRegistration.getMapping().hasApplication()) {
            return;
        }
        String rsuName = rsuRegistration.getMapping().getName();
        GeoPoint rsuPosition = rsuRegistration.getMapping().getPosition();
        final RoadSideUnit roadSideUnit = new RoadSideUnit(rsuName, rsuPosition);
        addSimulationUnit(roadSideUnit);
        doSensorRegistration(rsuRegistration.getTime(), roadSideUnit.getId());

        final Event event = new Event(
                rsuRegistration.getTime(),
                this,
                new StartApplications(roadSideUnit.getId(), rsuRegistration.getMapping()),
                Event.NICE_MAX_PRIORITY
        );
        SimulationKernel.SimulationKernel.getEventManager().addEvent(event);
    }

    /**
     * Registers a Traffic Management Center (TMC).
     * The TMC is only registered if it is equipped with an application.
     *
     * @param tmcRegistration traffic management center
     */
    public void registerTmc(TmcRegistration tmcRegistration) {
        if (!tmcRegistration.getMapping().hasApplication()) {
            return;
        }
        final TrafficManagementCenterUnit tmc = new TrafficManagementCenterUnit(tmcRegistration.getMapping());
        addSimulationUnit(tmc);
        // doSensorRegistration(tmcRegistration.getTime(), tmc.getId());

        final Event event = new Event(
                tmcRegistration.getTime(),
                this, new StartApplications(tmc.getId(), tmcRegistration.getMapping()),
                Event.NICE_MAX_PRIORITY
        );
        SimulationKernel.SimulationKernel.getEventManager().addEvent(event);
    }

    /**
     * Registers a Server. Unit is only registered if it is equipped with an application
     *
     * @param serverRegistration the interaction containing the mapping of the server
     */
    public void registerServer(ServerRegistration serverRegistration) {
        if (!serverRegistration.getMapping().hasApplication()) {
            return;
        }
        final ServerUnit server = new ServerUnit(serverRegistration.getMapping());
        addSimulationUnit(server);

        final Event event = new Event(
                serverRegistration.getTime(),
                this, new StartApplications(server.getId(), serverRegistration.getMapping()),
                Event.NICE_MAX_PRIORITY
        );
        SimulationKernel.SimulationKernel.getEventManager().addEvent(event);
    }

    /**
     * Registers a charging station.
     * The charging station is only registered if it is equipped with an application.
     *
     * @param chargingStationRegistration charging station
     */
    public void registerChargingStation(ChargingStationRegistration chargingStationRegistration) {
        if (!chargingStationRegistration.getMapping().hasApplication()) {
            return;
        }
        String name = chargingStationRegistration.getMapping().getName();
        GeoPoint position = chargingStationRegistration.getMapping().getPosition();
        final ChargingStationUnit chargingStationUnit = new ChargingStationUnit(name, position);
        addSimulationUnit(chargingStationUnit);
        doSensorRegistration(chargingStationRegistration.getTime(), chargingStationUnit.getId());

        final Event event = new Event(
                chargingStationRegistration.getTime(),
                this, new StartApplications(chargingStationUnit.getId(), chargingStationRegistration.getMapping()),
                Event.NICE_MAX_PRIORITY
        );
        SimulationKernel.SimulationKernel.getEventManager().addEvent(event);
    }

    /**
     * Registers a traffic light.
     * The traffic light is only registered if it is equipped with an application.
     *
     * @param trafficLightRegistration traffic light
     */
    public void registerTrafficLight(TrafficLightRegistration trafficLightRegistration) {
        if (!trafficLightRegistration.getMapping().hasApplication()) {
            return;
        }
        final TrafficLightGroupUnit trafficLightGroupUnit = new TrafficLightGroupUnit(
                trafficLightRegistration.getMapping().getName(),
                trafficLightRegistration.getMapping().getPosition(),
                trafficLightRegistration.getTrafficLightGroup()
        );
        addSimulationUnit(trafficLightGroupUnit);
        doSensorRegistration(trafficLightRegistration.getTime(), trafficLightGroupUnit.getId());

        final Event event = new Event(
                trafficLightRegistration.getTime(),
                this,
                new StartApplications(trafficLightGroupUnit.getId(), trafficLightRegistration.getMapping()),
                Event.NICE_MAX_PRIORITY
        );
        SimulationKernel.SimulationKernel.getEventManager().addEvent(event);
    }

    /**
     * Registers a vehicle.
     * The vehicle is only registered if it is equipped with an application.
     *
     * @param time                timestamp when the vehicle was added to the simulation
     * @param vehicleRegistration vehicle
     */
    public void registerVehicle(long time, VehicleRegistration vehicleRegistration) {
        if (!vehicleRegistration.getMapping().hasApplication()) {
            return;
        }

        GeoPoint initialPosition = null;
        if (vehicleRegistration.getDeparture() != null && vehicleRegistration.getDeparture().getRouteId() != null) {
            initialPosition = SimulationKernel.SimulationKernel.getCentralNavigationComponent()
                    .getSourcePositionOfRoute(vehicleRegistration.getDeparture().getRouteId());
        }

        final VehicleUnit vehicle;
        String vehicleName = vehicleRegistration.getMapping().getName();
        VehicleType vehicleType = vehicleRegistration.getMapping().getVehicleType();
        if (vehicleRegistration.getMapping().getVehicleType().getVehicleClass().equals(VehicleClass.ElectricVehicle)) {
            vehicle = new ElectricVehicleUnit(vehicleName, vehicleType, initialPosition);
        } else {
            vehicle = new VehicleUnit(vehicleName, vehicleType, initialPosition);
        }

        addSimulationUnit(vehicle);
        doSensorRegistration(time, vehicle.getId());

        // Start Applications
        Event event = new Event(
                time,
                this,
                new StartApplications(vehicle.getId(), vehicleRegistration.getMapping()),
                Event.NICE_MAX_PRIORITY
        );
        SimulationKernel.SimulationKernel.getEventManager().addEvent(event);
    }

    /**
     * This method registers the simulation unit to sensor events.
     *
     * @param time the time for the registration
     * @param id   the id of the simulation unit
     */
    private void doSensorRegistration(final long time, final String id) {
        AbstractSimulationUnit simulationUnit = UnitSimulator.getUnitFromIdNonNull(id);
        // Note: we do not use the SimulationKernel.SimulationKernel.getCurrentSimulationTime() here, since the
        // current simulation time lies in the past
        EnvironmentSensorActivation environmentSensorActivation = new EnvironmentSensorActivation(time, id);
        simulationUnit.sendInteractionToRti(environmentSensorActivation);
    }

    @Override
    public void processEvent(Event event) {
        final Object resource = event.getResource();

        if (resource instanceof RemoveVehicles) {
            removeVehicles((RemoveVehicles) resource);
        } else if (resource instanceof StartApplications) {
            startApplications((StartApplications) resource);
        }
    }

    /**
     * Remove vehicles from the simulation.
     *
     * @param removeVehicles Vehicles to be removed.
     */
    private void removeVehicles(RemoveVehicles removeVehicles) {
        for (String vehicleId : removeVehicles.getRemovedNames()) {
            log.trace("remove vehicle: {}" + vehicleId);
            AbstractSimulationUnit unit = getUnitFromId(vehicleId);
            if (unit == null) {
                continue;
            }
            // tear down the simulation unit
            unit.tearDown();
            // finally remove the vehicle from the maps
            removeSimulationUnit(unit);
        }
    }

    private void startApplications(StartApplications loadUnit) {
        // it is not possible to start applications on a not simulating unit
        AbstractSimulationUnit simulationUnit = getUnitFromIdNonNull(loadUnit.getUnitId());

        // load applications into simulation unit
        simulationUnit.loadApplications(loadUnit.getUnitMapping().getApplications());
    }

    public void processSumoTraciMessage(final SumoTraciResult sumoTraciResult) {
        for (AbstractSimulationUnit unit : allUnits.values()) {
            unit.processSumoTraciMessage(sumoTraciResult);
        }
    }
}
