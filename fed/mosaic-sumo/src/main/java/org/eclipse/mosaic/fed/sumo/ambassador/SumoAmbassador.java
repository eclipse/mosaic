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

import org.eclipse.mosaic.fed.sumo.util.SumoRouteFileCreator;
import org.eclipse.mosaic.fed.sumo.util.SumoVehicleClassMapping;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.interactions.traffic.VehicleRoutesInitialization;
import org.eclipse.mosaic.interactions.traffic.VehicleTypesInitialization;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.interactions.vehicle.VehicleRouteRegistration;
import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.objects.mapping.VehicleMapping;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.lib.util.XmlUtils;
import org.eclipse.mosaic.rti.api.FederateAmbassador;
import org.eclipse.mosaic.rti.api.Interaction;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.parameters.AmbassadorParameter;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of a {@link AbstractSumoAmbassador} for the traffic simulator
 * SUMO. It allows to control the progress of the traffic simulation and
 * publishes {@link VehicleUpdates}.
 *
 * @see FederateAmbassador
 * @see VehicleUpdates
 */
public class SumoAmbassador extends AbstractSumoAmbassador {

    /**
     * Caches the {@link VehicleRoutesInitialization}-interaction until the {@link VehicleTypesInitialization}-interaction
     * is received.
     */
    private VehicleRoutesInitialization cachedVehicleRoutesInitialization;

    /**
     * Caches the {@link VehicleTypesInitialization}-interaction if it is received before
     * the {@link VehicleRoutesInitialization}-interaction.
     */
    private VehicleTypesInitialization cachedVehicleTypesInitialization;

    /**
     * Name of the route file which has to be altered.
     */
    private final String routeFilename;

    /**
     * Cached {@link VehicleRegistration}-interaction, which is filled if a vehicle could not be
     * emitted e.g. due a blocked lane.
     */
    private final List<VehicleRegistration> notYetAddedVehicles = new ArrayList<>();

    /**
     * Map of vehicle types initially send to the ambassador.
     */
    private final Map<String, VehicleType> initialTypes = new HashMap<>();


    /**
     * This contains all routes known to sumo.
     */
    private final Set<String> sumoKnownRoutes = new HashSet<>();

    /**
     * Instance of {@link SumoRouteFileCreator} used to write routes to a *.rou.xml file.
     */
    private SumoRouteFileCreator sumoRouteFileCreator;

    /**
     * Constructor for {@link SumoAmbassador}. Loads the
     * SUMO-configuration, and the SUMO-route file.
     *
     * @param ambassadorParameter parameters for the ambassador containing its' id and configuration
     */
    public SumoAmbassador(AmbassadorParameter ambassadorParameter) {
        super(ambassadorParameter);

        File absoluteConfiguration = new File(sumoConfig.sumoConfigurationFile);
        if (!absoluteConfiguration.exists()) {
            absoluteConfiguration = new File(this.ambassadorParameter.configuration.getParent(), sumoConfig.sumoConfigurationFile);
        }

        routeFilename = parseRouteFilename(absoluteConfiguration);

        if (StringUtils.isBlank(routeFilename)) {
            String myError = "The tag <route-files value=\"*.rou.xml\"/> is missing in " + absoluteConfiguration.getAbsolutePath();
            log.error(myError);
            throw new RuntimeException(myError);
        }
    }

    /**
     * Find the name of the route file and return it.
     *
     * @param sumoConfigurationFile The SUMO configuration file.
     * @return The route-files filename.
     */
    private String parseRouteFilename(File sumoConfigurationFile) {

        try {
            XMLConfiguration sumoConfiguration = XmlUtils.readXmlFromFile(sumoConfigurationFile);
            String routeFile = XmlUtils.getValueFromXpath(sumoConfiguration, "/input/route-files/@value", null);
            return Validate.notNull(routeFile);
        } catch (IOException e) {
            log.error("An error occurred while parsing the SUMO configuration file", e);
        }

        return null;
    }

    @Override
    public void initialize(long startTime, long endTime) throws InternalFederateException {
        super.initialize(startTime, endTime);
    }

