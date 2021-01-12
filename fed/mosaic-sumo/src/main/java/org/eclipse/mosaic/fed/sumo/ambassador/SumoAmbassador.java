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

import org.eclipse.mosaic.fed.sumo.util.SumoVehicleClassMapping;
import org.eclipse.mosaic.fed.sumo.util.SumoRouteFileCreator;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.interactions.traffic.VehicleRoutesInitialization;
import org.eclipse.mosaic.interactions.traffic.VehicleTypesInitialization;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.interactions.vehicle.VehicleRouteRegistration;
import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.math.MathUtils;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
     * Cached {@link VehicleRegistration}-interaction, which is filled if a vehicle could not be
     * emitted e.g. due a blocked lane.
     */
    private final List<VehicleRegistration> notYetAddedVehicles = new ArrayList<>();

    /**
     * Name of the additional route file being created, which contains all vTypes added through Mapping.
     */
    private final String vehicleTypeRouteFileName;

    /**
     * Instance of {@link SumoRouteFileCreator} used to write routes to a *.rou.xml file.
     */
    private SumoRouteFileCreator sumoRouteFileCreator;

    private final File sumoConfigurationFile;

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
        sumoConfigurationFile = absoluteConfiguration;

        vehicleTypeRouteFileName = extractVehicleTypeRouteFileName();

        if (StringUtils.isBlank(vehicleTypeRouteFileName)) {
            String myError = "The tag <route-files value=\"*.rou.xml\"/> is missing in " + sumoConfigurationFile.getAbsolutePath();
            log.error(myError);
            throw new RuntimeException(myError);
        }
    }

    /**
     * Find the name of the route file and extend it with {@code "_vTypes.rou.xml"}
     * for new vehicle types to be written in.
     *
     * @return The route-file name for new vehicle types.
     */
    private String extractVehicleTypeRouteFileName() {
        try {
            XMLConfiguration sumoConfiguration = XmlUtils.readXmlFromFile(sumoConfigurationFile);
            String routeFileName = XmlUtils.getValueFromXpath(sumoConfiguration, "/input/route-files/@value", null);
            Validate.notNull(routeFileName);
            String[] routeFiles = routeFileName.split("[\\s,]+"); // split by comma (+ white space)
            if (routeFiles.length > 1) {
                log.debug("It seems like there was more than one route-file defined.");
            }
            // get first route-file, extend it with "_vTypes.rou.xml"
            return Validate.notNull(routeFiles[0]).split("\\.")[0] + "_vTypes.rou.xml";
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
    protected void receiveInteraction(VehicleRegistration interaction) throws InternalFederateException {
        VehicleMapping vehicleMapping = interaction.getMapping();
        log.info("VehicleRegistration \"{}\" received at simulation time {} ns", vehicleMapping.getName(), interaction.getTime());
        notYetAddedVehicles.add(interaction);
    }

    /**
     * This processes a {@link VehicleRouteRegistration} that have been dynamically created.
     *
     * @param interaction Interaction containing information about an added route.
     */
    private void receiveInteraction(VehicleRouteRegistration interaction) throws InternalFederateException {
        VehicleRoute newRoute = interaction.getRoute();
        if (!routeCache.containsKey(newRoute.getId())) {
            routeCache.put(newRoute.getId(), newRoute);
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
            routeCache.put(route.getId(), route);
        }

        cachedVehicleRoutesInitialization = interaction;
        if (sumoReadyToStart()) {
            sumoStartupProcedure();
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
            sumoStartupProcedure();
        }
    }

    private boolean sumoReadyToStart() {
        return descriptor != null && cachedVehicleRoutesInitialization != null && cachedVehicleTypesInitialization != null;
    }

    protected void sumoStartupProcedure() throws InternalFederateException {
        writeTypesFromMapping(cachedVehicleTypesInitialization);
        startSumoLocal();
        initTraci();
        readInitialRoutesFromTraci();
        addInitialRoutesFromMapping();
    }

    /**
     * Read Routes priorly defined in Sumo route-file to later make them available to the rest of the
     * simulations using {@link VehicleRouteRegistration}.
     *
     * @throws InternalFederateException if Traci connection couldn't be established
     */
    private void readInitialRoutesFromTraci() throws InternalFederateException {
        for (String id : traci.getRouteControl().getRouteIds()) {
            if (!routeCache.containsKey(id)) {
                VehicleRoute route = readRouteFromTraci(id);
                routeCache.put(route.getId(), route);
            }
        }
    }

    /**
     * Passes on the routes from Mapping to SUMO.
     *
     * @throws InternalFederateException if there was a problem with traci
     */
    private void addInitialRoutesFromMapping() throws InternalFederateException {
        for (Map.Entry<String, VehicleRoute> routeEntry : cachedVehicleRoutesInitialization.getRoutes().entrySet()) {
            traci.getRouteControl().addRoute(routeEntry.getKey(), routeEntry.getValue().getEdgeIdList());
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

                    if (!routeCache.containsKey(routeId)) {
                        throw new IllegalArgumentException(
                                "Unknown route " + routeId + " for vehicle with departure time " + interaction.getTime()
                        );
                    }

                    traci.getSimulationControl().addVehicle(vehicleId, routeId, vehicleType, laneId, departPos, departSpeed);
                    if (sumoConfig.subscribeToAllVehicles || interaction.getMapping().hasApplication()) {
                        traci.getSimulationControl().subscribeForVehicle(vehicleId, interaction.getTime(), this.getEndTime());
                    }

                    applyChangesInVehicleTypeForVehicle(
                            vehicleId,
                            interaction.getMapping().getVehicleType(),
                            cachedVehicleTypesInitialization.getTypes().get(vehicleType)
                    );

                    if (sumoConfig.writeVehicleDepartures && sumoRouteFileCreator.departuresInitialized()) {
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
        double epsilon = 1e-4;
        if (!MathUtils.isFuzzyEqual(actualVehicleType.getTau(), baseVehicleType.getTau(), epsilon)) {
            double minReactionTime = sumoConfig.updateInterval / 1000d;
            traci.getVehicleControl().setReactionTime(
                    vehicleId, Math.max(minReactionTime, actualVehicleType.getTau() + sumoConfig.timeGapOffset)
            );
        }
        if (!MathUtils.isFuzzyEqual(actualVehicleType.getMaxSpeed(), baseVehicleType.getMaxSpeed(), epsilon)) {
            traci.getVehicleControl().setMaxSpeed(vehicleId, actualVehicleType.getMaxSpeed());
        }
        if (!MathUtils.isFuzzyEqual(actualVehicleType.getAccel(), baseVehicleType.getAccel(), epsilon)) {
            traci.getVehicleControl().setMaxAcceleration(vehicleId, actualVehicleType.getAccel());
        }
        if (!MathUtils.isFuzzyEqual(actualVehicleType.getDecel(), baseVehicleType.getDecel(), epsilon)) {
            traci.getVehicleControl().setMaxDeceleration(vehicleId, actualVehicleType.getDecel());
        }
        if (!MathUtils.isFuzzyEqual(actualVehicleType.getMinGap(), baseVehicleType.getMinGap(), epsilon)) {
            traci.getVehicleControl().setMinimumGap(vehicleId, actualVehicleType.getMinGap());
        }
        if (!MathUtils.isFuzzyEqual(actualVehicleType.getLength(), baseVehicleType.getLength(), epsilon)) {
            traci.getVehicleControl().setVehicleLength(vehicleId, actualVehicleType.getLength());
        }
        if (!MathUtils.isFuzzyEqual(actualVehicleType.getSpeedFactor(), baseVehicleType.getSpeedFactor(), epsilon)) {
            traci.getVehicleControl().setSpeedFactor(vehicleId, actualVehicleType.getSpeedFactor());
        }
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
        if (sumoConfig.writeVehicleDepartures && sumoRouteFileCreator.departuresInitialized()) {
            // debug feature: write all vehicle departures to an additional route-file
            File logDir = new File(new File(descriptor.getHost().workingDirectory, descriptor.getId()), "log");
            if (logDir.exists() || logDir.mkdirs()) {
                File departureFile = new File(logDir, "departures.rou.xml");
                sumoRouteFileCreator.storeDepartures(departureFile);
            } else {
                log.error("Could not create directory for departure file.");
            }
        }
        super.finishSimulation();
    }

    /**
     * Writes a new SUMO route file based on the registered vehicle types.
     *
     * @param typesInit Interaction contains predefined vehicle types.
     */
    private void writeTypesFromMapping(VehicleTypesInitialization typesInit) {
        File outputFile = initVehicleTypeRouteFileCreator();

        // stores the rou.xml file to the working directory. this file is required for SUMO to run
        sumoRouteFileCreator
                .addVehicleTypes(typesInit.getTypes())
                .store(outputFile);
    }

    private File initVehicleTypeRouteFileCreator() {
        File dir = new File(descriptor.getHost().workingDirectory, descriptor.getId());
        String subDir = new File(sumoConfig.sumoConfigurationFile).getParent();
        if (StringUtils.isNotBlank(subDir)) {
            dir = new File(dir, subDir);
        }
        File tmpRouteFile = new File(dir, vehicleTypeRouteFileName);

        // keep single instance
        if (sumoRouteFileCreator == null) {
            this.sumoRouteFileCreator = new SumoRouteFileCreator(
                    sumoConfigurationFile, tmpRouteFile, sumoConfig.additionalVTypeParameters, sumoConfig.timeGapOffset
            );
            if (sumoConfig.writeVehicleDepartures) {
                sumoRouteFileCreator.initializeDepartureDocument();
            }
        }
        return tmpRouteFile;
    }

}
