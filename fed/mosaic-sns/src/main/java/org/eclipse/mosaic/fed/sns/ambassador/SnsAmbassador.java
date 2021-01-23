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

package org.eclipse.mosaic.fed.sns.ambassador;

import org.eclipse.mosaic.fed.sns.config.CSns;
import org.eclipse.mosaic.interactions.communication.AdHocCommunicationConfiguration;
import org.eclipse.mosaic.interactions.communication.V2xMessageReception;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.interactions.mapping.ChargingStationRegistration;
import org.eclipse.mosaic.interactions.mapping.RsuRegistration;
import org.eclipse.mosaic.interactions.mapping.TrafficLightRegistration;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.lib.enums.DestinationType;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.model.delay.GammaSpeedDelay;
import org.eclipse.mosaic.lib.model.transmission.TransmissionResult;
import org.eclipse.mosaic.lib.objects.communication.AdHocConfiguration;
import org.eclipse.mosaic.lib.objects.mapping.ChargingStationMapping;
import org.eclipse.mosaic.lib.objects.mapping.RsuMapping;
import org.eclipse.mosaic.lib.objects.mapping.TrafficLightMapping;
import org.eclipse.mosaic.lib.objects.mapping.VehicleMapping;
import org.eclipse.mosaic.lib.objects.v2x.V2xReceiverInformation;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.AbstractFederateAmbassador;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.Interaction;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.parameters.AmbassadorParameter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the Simple Network Simulator (SNS).
 * SNS is a fast alternative to other network simulators such as ns-3 or OMNeT++,
 * it models different transmission options for ad-hoc communication (ETSI ITS-G5)
 * including single-hop broadcast as well as multi-hop / geo-routing protocols.
 */
public class SnsAmbassador extends AbstractFederateAmbassador {

    /**
     * The transmissionSimulator is the component that performs the actual simple network simulation.
     */
    private TransmissionSimulator transmissionSimulator;

    /**
     * Application supported vehicles (from VehicleRegistration) to be added with vehicle movements.
     */
    final private HashMap<String, Double> registeredVehicles = new HashMap<>();

    /**
     * Value for the radius of a single hop extracted from the configuration in the initialization.
     */
    private double singlehopRadius;

    /**
     * Construct the Ambassador.
     *
     * @param ambassadorParameter parameter.
     */
    public SnsAmbassador(AmbassadorParameter ambassadorParameter) {
        super(ambassadorParameter);
        log.info("Start Simulation");
    }

    @Override
    public void initialize(final long startTime, final long endTime) throws InternalFederateException {
        super.initialize(startTime, endTime);
        if (log.isTraceEnabled()) {
            log.trace("subscribedMessages: {}", Arrays.toString(this.rti.getSubscribedInteractions().toArray()));
        }

        try {
            CSns configuration = new ObjectInstantiation<>(CSns.class).readFile(ambassadorParameter.configuration);
            if (configuration.singlehopDelay instanceof GammaSpeedDelay) {
                log.info("Detected GammaSpeedDelay for. The SNS is currently ignoring speed of entities in its evaluation of delay values");
            }
            this.singlehopRadius = configuration.singlehopRadius;
            final RandomNumberGenerator rng = rti.createRandomNumberGenerator();
            transmissionSimulator = new TransmissionSimulator(rng, configuration);
        } catch (InstantiationException e) {
            log.error("Could not read configuration. Reason: {}", e.getMessage());
        }

        log.info("Initialized SNS");
    }

    @Override
    protected void processInteraction(Interaction interaction) throws InternalFederateException {
        try {
            if (interaction.getTypeId().startsWith(RsuRegistration.TYPE_ID)) {
                this.process((RsuRegistration) interaction);
            } else if (interaction.getTypeId().startsWith(ChargingStationRegistration.TYPE_ID)) {
                this.process((ChargingStationRegistration) interaction);
            } else if (interaction.getTypeId().startsWith(TrafficLightRegistration.TYPE_ID)) {
                this.process((TrafficLightRegistration) interaction);
            } else if (interaction.getTypeId().startsWith(VehicleRegistration.TYPE_ID)) {
                this.process((VehicleRegistration) interaction);
            } else if (interaction.getTypeId().startsWith(VehicleUpdates.TYPE_ID)) {
                this.process((VehicleUpdates) interaction);
            } else if (interaction.getTypeId().startsWith(AdHocCommunicationConfiguration.TYPE_ID)) {
                this.process((AdHocCommunicationConfiguration) interaction);
            } else if (interaction.getTypeId().equals(V2xMessageTransmission.TYPE_ID)) {
                this.process((V2xMessageTransmission) interaction);
            } else {
                log.warn("Received unknown interaction={} @time={}", interaction.getTypeId(), TIME.format(interaction.getTime()));
            }
        } catch (Exception e) {
            throw new InternalFederateException(e);
        }
    }