    /**
     * This method processes the interaction.
     *
     * @param interaction The interaction that can be processed.
     * @throws InternalFederateException if an interaction could not be received properly
     */
    @Override
    public synchronized void processInteraction(Interaction interaction) throws InternalFederateException {
        // Init and VehicleRegistration are processed directly...
        if (interaction.getTypeId().equals(VehicleRoutesInitialization.TYPE_ID)) {
            this.receiveInteraction((VehicleRoutesInitialization) interaction);
        } else if (interaction.getTypeId().equals(VehicleTypesInitialization.TYPE_ID)) {
            this.receiveInteraction((VehicleTypesInitialization) interaction);
        } else if (interaction.getTypeId().equals(VehicleRegistration.TYPE_ID)) {
            this.receiveInteraction((VehicleRegistration) interaction);
        } else {
            // ... everything else is saved for later
            super.processInteraction(interaction);
        }
    }

    /**
     * This processes all other types of interactions as part of {@link #processTimeAdvanceGrant(long)}.
     *
     * @param interaction The interaction to process.
     * @param time        The time of the processed interaction.
     * @throws InternalFederateException Exception is thrown if the interaction time is not correct.
     */
    @Override
    protected void processInteractionAdvanced(Interaction interaction, long time) throws InternalFederateException {
        // make sure the interaction is not in the future
        if (interaction.getTime() > time) {
            throw new InternalFederateException(
                    "Interaction time lies in the future:" + interaction.getTime()
                            + " current time:" + time
            );
        }

        if (interaction.getTypeId().equals(VehicleRouteRegistration.TYPE_ID)) {
            receiveInteraction((VehicleRouteRegistration) interaction);
        } else {
            super.processInteractionAdvanced(interaction, time);
        }
    }

    /**
     * Handles the {@link VehicleRegistration}-registration and adds the vehicle to the current
     * simulation.
     *
     * @param interaction {@link VehicleRegistration} containing the vehicle definition.
     */
    private void receiveInteraction(VehicleRegistration interaction) {
        VehicleMapping av = interaction.getMapping();
        log.info("VehicleRegistration \"{}\" received at simulation time {} ns", av.getName(), interaction.getTime());
        notYetAddedVehicles.add(interaction);
    }

    /**
     * This processes a {@link VehicleRouteRegistration} that have been dynamically created.
     *
     * @param interaction Interaction containing information about an added route.
     */
    private void receiveInteraction(VehicleRouteRegistration interaction) throws InternalFederateException {
        VehicleRoute newRoute = interaction.getRoute();
        routeCache.put(newRoute.getId(), newRoute);
        if (!sumoKnownRoutes.contains(newRoute.getId())) {
            sumoKnownRoutes.add(newRoute.getId());
            traci.getRouteControl().addRoute(newRoute.getId(), newRoute.getEdgeIdList());
            log.debug("received newly propagated route {}", newRoute.getId());
        } else {
            log.debug("route has already been added to SUMO, ignoring id={}", newRoute.getId());
        }
    }

    /**
     * Extract data from the {@link VehicleRoutesInitialization} to SUMO.
     *
     * @param interaction interaction containing vehicle departures and pre calculated routes for change route requests.
     * @throws InternalFederateException if something goes wrong in startSumoLocal(), initTraci(), completeRoutes() or readRouteFromTraci()
     */
    private void receiveInteraction(VehicleRoutesInitialization interaction) throws InternalFederateException {
        log.debug("Received VehicleRoutesInitialization: {}", interaction.getTime());

        for (VehicleRoute route : interaction.getRoutes().values()) {
            sumoKnownRoutes.add(route.getId());
            routeCache.put(route.getId(), route);
        }

        cachedVehicleRoutesInitialization = interaction;
        if (sumoReadyToStart()) {
            sumoStartingProcedure();
        }
    }

    /**
     * Extract data from the {@link VehicleTypesInitialization} and forward to SUMO.
     *
     * @param interaction interaction containing vehicle types
     * @throws InternalFederateException if something goes wrong in startSumoLocal(), initTraci() or completeRoutes()
     */
    private synchronized void receiveInteraction(VehicleTypesInitialization interaction) throws InternalFederateException {
        log.debug("Received VehicleTypesInitialization");

        cachedVehicleTypesInitialization = interaction;
        if (sumoReadyToStart()) {
            sumoStartingProcedure();
        }
    }

