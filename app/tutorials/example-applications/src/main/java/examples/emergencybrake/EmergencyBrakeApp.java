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

package examples.emergencybrake;

import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.lib.enums.SensorType;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.etsi.Denm;
import org.eclipse.mosaic.lib.objects.v2x.etsi.DenmContent;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;

import java.io.File;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class implements an application for vehicles.
 * In case the vehicle sensors detect an obstacle the vehicle will perform an emergency brake.
 * If the emergency brake endures a specified minimum time duration a DENMessage is sent out.
 */
public class EmergencyBrakeApp extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication,
        CommunicationApplication {

    CEmergencyBrakeApp configuration;

    // Keep track of the vehicle movement for the emergency brake detection
    private float lastSpeed = 0;
    private float lastTime = 0;
    private long startedBrakingAt = Long.MIN_VALUE;

    // Keep status of the emergency brake performed on obstacle
    private boolean emergencyBrake = false;
    private long stoppedAt = Long.MIN_VALUE;

    @Override
    public void onStartup() {
        initConfig();
        getOs().getAdHocModule().enable();
    }

    /**
     * Sets all relevant variables from the configuration file.
     * Note: If the file is corrupted or contains invalid values, default values are assigned.
     */
    public void initConfig() {
        try {
            configuration = new ObjectInstantiation<>(CEmergencyBrakeApp.class)
                    .readFile(new File("applications/vehicle/emergency_brake_config.json"));
        } catch (InstantiationException e) {
            getLog().error("Exception: ", e);
        }
        getLog().info("Initializing brake application");
    }

    /**
     * Evaluates the vehicle's speed and acceleration data to decide whether an emergency break is happening.
     * In case of an emergency break a DENMessage is sent out.
     */
    private void detectEmergencyBrake() {

        float curSpeed = (float) getOs().getNavigationModule().getVehicleData().getSpeed();

        // Calculate brake deceleration
        float timeDifference = ((getOs().getSimulationTime() - lastTime) / TIME.SECOND);
        float speedDifference = curSpeed - lastSpeed;
        float curDeceleration = (speedDifference / timeDifference) / 9.81f;

        // If brake deceleration is low enough to qualify for an emergency brake, then keep track of the brake duration
        if (curDeceleration < -(configuration.emergencyBrakeThresh)) {
            if (startedBrakingAt < 0) {
                startedBrakingAt = getOs().getSimulationTime();
            }
        } else {
            startedBrakingAt = Long.MIN_VALUE;
        }

        long actualBrakeDuration = Long.MIN_VALUE;
        if (startedBrakingAt > 0) {
            actualBrakeDuration = getOs().getSimulationTime() - startedBrakingAt;
        }

        // If brake duration is high enough, then send a DENMessage
        if (actualBrakeDuration >= configuration.minimalBrakeDuration) {
            getLog().info("Detected emergency brake in progress");

            // Vehicle info
            GeoPoint vehicleLongLat = getOs().getPosition();
            String roadId = getOs().getNavigationModule().getRoadPosition().getConnection().getId();

            // Prepare the DENMessage
            MessageRouting routing =
                    getOs().getAdHocModule().createMessageRouting().viaChannel(AdHocChannel.CCH).topoBroadCast();
            Denm denm = new Denm(routing, new DenmContent(getOs().getSimulationTime(), vehicleLongLat, roadId,
                    SensorType.SPEED, 1, curSpeed, curDeceleration * 9.81f, null,
                    null, null));
            getLog().debug("Sender position: " + denm.getSenderPosition());
            getLog().debug("Sender speed at event time: " + denm.getCausedSpeed() + "m/s");
            getLog().debug("Sender deceleration at event time: " + denm.getSenderDeceleration() + "m/s2");
            getLog().debug("RoadId on which the event take place: " + denm.getEventRoadId());
            getLog().debug("DENMessage successfully filled");

            // Send the DENMessage
            getOs().getAdHocModule().sendV2xMessage(denm);
            getLog().info("Sent DENMessage");
        }

        // Update time and speed buffer for calculation of deceleration
        lastTime = getOs().getSimulationTime();
        lastSpeed = curSpeed;

        getLog().debug("t=" + getOs().getSimulationTime()
                + "\t SpeedDifference: " + speedDifference
                + "\t timeDifference: " + timeDifference
                + "\t curDeceleration: " + curDeceleration);
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public void onMessageReceived(ReceivedV2xMessage receivedV2xMessage) {

    }

    @Override
    public void onAcknowledgementReceived(ReceivedAcknowledgement acknowledgement) {

    }

    @Override
    public void onCamBuilding(CamBuilder camBuilder) {

    }

    @Override
    public void onMessageTransmitted(V2xMessageTransmission v2xMessageTransmission) {

    }

    @Override
    public void processEvent(Event event) throws Exception {

    }

    /**
     * Checks for an obstacle detected by the vehicle sensors.
     * If an obstacle is detected an emergency brake will be performed.
     *
     * @param previousVehicleData the previous state of the vehicle
     * @param updatedVehicleData  the updated state of the vehicle
     */
    @Override
    public void onVehicleUpdated(@Nullable VehicleData previousVehicleData, @Nonnull VehicleData updatedVehicleData) {

        boolean obstacleDetected = getOs().getStateOfEnvironmentSensor(SensorType.OBSTACLE) > 0;

        // Initiate emergency brake if obstacle is detected
        if (obstacleDetected && !emergencyBrake) {
            stoppedAt = getOs().getSimulationTime();
            getOs().changeSpeedWithForcedAcceleration(configuration.targetSpeed, configuration.deceleration);
            emergencyBrake = true;
            getLog().infoSimTime(this, "Performing emergency brake caused by detected obstacle");
        }

        // Continue driving normal as soon emergency brake is done and no obstacle is detectable
        if (emergencyBrake && !obstacleDetected && idlePeriodOver(stoppedAt) && reachedSpeed(configuration.targetSpeed)) {
            getOs().resetSpeed();
            stoppedAt = Long.MIN_VALUE;
            emergencyBrake = false;
            getLog().infoSimTime(this, "Passed obstacle");
        }

        // Call emergency brake detection
        detectEmergencyBrake();

    }

    private boolean idlePeriodOver(long stoppedAt) {
        return getOs().getSimulationTime() > stoppedAt + configuration.idlePeriod;
    }

    private boolean reachedSpeed(double speed) {
        return Math.abs(getOs().getVehicleData().getSpeed() - speed) < 0.1d;
    }
}