    private void process(RsuRegistration interaction) {
        final RsuMapping applicationRsu = interaction.getMapping();
        if (applicationRsu.hasApplication()) {
            SimulationEntities.INSTANCE.createOrUpdateOfflineNode(applicationRsu.getName(), applicationRsu.getPosition().toCartesian());
            log.info("Added RSU id={} @time={}", applicationRsu.getName(), TIME.format(interaction.getTime()));
        }
    }

    private void process(ChargingStationRegistration interaction) {
        final ChargingStationMapping applicationCs = interaction.getMapping();
        if (applicationCs.hasApplication()) {
            SimulationEntities.INSTANCE.createOrUpdateOfflineNode(applicationCs.getName(), applicationCs.getPosition().toCartesian());
            log.info("Added ChargingStation id={} @time={}", applicationCs.getName(), TIME.format(interaction.getTime()));
        }
    }

    private void process(TrafficLightRegistration interaction) {
        final TrafficLightMapping applicationTl = interaction.getMapping();
        if (applicationTl.hasApplication()) {
            SimulationEntities.INSTANCE.createOrUpdateOfflineNode(applicationTl.getName(), applicationTl.getPosition().toCartesian());
            log.info("Added TrafficLight id={} @time={}", applicationTl.getName(), TIME.format(interaction.getTime()));
        }
    }

    private void process(VehicleRegistration interaction) {
        final VehicleMapping applicationVeh = interaction.getMapping();
        if (applicationVeh.hasApplication()) {
            registeredVehicles.put(applicationVeh.getName(), null);
            log.debug(
                    "Registered Vehicle (with application) for later adding id={} @time={}",
                    applicationVeh.getName(),
                    TIME.format(interaction.getTime())
            );
        }
    }

    private void process(VehicleUpdates interaction) {
        for (VehicleData added : interaction.getAdded()) {
            if (addOrUpdateVehicle(added)) {
                log.info("Added Vehicle id={} @time={}", added.getName(), TIME.format(interaction.getTime()));
            }
        }
        for (VehicleData updated : interaction.getUpdated()) {
            if (addOrUpdateVehicle(updated) && log.isTraceEnabled()) {
                log.trace("Moved Vehicle id={} to position={} @time={}",
                        updated.getName(), updated.getPosition(), TIME.format(interaction.getTime()));
            }
        }
        for (final String removedName : interaction.getRemovedNames()) {
            removeVehicle(removedName);
            log.info("Removed Vehicle id={} @time={}", removedName, TIME.format(interaction.getTime()));
        }
    }