    private boolean sumoReadyToStart() {
        return descriptor != null && cachedVehicleRoutesInitialization != null && cachedVehicleTypesInitialization != null;
    }

    private void sumoStartingProcedure() throws InternalFederateException {
        writeRouteFile(cachedVehicleTypesInitialization, cachedVehicleRoutesInitialization);
        startSumoLocal();
        initTraci();
        completeRoutes();
    }

    private void completeRoutes() throws InternalFederateException {
        for (String id : traci.getRouteControl().getRouteIds()) {
            if (!sumoKnownRoutes.contains(id)) {
                VehicleRoute route = readRouteFromTraci(id);
                sumoKnownRoutes.add(route.getId());
                routeCache.put(route.getId(), route);
            }
        }
    }
    /**
     * Vehicles of the {@link #notYetAddedVehicles} list will be added to simulation by this function
     * or cached again for the next time advance.
     *
     * @param time Current system time
     * @throws InternalFederateException if vehicle couldn't be added
     */
    @Override
    protected void flushNotYetAddedVehicles(long time) throws InternalFederateException {
        // if not yet a last advance time was set, sumo is not ready
        if (time < 0) {
            return;
        }
        for (Iterator<VehicleRegistration> iterator = notYetAddedVehicles.iterator(); iterator.hasNext(); ) {
            VehicleRegistration interaction = iterator.next();

            String vehicleId = interaction.getMapping().getName();
            String vehicleType = interaction.getMapping().getVehicleType().getName();
            String routeId = interaction.getDeparture().getRouteId();
            String departPos = String.format(Locale.ENGLISH, "%.2f", interaction.getDeparture().getDeparturePos());
            String departSpeed = extractDepartureSpeed(interaction);
            String laneId = extractDepartureLane(interaction);

            try {
                if (interaction.getTime() <= time) {
                    log.info("Adding new vehicle \"{}\" at simulation time {} ns (type={}, routeId={}, laneId={}, departPos={})",
                            vehicleId, interaction.getTime(), vehicleType, routeId, laneId, departPos);

                    if (!sumoKnownRoutes.contains(routeId)) {
                        throw new IllegalArgumentException(
                                "Unknown route " + routeId + " for vehicle with departure time " + interaction.getTime()
                        );
                    }
                    sumoKnownRoutes.add(routeId);

                    traci.getSimulationControl().addVehicle(vehicleId, routeId, vehicleType, laneId, departPos, departSpeed);
                    if (sumoConfig.subscribeToAllVehicles || interaction.getMapping().hasApplication()) {
                        traci.getSimulationControl().subscribeForVehicle(vehicleId, interaction.getTime(), this.getEndTime());
                    }

                    applyChangesInVehicleTypeForVehicle(
                            vehicleId,
                            interaction.getMapping().getVehicleType(),
                            initialTypes.get(vehicleType)
                    );

                    if (sumoConfig.writeVehicleDepartures) {
                        sumoRouteFileCreator.addVehicle(time, vehicleId, vehicleType, routeId, laneId, departPos, departSpeed);
                    }
                    iterator.remove();
                }
            } catch (InternalFederateException e) {
                log.warn("Vehicle with id: " + vehicleId + " could not be added.(" + e.getClass().getCanonicalName() + ")", e);
                if (sumoConfig.exitOnInsertionError) {
                    throw e;
                }
                iterator.remove();
            }

        }
    }

