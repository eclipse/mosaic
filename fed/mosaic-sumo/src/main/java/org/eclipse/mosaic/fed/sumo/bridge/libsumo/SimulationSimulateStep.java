/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.LeadFollowVehicle;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.TrafficLightSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.VehicleContextSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.VehicleSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.bridge.traci.constants.CommandRetrieveVehicleState;
import org.eclipse.mosaic.fed.sumo.config.CSumo;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.util.objects.Position;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.sumo.libsumo.ContextSubscriptionResults;
import org.eclipse.sumo.libsumo.InductionLoop;
import org.eclipse.sumo.libsumo.LaneArea;
import org.eclipse.sumo.libsumo.Simulation;
import org.eclipse.sumo.libsumo.StringDoublePair;
import org.eclipse.sumo.libsumo.TraCIPosition;
import org.eclipse.sumo.libsumo.TraCIVehicleData;
import org.eclipse.sumo.libsumo.TrafficLight;
import org.eclipse.sumo.libsumo.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SimulationSimulateStep implements org.eclipse.mosaic.fed.sumo.bridge.api.SimulationSimulateStep {

    private static final Logger LOG = LoggerFactory.getLogger(SimulationSimulateStep.class);

    final static List<String> VEHICLE_SUBSCRIPTIONS = new ArrayList<>();
    final static List<String> INDUCTION_LOOP_SUBSCRIPTIONS = new ArrayList<>();
    final static List<String> LANE_AREA_SUBSCRIPTIONS = new ArrayList<>();
    final static List<String> TRAFFIC_LIGHT_SUBSCRIPTIONS = new ArrayList<>();

    private final boolean fetchEmissions;
    private final boolean fetchLeader;
    private final boolean fetchSignals;

    public SimulationSimulateStep(Bridge ignored, CSumo sumoConfiguration) {
        VEHICLE_SUBSCRIPTIONS.clear();
        INDUCTION_LOOP_SUBSCRIPTIONS.clear();
        LANE_AREA_SUBSCRIPTIONS.clear();
        TRAFFIC_LIGHT_SUBSCRIPTIONS.clear();

        fetchEmissions = sumoConfiguration.subscriptions.contains(CSumo.SUBSCRIPTION_EMISSIONS);
        fetchSignals = sumoConfiguration.subscriptions.contains(CSumo.SUBSCRIPTION_SIGNALS);
        fetchLeader = sumoConfiguration.subscriptions.contains(CSumo.SUBSCRIPTION_LEADER);
    }

    public SimulationSimulateStep() {
        VEHICLE_SUBSCRIPTIONS.clear();
        INDUCTION_LOOP_SUBSCRIPTIONS.clear();
        LANE_AREA_SUBSCRIPTIONS.clear();
        TRAFFIC_LIGHT_SUBSCRIPTIONS.clear();

        fetchEmissions = true;
        fetchSignals = true;
        fetchLeader = true;
    }

    public List<AbstractSubscriptionResult> execute(Bridge bridge, long time) throws CommandException, InternalFederateException {
        Simulation.step((double) (time) / TIME.SECOND);

        final List<AbstractSubscriptionResult> results = new ArrayList<>();
        readVehicles(results);
        readSurroundingVehiclesFromContextSubscriptions(results);
        readInductionLoops(results);
        readLaneAreas(results);
        readTrafficLights(results);

        return results;

    }

    private void readVehicles(List<AbstractSubscriptionResult> results) {
        for (String arrived : Simulation.getArrivedIDList()) {
            VEHICLE_SUBSCRIPTIONS.remove(Bridge.VEHICLE_ID_TRANSFORMER.fromExternalId(arrived));
        }

        String sumoVehicleId;
        for (String mosaicVehicleId : VEHICLE_SUBSCRIPTIONS) {
            sumoVehicleId = Bridge.VEHICLE_ID_TRANSFORMER.toExternalId(mosaicVehicleId);

            VehicleSubscriptionResult result = new VehicleSubscriptionResult();
            result.id = mosaicVehicleId;

            result.position = getPosition(Vehicle.getPosition(sumoVehicleId));
            if (result.position == null) {
                continue;
            }

            result.speed = Vehicle.getSpeed(sumoVehicleId);
            result.distanceDriven = Vehicle.getDistance(sumoVehicleId);
            result.heading = Vehicle.getAngle(sumoVehicleId);
            result.slope = Vehicle.getSlope(sumoVehicleId);
            result.acceleration = Vehicle.getAcceleration(sumoVehicleId);
            result.minGap = Vehicle.getMinGap(sumoVehicleId);


            result.stoppedStateEncoded = Vehicle.getStopState(sumoVehicleId);

            if (fetchSignals) {
                result.signalsEncoded = Vehicle.getSignals(sumoVehicleId);
            }

            result.routeId = Vehicle.getRouteID(sumoVehicleId);

            result.edgeId = Vehicle.getRoadID(sumoVehicleId);
            result.lanePosition = Vehicle.getLanePosition(sumoVehicleId);
            result.lateralLanePosition = Vehicle.getLateralLanePosition(sumoVehicleId);
            result.laneIndex = Vehicle.getLaneIndex(sumoVehicleId);

            if (fetchEmissions) {
                result.co2 = Vehicle.getCO2Emission(sumoVehicleId);
                result.co = Vehicle.getCOEmission(sumoVehicleId);
                result.hc = Vehicle.getHCEmission(sumoVehicleId);
                result.pmx = Vehicle.getPMxEmission(sumoVehicleId);
                result.nox = Vehicle.getNOxEmission(sumoVehicleId);
                result.fuel = Vehicle.getFuelConsumption(sumoVehicleId);
            }

            result.leadingVehicle = LeadFollowVehicle.NONE;
            result.followerVehicle = LeadFollowVehicle.NONE;

            if (fetchLeader) {
                final StringDoublePair leader = Vehicle.getLeader(sumoVehicleId);
                if (StringUtils.isNotBlank(leader.getFirst())) {
                    result.leadingVehicle = new LeadFollowVehicle(
                            Bridge.VEHICLE_ID_TRANSFORMER.fromExternalId(leader.getFirst()),
                            leader.getSecond()
                    );
                }

                final StringDoublePair follower = Vehicle.getFollower(sumoVehicleId);
                if (StringUtils.isNotBlank(follower.getFirst())) {
                    result.followerVehicle = new LeadFollowVehicle(
                            Bridge.VEHICLE_ID_TRANSFORMER.fromExternalId(follower.getFirst()),
                            follower.getSecond()
                    );
                }
            }

            if (fetchLeader) {
                final StringDoublePair leader = Vehicle.getLeader(sumoVehicleId);
                if (StringUtils.isNotBlank(leader.getFirst())) {
                    result.leadingVehicle = new LeadingVehicle(
                            Bridge.VEHICLE_ID_TRANSFORMER.fromExternalId(leader.getFirst()),
                            leader.getSecond()
                    );
                }
            }

            results.add(result);
        }
    }

    private Position getPosition(TraCIPosition traCIPosition) {
        if (traCIPosition.getX() < -1000 && traCIPosition.getY() < -1000) {
            return null;
        }
        return new Position(CartesianPoint.xyz(traCIPosition.getX(), traCIPosition.getY(), traCIPosition.getZ() < -1000 ? 0 : traCIPosition.getZ()));
    }

    private void readSurroundingVehiclesFromContextSubscriptions(List<AbstractSubscriptionResult> results) {
        ContextSubscriptionResults contextResults = Vehicle.getAllContextSubscriptionResults();
        contextResults.forEach((sumoId, subscriptionResults) -> {
            final VehicleContextSubscriptionResult result = new VehicleContextSubscriptionResult();
            result.id = Bridge.VEHICLE_ID_TRANSFORMER.fromExternalId(sumoId);
            subscriptionResults.forEach((childSumoId, childSubscriptionResults) -> {
                if (childSumoId.equals(sumoId)) {
                    return;
                }
                final VehicleSubscriptionResult surroundingVehicle = new VehicleSubscriptionResult();
                surroundingVehicle.id = Bridge.VEHICLE_ID_TRANSFORMER.fromExternalId(childSumoId);
                try {
                    /* FIXME Work-around (09-02-2022):
                     * The objects returned here cannot be cast to their actual type (e.g. TraCIDouble or TraCIPosition).
                     * Therefore we currently just parse their string output.
                     * See https://github.com/eclipse/sumo/issues/8930
                     */
                    surroundingVehicle.position = parsePosition(childSubscriptionResults.get(CommandRetrieveVehicleState.VAR_POSITION_3D.var).getString());
                    surroundingVehicle.speed = parseDouble(childSubscriptionResults.get(CommandRetrieveVehicleState.VAR_SPEED.var).getString());
                    surroundingVehicle.heading = parseDouble(childSubscriptionResults.get(CommandRetrieveVehicleState.VAR_ANGLE.var).getString());

                    if (surroundingVehicle.position != null) {
                        result.contextSubscriptions.add(surroundingVehicle);
                    }
                } catch (Throwable e) {
                    LOG.warn("Cannot read surrounding vehicle.", e);
                }
            });
            if (!result.contextSubscriptions.isEmpty()) {
                results.add(result);
            }
        });
    }

    private final static Pattern PATTERN_POSITION = Pattern.compile("TraCIPosition\\(([0-9.]+), ?([0-9.]+), ?([0-9.]+)\\)");

    private Position parsePosition(String in) {
        Matcher matcher = PATTERN_POSITION.matcher(in);
        if (matcher.find()) {
            return new Position(CartesianPoint.xyz(parseDouble(matcher.group(1)), parseDouble(matcher.group(2)), parseDouble(matcher.group(3))));
        }
        return null;
    }

    private double parseDouble(String in) {
        return Double.parseDouble(in);
    }


    private void readInductionLoops(List<AbstractSubscriptionResult> results) {
        for (String inductionLoop : INDUCTION_LOOP_SUBSCRIPTIONS) {
            InductionLoopSubscriptionResult result = new InductionLoopSubscriptionResult();
            result.id = inductionLoop;
            result.meanSpeed = InductionLoop.getLastStepMeanSpeed(inductionLoop);
            result.meanVehicleLength = InductionLoop.getLastStepMeanLength(inductionLoop);
            result.vehiclesOnInductionLoop = new ArrayList<>();

            for (TraCIVehicleData inductionLoopVeh : InductionLoop.getVehicleData(inductionLoop)) {
                InductionLoopVehicleData vehicleData = new InductionLoopVehicleData();
                vehicleData.vehicleId = Bridge.VEHICLE_ID_TRANSFORMER.fromExternalId(inductionLoopVeh.getId());
                vehicleData.entryTime = (long) (inductionLoopVeh.getEntryTime() * TIME.NANO_SECOND);
                vehicleData.leaveTime = (long) (inductionLoopVeh.getLeaveTime() * TIME.NANO_SECOND);
                result.vehiclesOnInductionLoop.add(vehicleData);
            }
            results.add(result);
        }
    }

    private void readLaneAreas(List<AbstractSubscriptionResult> results) {
        for (String laneArea : LANE_AREA_SUBSCRIPTIONS) {
            LaneAreaSubscriptionResult result = new LaneAreaSubscriptionResult();
            result.id = laneArea;
            result.vehicleCount = LaneArea.getLastStepVehicleNumber(laneArea);
            result.meanSpeed = LaneArea.getLastStepMeanSpeed(laneArea);
            result.haltingVehicles = LaneArea.getLastStepHaltingNumber(laneArea);
            result.length = LaneArea.getLength(laneArea);

            for (String vehicle : LaneArea.getLastStepVehicleIDs(laneArea)) {
                result.vehicles.add(Bridge.VEHICLE_ID_TRANSFORMER.fromExternalId(vehicle));
            }
            results.add(result);
        }
    }

    private void readTrafficLights(List<AbstractSubscriptionResult> results) {
        for (String trafficLight : TRAFFIC_LIGHT_SUBSCRIPTIONS) {
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