    private void process(AdHocCommunicationConfiguration interaction) {
        AdHocConfiguration adHocConfiguration = interaction.getConfiguration();
        String nodeId = adHocConfiguration.getNodeId();
        // Switch the communication modules for simulated nodes on or off
        // (SNS only supports configurations with one radio).
        switch (adHocConfiguration.getRadioMode()) {
            case OFF:
                if (SimulationEntities.INSTANCE.isNodeSimulated(nodeId)) {
                    SimulationEntities.INSTANCE.disableWifi(nodeId);
                } else {
                    if (log.isDebugEnabled()) {
                        // for really rare cases
                        if (registeredVehicles.get(nodeId) != null) {
                            log.debug("Disabled Wifi of vehicle, which was enabled before, but not yet moved (just to let you know)");
                        }
                    }
                    registeredVehicles.put(nodeId, null);
                }
                break;
            case SINGLE:
                double communicationRadius;

                communicationRadius = singlehopRadius;
                if (adHocConfiguration.getConf0() != null) {
                    if (adHocConfiguration.getConf0().getRadius() != null) {
                        communicationRadius = adHocConfiguration.getConf0().getRadius();
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug(
                                    "Node {} is configured with a power value. The SNS supposed to handle configurations using radii.",
                                    adHocConfiguration.getNodeId());
                        }
                    }
                }
                if (SimulationEntities.INSTANCE.isNodeSimulated(nodeId)) {
                    SimulationEntities.INSTANCE.enableWifi(nodeId, communicationRadius);
                } else {
                    registeredVehicles.put(nodeId, communicationRadius);
                }
                break;
            case DUAL:
            default:
                log.warn("Tried to configure unsupported AdHocConfiguration (SNS only supports single radio): {}",
                        adHocConfiguration.getRadioMode());
        }
    }

    private void process(V2xMessageTransmission interaction) throws InternalFederateException {
        DestinationType type = interaction.getMessage().getRouting().getDestination().getType();
        if (type != DestinationType.AD_HOC_GEOCAST && type != DestinationType.AD_HOC_TOPOCAST) {
            return;
        }
        // Calculate transmission
        Map<String, TransmissionResult> transmissionResults = transmissionSimulator.preProcessInteraction(interaction);
        // send transmission results to rti
        prepareV2xMessageReceptions(transmissionResults, interaction);
    }

    /**
     * Adds or Updates vehicles positions (which is the same from the view of SNS - mainly new positions).
     *
     * @param vehicleData {@link VehicleData}
     * @return {@code true} if vehicle is online and able to send/receive messages {@code false}
     */
    private boolean addOrUpdateVehicle(VehicleData vehicleData) {
        final String vehicleName = vehicleData.getName();

        // During simulation, the regular case: just move vehicles
        if (SimulationEntities.INSTANCE.isNodeOnline(vehicleName)) {
            SimulationEntities.INSTANCE.updateOnlineNode(vehicleName, vehicleData.getProjectedPosition());
            return true;
        }

        if (SimulationEntities.INSTANCE.isNodeOffline(vehicleName)) {
            SimulationEntities.INSTANCE.createOrUpdateOfflineNode(vehicleName, vehicleData.getProjectedPosition());
            return false;
        }

        // In case the AdHocConfiguration arrived earlier than the first VehicleUpdates: create online vehicles
        Double communicationRadius = registeredVehicles.get(vehicleName);
        if (communicationRadius != null) {
            SimulationEntities.INSTANCE.createOnlineNode(vehicleName, vehicleData.getProjectedPosition(), communicationRadius);
            return true;
        }

        return false;
    }

    /**
     * Removes a vehicle from being handled in transmissions.
     *
     * @param vehicleId the id of the vehicle to be removed
     */
    private void removeVehicle(String vehicleId) {
        registeredVehicles.remove(vehicleId);
        SimulationEntities.INSTANCE.removeNode(vehicleId);
    }

    /**
     * Create the {@link V2xMessageReception}s to be sent to the RTI, the Application Simulator and accordingly
     * applications of possible receivers.
     *
     * @param transmissionResults    Set of successful receivers
     * @param v2xMessageTransmission Originally sent message (for messageId and sending time)
     */
    private void prepareV2xMessageReceptions(Map<String, TransmissionResult> transmissionResults,
                                             V2xMessageTransmission v2xMessageTransmission) throws InternalFederateException {
        if (transmissionResults == null) {
            return;
        }
        for (Map.Entry<String, TransmissionResult> transmissionResultEntry : transmissionResults.entrySet()) {
            if (transmissionResultEntry.getValue().success) {
                long receiveTime = v2xMessageTransmission.getTime() + transmissionResultEntry.getValue().delay;
                if (log.isDebugEnabled()) {
                    log.debug("Receive v2xMessage.id={} on node={} @time={}",
                            v2xMessageTransmission.getMessageId(), transmissionResultEntry.getKey(), TIME.format(receiveTime)
                    );
                }

                final V2xMessageReception v2xMessageReception = new V2xMessageReception(
                        receiveTime,
                        transmissionResultEntry.getKey(),
                        v2xMessageTransmission.getMessageId(),
                        new V2xReceiverInformation(receiveTime).sendTime(v2xMessageTransmission.getTime())
                );
                try {
                    rti.triggerInteraction(v2xMessageReception);
                } catch (IllegalValueException | InternalFederateException e) {
                    throw new InternalFederateException(e);
                }

            }
        }

    }

    @Override
    public boolean isTimeConstrained() {
        return true;
    }

    @Override
    public boolean isTimeRegulating() {
        return true;
    }
}