    private void applyChangesInVehicleTypeForVehicle(String vehicleId, VehicleType actualVehicleType, VehicleType baseVehicleType) throws InternalFederateException {
        if (!eq(actualVehicleType.getTau(), baseVehicleType.getTau())) {
            double minReactionTime = sumoConfig.updateInterval / 1000d;
            traci.getVehicleControl().setReactionTime(
                    vehicleId, Math.max(minReactionTime, actualVehicleType.getTau() + sumoConfig.timeGapOffset)
            );
        }
        if (!eq(actualVehicleType.getMaxSpeed(), baseVehicleType.getMaxSpeed())) {
            traci.getVehicleControl().setMaxSpeed(vehicleId, actualVehicleType.getMaxSpeed());
        }
        if (!eq(actualVehicleType.getAccel(), baseVehicleType.getAccel())) {
            traci.getVehicleControl().setMaxAcceleration(vehicleId, actualVehicleType.getAccel());
        }
        if (!eq(actualVehicleType.getDecel(), baseVehicleType.getDecel())) {
            traci.getVehicleControl().setMaxDeceleration(vehicleId, actualVehicleType.getDecel());
        }
        if (!eq(actualVehicleType.getMinGap(), baseVehicleType.getMinGap())) {
            traci.getVehicleControl().setMinimumGap(vehicleId, actualVehicleType.getMinGap());
        }
        if (!eq(actualVehicleType.getLength(), baseVehicleType.getLength())) {
            traci.getVehicleControl().setVehicleLength(vehicleId, actualVehicleType.getLength());
        }
        if (!eq(actualVehicleType.getSpeedFactor(), baseVehicleType.getSpeedFactor())) {
            traci.getVehicleControl().setSpeedFactor(vehicleId, actualVehicleType.getSpeedFactor());
        }
    }

    private boolean eq(double a, double b) {
        return Math.abs(a - b) < 0.0001;
    }


    private String extractDepartureSpeed(VehicleRegistration interaction) {
        switch (interaction.getDeparture().getDepartSpeedMode()) {
            case PRECISE:
                return String.format(Locale.ENGLISH, "%.2f", interaction.getDeparture().getDepartSpeed());
            case RANDOM:
                return "random";
            case MAXIMUM:
            default:
                return "max";
        }
    }

    private String extractDepartureLane(VehicleRegistration interaction) {
        switch (interaction.getDeparture().getLaneSelectionMode()) {
            case RANDOM:
                return "random";
            case FREE:
                return "free";
            case ALLOWED:
                return "allowed";
            case BEST:
                return "best";
            case FIRST:
                return "first";
            case HIGHWAY:
                return isTruckOrTrailer(interaction.getMapping().getVehicleType().getVehicleClass())
                        ? "first"
                        : "best";
            default:
                int extractedLaneId = interaction.getDeparture().getDepartureLane();
                return extractedLaneId >= 0
                        ? Integer.toString(extractedLaneId)
                        : "best";
        }
    }

    private boolean isTruckOrTrailer(VehicleClass vehicleClass) {
        return SumoVehicleClassMapping.toSumo(vehicleClass).equals("truck")
                || SumoVehicleClassMapping.toSumo(vehicleClass).equals("trailer");
    }


    @Override
    public void finishSimulation() throws InternalFederateException {
        if (sumoConfig.writeVehicleDepartures) {
            // debug feature: write all vehicle departures to an additional route-file
            File logDir = new File(new File(descriptor.getHost().workingDirectory, descriptor.getId()), "log");
            if (logDir.exists() || logDir.mkdirs()) {
                File departureFile = new File(logDir, "departures.rou.xml");
                sumoRouteFileCreator.store(departureFile);
            } else {
                log.error("Could not create directory for departure file.");
            }
        }
        super.finishSimulation();
    }

    /**
     * Writes a new SUMO route file based on the registered vehicle types and routes.
     *
     * @param typesInit  Interaction contains predefined vehicle types.
     * @param routesInit Interaction contains paths and their IDs.
     */
    private void writeRouteFile(VehicleTypesInitialization typesInit, VehicleRoutesInitialization routesInit) {
        File outputFile = initRouteFileCreator();

        this.initialTypes.putAll(typesInit.getTypes());

        // stores the rou.xml file to the working directory. this file is required for SUMO to run
        sumoRouteFileCreator
                .addVehicleTypes(typesInit.getTypes())
                .addRoutes(routesInit.getRoutes())
                .store(outputFile);
    }

    private File initRouteFileCreator() {
        File dir = new File(descriptor.getHost().workingDirectory, descriptor.getId());
        String subDir = new File(sumoConfig.sumoConfigurationFile).getParent();
        if (StringUtils.isNotBlank(subDir)) {
            dir = new File(dir, subDir);
        }
        File tmpRouteFile = new File(dir, routeFilename);

        // keep single instance
        if (sumoRouteFileCreator == null) {
            this.sumoRouteFileCreator = new SumoRouteFileCreator(tmpRouteFile, sumoConfig.timeGapOffset);
        }
        return tmpRouteFile;
    }
}
