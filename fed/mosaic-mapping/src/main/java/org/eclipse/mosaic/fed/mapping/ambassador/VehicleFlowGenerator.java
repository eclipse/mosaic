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

package org.eclipse.mosaic.fed.mapping.ambassador;

import org.eclipse.mosaic.fed.mapping.ambassador.spawning.VehicleTypeSpawner;
import org.eclipse.mosaic.fed.mapping.ambassador.spawning.flow.AbstractSpawningMode;
import org.eclipse.mosaic.fed.mapping.ambassador.spawning.flow.ConstantSpawningMode;
import org.eclipse.mosaic.fed.mapping.ambassador.spawning.flow.GrowAndShrinkSpawningMode;
import org.eclipse.mosaic.fed.mapping.ambassador.spawning.flow.PoissonSpawningMode;
import org.eclipse.mosaic.fed.mapping.ambassador.spawning.flow.SpawningMode;
import org.eclipse.mosaic.fed.mapping.ambassador.spawning.lane.HighwaySpecificLaneIndexSelector;
import org.eclipse.mosaic.fed.mapping.ambassador.spawning.lane.LaneIndexSelector;
import org.eclipse.mosaic.fed.mapping.ambassador.spawning.lane.RoundRobinLaneIndexSelector;
import org.eclipse.mosaic.fed.mapping.ambassador.weighting.DeterministicSelector;
import org.eclipse.mosaic.fed.mapping.ambassador.weighting.StochasticSelector;
import org.eclipse.mosaic.fed.mapping.ambassador.weighting.WeightedSelector;
import org.eclipse.mosaic.fed.mapping.config.CMappingConfiguration;
import org.eclipse.mosaic.fed.mapping.config.CPrototype;
import org.eclipse.mosaic.fed.mapping.config.units.CVehicle;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.interactions.mapping.advanced.RoutelessVehicleRegistration;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.objects.UnitNameGenerator;
import org.eclipse.mosaic.lib.objects.mapping.OriginDestinationPair;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleDeparture;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.Interaction;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Class for traffic stream generation.
 */
