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
import org.eclipse.mosaic.fed.sumo.util.SumoVehicleTypesWriter;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.interactions.mapping.advanced.ScenarioVehicleRegistration;
import org.eclipse.mosaic.interactions.traffic.VehicleRoutesInitialization;
import org.eclipse.mosaic.interactions.traffic.VehicleTypesInitialization;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.interactions.vehicle.VehicleRouteRegistration;
import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.math.MathUtils;
import org.eclipse.mosaic.lib.objects.mapping.VehicleMapping;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleType;
import org.eclipse.mosaic.rti.api.FederateAmbassador;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.Interaction;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.parameters.AmbassadorParameter;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
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
     * Cached {@link VehicleRegistration}-interactions, which will clear vehicles if they can be added and try again
     * next time step if it couldn't be emitted.
     */
    private final List<VehicleRegistration> notYetAddedVehicles = new ArrayList<>();

    /**
     * Cached {@link VehicleRegistration}-interactions, for vehicles, that haven't been subscribed to yet.
     */
    private final List<VehicleRegistration> notYetSubscribedVehicles = new ArrayList<>();

    /**
     * Set containing all vehicles, that have been added using the SUMO route file.
     */
    private final Set<String> vehiclesAddedViaRouteFile = new HashSet<>();

    /**
     * Set containing all vehicles, that have been added from the RTI e.g. using the Mapping file.
     */
    private final Set<String> vehiclesAddedViaRti = new HashSet<>();

    /**
     * Constructor for {@link SumoAmbassador}.
     *
     * @param ambassadorParameter parameters for the ambassador containing its' id and configuration
     */
    public SumoAmbassador(AmbassadorParameter ambassadorParameter) {
        super(ambassadorParameter);
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
     * @param vehicleRegistration {@link VehicleRegistration} containing the vehicle definition.
     */
    private void receiveInteraction(VehicleRegistration vehicleRegistration) {
        VehicleMapping vehicleMapping = vehicleRegistration.getMapping();
        String vehicleId = vehicleMapping.getName();
        String logMessage;
        boolean isVehicleAddedViaRti = !vehiclesAddedViaRouteFile.contains(vehicleMapping.getName());
        if (isVehicleAddedViaRti) {
            vehiclesAddedViaRti.add(vehicleMapping.getName());
            notYetAddedVehicles.add(vehicleRegistration);
            logMessage = "VehicleRegistration from RTI \"{}\" received at simulation time {} ns (subscribe={})";
        } else { // still subscribe to vehicles with apps
            logMessage = "VehicleRegistration for SUMO vehicle \"{}\" received at simulation time {} ns (subscribe={})";
        }

        boolean subscribeToVehicle = sumoConfig.subscribeToAllVehicles || vehicleMapping.hasApplication();
        log.info(logMessage, vehicleId, vehicleRegistration.getTime(), subscribeToVehicle);

        // now prepare vehicles to subscribe to
        if (subscribeToVehicle) {
            notYetSubscribedVehicles.add(vehicleRegistration);
        }
    }

    /**
     * This processes a {@link VehicleRouteRegistration} that have been dynamically created.
     *
     * @param vehicleRouteRegistration Interaction containing information about an added route.
     */
    private void receiveInteraction(VehicleRouteRegistration vehicleRouteRegistration) throws InternalFederateException {
        VehicleRoute newRoute = vehicleRouteRegistration.getRoute();
        propagateRouteIfAbsent(newRoute.getId(), newRoute);
    }

    /**
     * Extract data from the {@link VehicleRoutesInitialization} to SUMO.
     *
     * @param vehicleRoutesInitialization interaction containing vehicle departures and pre calculated routes for change route requests.
     * @throws InternalFederateException if something goes wrong in startSumoLocal(), initTraci(), completeRoutes() or readRouteFromTraci()
     */
    private void receiveInteraction(VehicleRoutesInitialization vehicleRoutesInitialization) throws InternalFederateException {
        log.debug("Received VehicleRoutesInitialization: {}", vehicleRoutesInitialization.getTime());

        cachedVehicleRoutesInitialization = vehicleRoutesInitialization;
        if (sumoReadyToStart()) {
            sumoStartupProcedure();
        }
    }

    /**
     * Extract data from the {@link VehicleTypesInitialization} and forward to SUMO.
     *
     * @param vehicleTypesInitialization interaction containing vehicle types
     * @throws InternalFederateException if something goes wrong in startSumoLocal(), initTraci() or completeRoutes()
     */
    private void receiveInteraction(VehicleTypesInitialization vehicleTypesInitialization) throws InternalFederateException {
        log.debug("Received VehicleTypesInitialization");

        cachedVehicleTypesInitialization = vehicleTypesInitialization;
        if (sumoReadyToStart()) {
            sumoStartupProcedure();
        }
    }

    private boolean sumoReadyToStart() {
        return descriptor != null && cachedVehicleRoutesInitialization != null && cachedVehicleTypesInitialization != null;
    }

    private void sumoStartupProcedure() throws InternalFederateException {
        writeTypesFromRti(cachedVehicleTypesInitialization);
        startSumoLocal();
        initSumoConnection();
        readInitialRoutesFromTraci();
        addInitialRoutes();
    }

    /**
     * Read Routes priorly defined in Sumo route-file to later make them available to the rest of the
     * simulations using {@link VehicleRouteRegistration}.
     *
     * @throws InternalFederateException if Traci connection couldn't be established
     */
    private void readInitialRoutesFromTraci() throws InternalFederateException {
        for (String id : bridge.getRouteControl().getRouteIds()) {
            if (!routeCache.containsKey(id)) {
                VehicleRoute route = readRouteFromTraci(id);
                routeCache.put(route.getId(), route);
            }
        }
    }

    /**
     * Passes on initial routes to SUMO.
     *
     * @throws InternalFederateException if there was a problem with traci
     */
    private void addInitialRoutes() throws InternalFederateException {
        for (Map.Entry<String, VehicleRoute> routeEntry : cachedVehicleRoutesInitialization.getRoutes().entrySet()) {
            propagateRouteIfAbsent(routeEntry.getKey(), routeEntry.getValue());
        }
    }

    /**
     * Vehicles of the {@link #notYetSubscribedVehicles} list will be added to simulation by this function
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
        // first check if there were new vehicles added via SUMO
        propagateSumoVehiclesToRti();
        // now add all vehicles, that were received from RTI
        addNotYetAddedVehicles(time);
        // now subscribe to all relevant vehicles
        subscribeToNotYetSubscribedVehicles(time);

    }

    private void propagateSumoVehiclesToRti() throws InternalFederateException {
        final List<String> departedVehicles = bridge.getSimulationControl().getDepartedVehicles();
        String vehicleTypeId;
        for (String vehicleId : departedVehicles) {
            if (vehiclesAddedViaRti.contains(vehicleId)) { // only handle route file vehicles here
                continue;
            }
            vehiclesAddedViaRouteFile.add(vehicleId);
            vehicleTypeId = bridge.getVehicleControl().getVehicleTypeId(vehicleId);
            try {
                rti.triggerInteraction(new ScenarioVehicleRegistration(this.nextTimeStep, vehicleId, vehicleTypeId));
            } catch (IllegalValueException e) {
                throw new InternalFederateException(e);
            }
            if (sumoConfig.subscribeToAllVehicles) {
                bridge.getSimulationControl().subscribeForVehicle(vehicleId, this.nextTimeStep, this.getEndTime());
            }
        }
    }

    private void addNotYetAddedVehicles(long time) throws InternalFederateException {
        for (Iterator<VehicleRegistration> iterator = notYetAddedVehicles.iterator(); iterator.hasNext(); ) {
            VehicleRegistration vehicleRegistration = iterator.next();

            String vehicleId = vehicleRegistration.getMapping().getName();
            String vehicleType = vehicleRegistration.getMapping().getVehicleType().getName();
            String routeId = vehicleRegistration.getDeparture().getRouteId();
            String departPos = String.format(Locale.ENGLISH, "%.2f", vehicleRegistration.getDeparture().getDeparturePos());
            int departIndex = vehicleRegistration.getDeparture().getDepartureConnectionIndex();
            String departSpeed = extractDepartureSpeed(vehicleRegistration);
            String laneId = extractDepartureLane(vehicleRegistration);

            try {
                if (vehicleRegistration.getTime() <= time) {
                    log.info("Adding new vehicle \"{}\" at simulation time {} ns (type={}, routeId={}, laneId={}, departPos={})",
                            vehicleId, vehicleRegistration.getTime(), vehicleType, routeId, laneId, departPos);

                    if (!routeCache.containsKey(routeId)) {
                        throw new IllegalArgumentException(
                                "Unknown route " + routeId + " for vehicle with departure time " + vehicleRegistration.getTime()
                        );
                    }

                    if (departIndex > 0) {
                        routeId = cutAndAddRoute(routeId, departIndex);
                    }

                    bridge.getSimulationControl().addVehicle(vehicleId, routeId, vehicleType, laneId, departPos, departSpeed);

                    applyChangesInVehicleTypeForVehicle(
                            vehicleId,
                            vehicleRegistration.getMapping().getVehicleType(),
                            cachedVehicleTypesInitialization.getTypes().get(vehicleType)
                    );
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

    private String cutAndAddRoute(String routeId, int departIndex) throws InternalFederateException {
        String newRouteId = routeId + "_cut" + departIndex;
        if (routeCache.containsKey(newRouteId)) {
            return newRouteId;
        }
        final VehicleRoute route = routeCache.get(routeId);
        final List<String> connections = route.getConnectionIds();
        if (departIndex >= connections.size()) {
            throw new IllegalArgumentException("The departIndex=" + departIndex + " is too large for route with id=" + routeId);
        }
        final VehicleRoute cutRoute = new VehicleRoute(newRouteId, connections.subList(departIndex, connections.size()), route.getNodeIds(), route.getLength());
        propagateRouteIfAbsent(newRouteId, cutRoute);
        return newRouteId;
    }

    private void subscribeToNotYetSubscribedVehicles(long time) throws InternalFederateException {
        for (Iterator<VehicleRegistration> iterator = notYetSubscribedVehicles.iterator(); iterator.hasNext(); ) {
            VehicleRegistration currentVehicleRegistration = iterator.next();
            String vehicleId = currentVehicleRegistration.getMapping().getName();
            try {
                // always subscribe to vehicles, that are came from SUMO and are in notYetSubscribedVehicles-list
                if (vehiclesAddedViaRouteFile.contains(vehicleId) || currentVehicleRegistration.getTime() <= time) {
                    bridge.getSimulationControl().subscribeForVehicle(vehicleId, currentVehicleRegistration.getTime(), this.getEndTime());
                    iterator.remove();
                }
            } catch (InternalFederateException e) {
                log.warn("Couldn't subscribe to vehicle {}.", vehicleId);
                if (sumoConfig.exitOnInsertionError) {
                    throw e;
                }
                iterator.remove();
            }
        }

    }

    private void applyChangesInVehicleTypeForVehicle(String vehicleId, VehicleType actualVehicleType, VehicleType baseVehicleType) throws InternalFederateException {
        if (!MathUtils.isFuzzyEqual(actualVehicleType.getTau(), baseVehicleType.getTau())) {
            double minReactionTime = sumoConfig.updateInterval / 1000d;
            bridge.getVehicleControl().setReactionTime(
                    vehicleId, Math.max(minReactionTime, actualVehicleType.getTau() + sumoConfig.timeGapOffset)
            );
        }
        if (!MathUtils.isFuzzyEqual(actualVehicleType.getMaxSpeed(), baseVehicleType.getMaxSpeed())) {
            bridge.getVehicleControl().setMaxSpeed(vehicleId, actualVehicleType.getMaxSpeed());
        }
        if (!MathUtils.isFuzzyEqual(actualVehicleType.getAccel(), baseVehicleType.getAccel())) {
            bridge.getVehicleControl().setMaxAcceleration(vehicleId, actualVehicleType.getAccel());
        }
        if (!MathUtils.isFuzzyEqual(actualVehicleType.getDecel(), baseVehicleType.getDecel())) {
            bridge.getVehicleControl().setMaxDeceleration(vehicleId, actualVehicleType.getDecel());
        }
        if (!MathUtils.isFuzzyEqual(actualVehicleType.getMinGap(), baseVehicleType.getMinGap())) {
            bridge.getVehicleControl().setMinimumGap(vehicleId, actualVehicleType.getMinGap());
        }
        if (!MathUtils.isFuzzyEqual(actualVehicleType.getLength(), baseVehicleType.getLength())) {
            bridge.getVehicleControl().setVehicleLength(vehicleId, actualVehicleType.getLength());
        }
        if (!MathUtils.isFuzzyEqual(actualVehicleType.getSpeedFactor(), baseVehicleType.getSpeedFactor())) {
            bridge.getVehicleControl().setSpeedFactor(vehicleId, actualVehicleType.getSpeedFactor());
        }
    }

    private String extractDepartureSpeed(VehicleRegistration vehicleRegistration) {
        switch (vehicleRegistration.getDeparture().getDepartureSpeedMode()) {
            case PRECISE:
                return String.format(Locale.ENGLISH, "%.2f", vehicleRegistration.getDeparture().getDepartureSpeed());
            case RANDOM:
                return "random";
            case MAXIMUM:
            default:
                return "max";
        }
    }

    private String extractDepartureLane(VehicleRegistration vehicleRegistration) {
        switch (vehicleRegistration.getDeparture().getLaneSelectionMode()) {
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
                return isTruckOrTrailer(vehicleRegistration.getMapping().getVehicleType().getVehicleClass())
                        ? "first"
                        : "best";
            default:
                int extractedLaneId = vehicleRegistration.getDeparture().getDepartureLane();
                return extractedLaneId >= 0
                        ? Integer.toString(extractedLaneId)
                        : "best";
        }
    }

    private boolean isTruckOrTrailer(VehicleClass vehicleClass) {
        return SumoVehicleClassMapping.toSumo(vehicleClass).equals("truck")
                || SumoVehicleClassMapping.toSumo(vehicleClass).equals("trailer");
    }

    private void propagateRouteIfAbsent(String routeId, VehicleRoute route) throws InternalFederateException {
        // if the route is already known (because it is defined in a route-file) don't add route
        if (routeCache.containsKey(routeId)) {
            log.warn("Could not add route \"{}\", because it is already known to SUMO.", routeId);
        } else {
            routeCache.put(routeId, route);
            bridge.getRouteControl().addRoute(routeId, route.getConnectionIds());
        }
    }

    /**
     * Writes a new SUMO additional-file based on the registered vehicle types.
     *
     * @param typesInit Interaction contains predefined vehicle types.
     */
    private void writeTypesFromRti(VehicleTypesInitialization typesInit) {
        File dir = new File(descriptor.getHost().workingDirectory, descriptor.getId());
        String subDir = new File(sumoConfig.sumoConfigurationFile).getParent();
        if (StringUtils.isNotBlank(subDir)) {
            dir = new File(dir, subDir);
        }
        SumoVehicleTypesWriter sumoVehicleTypesWriter = new SumoVehicleTypesWriter(dir, sumoConfig);
        // stores the *.add.xml file to the working directory. this file is required for SUMO to run
        sumoVehicleTypesWriter
                .addVehicleTypes(typesInit.getTypes())
                .store();
    }
}
