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
package org.eclipse.mosaic.fed.sumo.bridge.libsumo;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.AbstractSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.InductionLoopSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.InductionLoopVehicleData;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.LaneAreaSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.TrafficLightSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.VehicleSubscriptionResult;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.util.objects.Position;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.eclipse.sumo.libsumo.InductionLoop;
import org.eclipse.sumo.libsumo.LaneArea;
import org.eclipse.sumo.libsumo.Simulation;
import org.eclipse.sumo.libsumo.StringVector;
import org.eclipse.sumo.libsumo.TraCIPosition;
import org.eclipse.sumo.libsumo.TrafficLight;
import org.eclipse.sumo.libsumo.Vehicle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SimulationSimulateStep implements org.eclipse.mosaic.fed.sumo.bridge.api.SimulationSimulateStep {

    final static Set<String> VEHICLE_SUBSCRIPTIONS = new HashSet<>();
    final static Set<String> INDUCTION_LOOP_SUBSCRIPTIONS = new HashSet<>();
    final static Set<String> LANE_AREA_SUBSCRIPTIONS = new HashSet<>();
    final static Set<String> TRAFFIC_LIGHT_SUBSCRIPTIONS = new HashSet<>();

    public SimulationSimulateStep() {
        VEHICLE_SUBSCRIPTIONS.clear();
        INDUCTION_LOOP_SUBSCRIPTIONS.clear();
        LANE_AREA_SUBSCRIPTIONS.clear();
        TRAFFIC_LIGHT_SUBSCRIPTIONS.clear();
    }

    public List<AbstractSubscriptionResult> execute(Bridge traciCon, long time) throws CommandException, InternalFederateException {
        Simulation.step((double) (time) / TIME.SECOND);

        List<AbstractSubscriptionResult> results = new ArrayList<>();
        readVehicles(results);
        readInductionLoops(results);
        readLaneAreas(results);
        readTrafficLights(results);

        return results;
    }

    private void readVehicles(List<AbstractSubscriptionResult> results) {
        StringVector vector = Vehicle.getIDList();

        String vehicle;
        for (int i = 0; i < vector.size(); i++) {
            vehicle = vector.get(i);

            if (!VEHICLE_SUBSCRIPTIONS.contains(vehicle)) {
                continue;
            }

            VehicleSubscriptionResult result = new VehicleSubscriptionResult();
            result.id = vehicle;
            result.speed = Vehicle.getSpeed(vehicle);
            result.distanceDriven = Vehicle.getDistance(vehicle);
            result.heading = Vehicle.getAngle(vehicle);
            result.slope = Vehicle.getSlope(vehicle);
            result.acceleration = Vehicle.getAcceleration(vehicle);
            result.minGap = Vehicle.getMinGap(vehicle);

            TraCIPosition traCIPosition = Vehicle.getPosition(vehicle);
            result.position = new Position(CartesianPoint.xyz(traCIPosition.getX(), traCIPosition.getY(), traCIPosition.getZ() < -1000 ? 0 : traCIPosition.getZ()));

            result.stoppedStateEncoded = Vehicle.getStopState(vehicle);
            result.signalsEncoded = Vehicle.getSignals(vehicle);

            result.routeId = Vehicle.getRouteID(vehicle);

            result.edgeId = Vehicle.getRoadID(vehicle);
            result.lanePosition = Vehicle.getLanePosition(vehicle);
            result.lateralLanePosition = Vehicle.getLateralLanePosition(vehicle);

            result.co2 = Vehicle.getCO2Emission(vehicle);
            result.co = Vehicle.getCOEmission(vehicle);
            result.hc = Vehicle.getHCEmission(vehicle);
            result.pmx = Vehicle.getPMxEmission(vehicle);
            result.nox = Vehicle.getNOxEmission(vehicle);
            result.fuel = Vehicle.getFuelConsumption(vehicle);

            results.add(result);
        }

        StringVector arrived = Simulation.getArrivedIDList();

        for (int i = 0; i < arrived.size(); i++) {
            VEHICLE_SUBSCRIPTIONS.remove(arrived.get(i));
        }
    }


    private void readInductionLoops(List<AbstractSubscriptionResult> results) {
        StringVector vector = InductionLoop.getIDList();

        String inductionLoop;
        for (int i = 0; i < vector.size(); i++) {
            inductionLoop = vector.get(i);

            if (!INDUCTION_LOOP_SUBSCRIPTIONS.contains(inductionLoop)) {
                continue;
            }

            InductionLoopSubscriptionResult result = new InductionLoopSubscriptionResult();
            result.id = inductionLoop;
            result.meanSpeed = InductionLoop.getLastStepMeanSpeed(inductionLoop);
            result.meanVehicleLength = InductionLoop.getLastStepMeanLength(inductionLoop);
            result.vehiclesOnInductionLoop = new ArrayList<>();

            StringVector inductionLoopVehicles = InductionLoop.getLastStepVehicleIDs(inductionLoop);

            String vehicle;
            for (int v = 0; v < inductionLoopVehicles.size(); v++) {
                vehicle = vector.get(i);
                InductionLoopVehicleData vehicleData = new InductionLoopVehicleData();
                vehicleData.vehicleId = vehicle;
                //TODO add more vehicle data!! (use InductionLoop.getVehicleData(inductionLoop)
                result.vehiclesOnInductionLoop.add(vehicleData);
            }
            results.add(result);
        }
    }

    private void readLaneAreas(List<AbstractSubscriptionResult> results) {
        StringVector vector = LaneArea.getIDList();

        String laneArea;
        for (int i = 0; i < vector.size(); i++) {
            laneArea = vector.get(i);

            if (!LANE_AREA_SUBSCRIPTIONS.contains(laneArea)) {
                continue;
            }

            LaneAreaSubscriptionResult result = new LaneAreaSubscriptionResult();
            result.id = laneArea;
            result.vehicleCount = LaneArea.getLastStepVehicleNumber(laneArea);
            result.meanSpeed = LaneArea.getLastStepMeanSpeed(laneArea);
            result.haltingVehicles = LaneArea.getLastStepHaltingNumber(laneArea);
            result.length = LaneArea.getLength(laneArea);

            StringVector laneAreaVehicles = LaneArea.getLastStepVehicleIDs(laneArea);

            result.vehicles = new ArrayList<>((int) laneAreaVehicles.size());
            for (int v = 0; v < laneAreaVehicles.size(); v++) {
                result.vehicles.add(vector.get(i));
            }
            results.add(result);
        }
    }

    private void readTrafficLights(List<AbstractSubscriptionResult> results) {
        StringVector vector = TrafficLight.getIDList();

        String trafficLight;
        for (int i = 0; i < vector.size(); i++) {
            trafficLight = vector.get(i);

            if (!TRAFFIC_LIGHT_SUBSCRIPTIONS.contains(trafficLight)) {
                continue;
            }

            TrafficLightSubscriptionResult result = new TrafficLightSubscriptionResult();
            result.id = trafficLight;
            result.currentProgramId = TrafficLight.getProgram(trafficLight);
            result.assumedNextPhaseSwitchTime = (long) (TrafficLight.getNextSwitch(trafficLight) * TIME.SECOND);
            result.currentPhaseIndex = TrafficLight.getPhase(trafficLight);
            result.currentStateEncoded = TrafficLight.getRedYellowGreenState(trafficLight);
            results.add(result);
        }

    }
}