public class VehicleFlowGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(VehicleFlowGenerator.class);

    private final RandomNumberGenerator randomNumberGenerator;

    /**
     * List of lanes to be used. The vehicles will be evenly distributed among
     * the given lanes. When no value is given lane zero will be used for all
     * vehicles.
     */
    private final List<Integer> lanes;
    private final LaneIndexSelector laneSelector;
    private final SpawningMode spawningMode;
    private final List<VehicleTypeSpawner> types;
    private final int departureConnectionIndex;
    private final int pos;
    private final String route;
    private final GeoCircle origin;
    private final GeoCircle destination;
    private final OriginDestinationPair odInfo;
    private final String group;
    /**
     * The speed at which the vehicle will depart. Only used by SUMO so far.
     */
    private double departSpeed;
    /**
     * The depart speed mode, where depending on the value, the depart speed behaves as follows.
     *
     * <p>PRECISE = Use the value given in {@link CVehicle#departSpeed}
     * RANDOM = The {@link CVehicle#departSpeed} will be overridden by a random value
     * MAXIMUM = The {@link CVehicle#departSpeed} will be overridden by the max value
     */
    private VehicleDeparture.DepartureSpeedMode departureSpeedMode;
    private VehicleDeparture.LaneSelectionMode laneSelectionMode;
    private long start = 0;
    private long end = Long.MAX_VALUE;

    /**
     * Reference to the selector which will select the next vehicle type to be used.
     */
    private WeightedSelector<VehicleTypeSpawner> selector;
    /**
     * Running variable to determine when the next vehicle has to be spawned.
     */
    private long nextSpawnTime = -1;
    /**
     * Max number of vehicles that should be spawned in this flow.
     * Value of 1 means an individual vehicle, Integer.MAX_VALUE means an endless flow.
     */
    private int maxNumberVehicles;

    /**
     * Constructor for {@link VehicleFlowGenerator} using one vehicle type configuration.
     *
     * @param vehicleConfiguration vehicle spawner configuration
     */
    public VehicleFlowGenerator(CVehicle vehicleConfiguration, @Nonnull RandomNumberGenerator randomNumberGenerator, boolean flowNoise) {

        // Enforce that types are defined
        if (vehicleConfiguration.types == null || vehicleConfiguration.types.isEmpty()) {
            throw new IllegalArgumentException("Missing vehicle type definition in vehicle!");
        }
        this.randomNumberGenerator = randomNumberGenerator;

        // set simple values
        this.departureConnectionIndex = vehicleConfiguration.departConnectionIndex;
        this.pos = vehicleConfiguration.pos;
        this.route = vehicleConfiguration.route;
        this.group = vehicleConfiguration.group;
        this.departSpeed = vehicleConfiguration.departSpeed;
        this.departureSpeedMode = vehicleConfiguration.departSpeedMode;
        this.laneSelectionMode = vehicleConfiguration.laneSelectionMode;
        // If maxNumberVehicles wasn't given in mapping, we assume that it should be an endless flow, so we set it to Integer.MAX_VALUE
        // and handle this case accordingly in the timeAdvance() method
        this.maxNumberVehicles = ObjectUtils.defaultIfNull(vehicleConfiguration.maxNumberVehicles, Integer.MAX_VALUE);

        // create prototypes
        this.types = createPrototypes(vehicleConfiguration);
        // create SpawningMode using given definitions
        this.spawningMode = createSpawningMode(vehicleConfiguration, randomNumberGenerator, flowNoise);
        // create the selector deciding which vehicle is spawned next
        this.selector = createSelector(vehicleConfiguration, randomNumberGenerator);
        // create lane index list
        this.lanes = createLanes(vehicleConfiguration);
        // create lane selector deciding which lane the next vehicle is spawned on
        laneSelector = createLaneSelector(lanes, laneSelectionMode);

        // origin-destination info only if both are given actually set them
        if ((vehicleConfiguration.origin != null) && (vehicleConfiguration.destination != null)) {
            this.origin = vehicleConfiguration.origin;
            this.destination = vehicleConfiguration.destination;
            this.odInfo = new OriginDestinationPair(origin, destination);
        } else {
            this.origin = null;
            this.destination = null;
            this.odInfo = null;
        }

        LOG.debug("New VehicleStreamGenerator created: " + this);
    }

    private List<VehicleTypeSpawner> createPrototypes(CVehicle vehicleConfiguration) {
        List<VehicleTypeSpawner> types = new ArrayList<>();
        for (CPrototype prototypeConfiguration : vehicleConfiguration.types) {
            if (prototypeConfiguration != null) {
                // Generate internal type
                VehicleTypeSpawner vehicleType = new VehicleTypeSpawner(prototypeConfiguration);
                types.add(vehicleType);
            }
        }
        return types;
    }

    /**
     * This method takes all necessary parameters to create a {@link SpawningMode} and
     * returns the definition of that {@link SpawningMode}.
     *
     * @param vehicleConfiguration  the vehicle configuration containing necessary information to determine the {@link SpawningMode}
     * @param randomNumberGenerator the {@link RandomNumberGenerator} to be used, for example to introduce flowNoise
     * @param flowNoise             whether flow noise should be introduced
     * @return the created {@link SpawningMode}
     */
    private SpawningMode createSpawningMode(
            CVehicle vehicleConfiguration, RandomNumberGenerator randomNumberGenerator, boolean flowNoise) {

        CVehicle.SpawningMode spawningMode = vehicleConfiguration.spawningMode;
        long startingTime = (long) vehicleConfiguration.startingTime * TIME.SECOND;
        // if no maxTime was given determine it by dividing the maximum amount of vehicles by the desired flow and adding the start time
        long maxTime;
        if (vehicleConfiguration.maxTime == null) {
            maxTime = (long) (((double) maxNumberVehicles / vehicleConfiguration.targetFlow) * (double) TIME.HOUR)
                    + (long) vehicleConfiguration.startingTime * TIME.SECOND;
        } else {
            maxTime = vehicleConfiguration.maxTime.longValue() * TIME.SECOND;
        }
        double targetFlow = vehicleConfiguration.targetFlow;
        SpawningMode newSpawningMode;
        switch (spawningMode) {
            case GROW:
                newSpawningMode = new AbstractSpawningMode.IncreaseLinear(
                        flowNoise ? randomNumberGenerator : null,
                        startingTime,
                        targetFlow,
                        maxTime
                );
                break;
            case SHRINK:
                newSpawningMode = new AbstractSpawningMode.DecreaseLinear(
                        flowNoise ? randomNumberGenerator : null,
                        startingTime,
                        targetFlow,
                        maxTime
                );
                break;
            case GROW_AND_SHRINK:
                newSpawningMode = new GrowAndShrinkSpawningMode(
                        flowNoise ? randomNumberGenerator : null,
                        startingTime,
                        targetFlow,
                        maxTime,
                        false
                );
                break;
            case GROW_EXPONENTIAL:
                newSpawningMode = new AbstractSpawningMode.IncreaseExponential(
                        flowNoise ? randomNumberGenerator : null,
                        startingTime,
                        targetFlow,
                        maxTime
                );
                break;
            case SHRINK_EXPONENTIAL:
                newSpawningMode = new AbstractSpawningMode.DecreaseExponential(
                        flowNoise ? randomNumberGenerator : null,
                        startingTime,
                        targetFlow,
                        maxTime
                );
                break;
            case GROW_AND_SHRINK_EXPONENTIAL:
                newSpawningMode = new GrowAndShrinkSpawningMode(
                        flowNoise ? randomNumberGenerator : null,
                        startingTime,
                        targetFlow,
                        maxTime,
                        true
                );
                break;
            case POISSON:
                newSpawningMode = new PoissonSpawningMode(
                        randomNumberGenerator,
                        startingTime,
                        targetFlow,
                        maxTime
                );
                break;
            case CONSTANT:
            default:
                newSpawningMode = new ConstantSpawningMode(
                        flowNoise ? randomNumberGenerator : null,
                        startingTime,
                        targetFlow,
                        maxTime
                );
        }
        return newSpawningMode;
    }

    private WeightedSelector<VehicleTypeSpawner> createSelector(
            CVehicle vehicleConfiguration, RandomNumberGenerator randomNumberGenerator) {
        if (types.size() == 1) {
            selector = () -> Iterables.getOnlyElement(types);
        } else if (vehicleConfiguration.deterministic) {
            selector = new DeterministicSelector<>(types, randomNumberGenerator);
        } else {
            selector = new StochasticSelector<>(types, randomNumberGenerator);
        }
        return selector;
    }

    private List<Integer> createLanes(CVehicle vehicleConfiguration) {
        List<Integer> lanes = new ArrayList<>();
        if (vehicleConfiguration.lanes != null) {
            for (Integer laneIdx : vehicleConfiguration.lanes) {
                if (laneIdx != null) {
                    lanes.add(laneIdx);
                }
            }
        }
        // if no lanes were given just use lane index 0
        if (lanes.isEmpty()) {
            lanes.add(0);
        }
        return lanes;
    }

    private LaneIndexSelector createLaneSelector(List<Integer> lanes, VehicleDeparture.LaneSelectionMode laneSelectionMode) {
        switch (laneSelectionMode) {
            case DEFAULT:
            case ROUNDROBIN:
                return new RoundRobinLaneIndexSelector(lanes);
            case ROUNDROBIN_HIGHWAY:
                return new HighwaySpecificLaneIndexSelector(lanes);
            default:
                // return invalid lane index -1 and let the traffic simulator decide based on traffic condition
                return (type) -> -1;
        }
    }

    void configure(CMappingConfiguration mappingParameterizationConfiguration) {
        if (mappingParameterizationConfiguration.start != null) {
            this.start = Double.valueOf(mappingParameterizationConfiguration.start * TIME.SECOND).longValue();
        }
        if (mappingParameterizationConfiguration.end != null) {
            this.end = Double.valueOf(mappingParameterizationConfiguration.end * TIME.SECOND).longValue();
        }
        Validate.isTrue(this.end > this.start);
    }

    void fillInPrototype(SpawningFramework framework) {
        for (VehicleTypeSpawner vehicleTypeSpawner : types) {
            CPrototype prototypeConfiguration = framework.getPrototypeByName(vehicleTypeSpawner.getPrototype());
            if (prototypeConfiguration == null) {
                continue;
            }
            vehicleTypeSpawner.fillInPrototype(prototypeConfiguration);
        }
    }

    void collectVehicleTypes(HashMap<String, VehicleType> types) {
        for (VehicleTypeSpawner vehicleTypeSpawner : this.types) {
            String key = vehicleTypeSpawner.getPrototype();
            if (types.containsKey(key) && (vehicleTypeSpawner.convertType().equals(types.get(key)))) {
                continue;
            }
            if ((key == null) || types.containsKey(key)) {
                key = UnitNameGenerator.nextPrototypeName(vehicleTypeSpawner.getPrototype());
                vehicleTypeSpawner.setPrototype(key);
            }
            types.put(key, vehicleTypeSpawner.convertType());
            LOG.trace("Registering Vehicle Type: " + key + ": " + types.get(key));
        }
    }

    /**
     * This contains the main logic of vehicle spawning.
     *
     * @param framework the {@link SpawningFramework} handling the time advance
     * @return true if there is no more vehicles to spawn or max time reached, thus the vehicle spawner can be removed
     * @throws InternalFederateException thrown if time advance couldn't be completed successfully
     */
    boolean timeAdvance(SpawningFramework framework) throws InternalFederateException {
        // to reduce load, first handle everything that might stop execution
        // =================================================================
        if (!spawningMode.isSpawningActive(framework.getTime())) {
            return true;
        }
        // if there are no more vehicles left to be spawned: destroy
        // note that numbers below zero will lead to the number being ignored
        if (maxNumberVehicles == 0) {
            return true;
        }
        // now determine if a vehicle has to be spawned
        // =================================================================
        // init some variables on the first time advance
        if (nextSpawnTime == -1) {
            nextSpawnTime = spawningMode.getNextSpawningTime(framework.getTime());
            try {
                framework.getRti().requestAdvanceTime(nextSpawnTime);
            } catch (IllegalValueException e) {
                LOG.error("Exception while requesting time advance in VehicleStreamGenerator.timeAdvance()", e);
                throw new InternalFederateException("Exception while requesting time advance in VehicleStreamGenerator.timeAdvance()", e);
            }
        }
        // check if we really need to spawn something right now
        if (nextSpawnTime != framework.getTime()) {
            return false;
        }

        nextSpawnTime = spawningMode.getNextSpawningTime(framework.getTime());
        try {
            framework.getRti().requestAdvanceTime(nextSpawnTime);
        } catch (IllegalValueException e) {
            LOG.error("Exception in VehicleStreamGenerator.timeAdvance()", e);
            throw new InternalFederateException("Exception in VehicleStreamGenerator.timeAdvance()", e);
        }

        // If maxNumberVehicles wasn't given in mapping, we assume that it should be an endless flow, so
        // we set it to Integer.MAX_VALUE and don't reduce the max number of vehicles in this case
        if (maxNumberVehicles > 0 && maxNumberVehicles != Integer.MAX_VALUE) {
            maxNumberVehicles--;
        }

        LOG.debug("TimerCall Spawner. Time=" + framework.getTime() + ", nextTime=" + nextSpawnTime);

        // determine the type of the vehicle to spawn by use of the selector
        // (either deterministic or stochastic, determined in constructor)
        VehicleTypeSpawner type = selector.nextItem();
        String name = UnitNameGenerator.nextVehicleName();


        createVehicle(framework, name, group, laneSelector.nextLane(type), type);

        return false;
    }

    private boolean notInTimeFrame(long time) {
        return time < start || time >= end;
    }

    private void createVehicle(SpawningFramework framework, String name, String group, int lane, VehicleTypeSpawner type)
            throws InternalFederateException {

        if (notInTimeFrame(framework.getTime())) {
            LOG.info("Omit vehicle spawner at time {} (not in time span)", framework.getTime());
            return;
        }
        // if no group is defined in vehicle definition take group declared in prototype
        group = ObjectUtils.defaultIfNull(group, type.getGroup());

        VehicleDeparture vehicleDeparture = new VehicleDeparture.Builder(route)
                .departureLane(laneSelectionMode, lane, pos)
                .departureConnection(departureConnectionIndex)
                .departureSpeed(departureSpeedMode, departSpeed)
                .create();

        Interaction interaction;
        if (origin != null) {
            interaction = new RoutelessVehicleRegistration(
                    framework.getTime(), name, group, type.getAppList(), vehicleDeparture, type.convertType(), odInfo
            );
        } else {
            interaction = new VehicleRegistration(framework.getTime(), name, group, type.getAppList(), vehicleDeparture,
                    type.convertTypeAndVaryParameters(randomNumberGenerator)
            );
        }

        try {
            LOG.info("Creating Vehicle. time={}, name={}, route={}, laneSelectionMode={}, lane={}, departureConnectionIndex{}, pos={}, "
                            + "type={}, departSpeed={}, apps={}",
                    framework.getTime(), name, route, laneSelectionMode, lane, departureConnectionIndex, pos, type, departSpeed, type.getAppList());
            framework.getRti().triggerInteraction(interaction);
        } catch (IllegalValueException e) {
            LOG.error("Couldn't send an {} interaction in VehicleStreamGenerator.timeAdvance()", interaction.getTypeId(), e);
            throw new InternalFederateException("Exception in VehicleStreamGenerator.timeAdvance()", e);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("spawningMode", spawningMode)
                .append("lanes", lanes)
                .append("types", types)
                .append("departureConnectionIndex", departureConnectionIndex)
                .append("pos", pos)
                .append("departSpeed", departSpeed)
                .append("route", route)
                .append("group", group)
                .append("origin", origin)
                .append("destination", destination)
                .append("odInfo", odInfo)
                .toString();
    }
}
