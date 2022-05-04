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

package org.eclipse.mosaic.fed.application.app.etsi;

import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.AdHocModuleConfiguration;
import org.eclipse.mosaic.fed.application.app.ConfigurableApplication;
import org.eclipse.mosaic.fed.application.app.api.os.OperatingSystem;
import org.eclipse.mosaic.fed.application.config.CEtsi;
import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.DATA;

/**
 * Abstract application implementing the ETSI standard.
 * ETSI TS 102 637-2 V1.2.1 (2011-03).
 *
 * @see VehicleCamSendingApp
 * @see ChargingStationCamSendingApp
 * @see RoadSideUnitCamSendingApp
 * @see TrafficLightCamSendingApp
 */
public abstract class AbstractCamSendingApp<OS extends OperatingSystem> extends ConfigurableApplication<CEtsi, OS> {

    private Data data;

    /**
     * Constructor using the default configuration filename ("EtsiApplication")
     * and the preconfigured ETSI specific parameter.
     */
    protected AbstractCamSendingApp() {
        this(CEtsi.class, "EtsiApplication");
    }

    /**
     * Constructor with a configuration class and a configuration filename.
     *
     * @param configClazz    Configuration class defining ETSI specific parameter.
     * @param configFileName Configuration filename.
     */
    protected AbstractCamSendingApp(Class<? extends CEtsi> configClazz, String configFileName) {
        super(configClazz, configFileName);
    }

    /**
     * Abstract method generating a {@link Data}-object from
     * the applications' state.
     *
     * @return the ETSI conform {@link Data}-object
     */
    public abstract Data generateEtsiData();

    /**
     * Calls setUp of {@link ConfigurableApplication} class (where it tries to instantiate the config class),
     * activates AdHoc module and schedules the first event to sample with a possible random timer offset.
     */
    @Override
    public void onStartup() {
        getLog().debugSimTime(this, "Initialize application");
        activateCommunicationModule();
        firstSample();
    }

    /**
     * Activates AdHoc module.
     */
    protected void activateCommunicationModule() {
        getOperatingSystem().getAdHocModule().enable(
                new AdHocModuleConfiguration()
                        .camMinimalPayloadLength(getConfiguration().minimalPayloadLength / DATA.BYTE)
                        .addRadio().channel(AdHocChannel.CCH).power(50).create()
        );
    }

    /**
     * Sends cooperative awareness message.
     */
    protected void sendCam() {
        getOperatingSystem().getAdHocModule().sendCam();
    }

    /**
     * Schedules the first event to sample with a possible random timer offset.
     */
    private void firstSample() {
        long randomOffset = (long) (getRandom().nextDouble() * getConfiguration().maxStartOffset);
        getOperatingSystem().getEventManager().addEvent(
                getOperatingSystem().getSimulationTime() + getConfiguration().minInterval + randomOffset, this::checkDataAndSendCam
        );
    }

    /**
     * Schedules a new event to sample something in the minimal interval given by configuration.
     */
    private void sample() {
        if (!canProcessEvent()) {
            return;
        }

        if (isTornDown()) {
            return;
        }

        getOperatingSystem().getEventManager().addEvent(
                getOperatingSystem().getSimulationTime() + getConfiguration().minInterval, this::checkDataAndSendCam
        );
    }

    void checkDataAndSendCam(Event event) {
        if (dataChanged()) {
            sendCam();
        }
        sample();
    }

    /**
     * This method check's if the {@link Data} of an application changed,
     * updates the {@link #data}-field.
     *
     * @return {@code true} if data changed, else {@code false}
     */
    private boolean dataChanged() {
        // initialize data the first time this method is called
        if (data == null) {
            data = generateEtsiData();
            return false;
        }

        Data newData = generateEtsiData();
        if (newData == null) {
            getLog().debugSimTime(this, "Could not check delta. Data is not available yet.");
            return false;
        }

        CheckDelta checkDelta = checkMaxDelta(data, newData, getConfiguration());
        if (checkDelta != null) {
            getLog().debugSimTime(this, "Message will be sent: reason: {}, delta: {}", checkDelta.reason, checkDelta.getDeltaValue());
            data = newData;
            return true;
        } else {
            getLog().debugSimTime(this, "No message will be sent.");
            return false;
        }

    }

    /**
     * Checks if the delta of specific {@link Data} values exceed
     * their max deltas and creates a {@link CheckDelta} with
     * the changed values.
     *
     * @param oldData  the prior {@link Data}-object
     * @param newData  the current {@link Data}-object
     * @param maxDelta the {@link CEtsi} configuration containing the maximum deltas
     * @return a {@link CheckDelta} with a {@link Reason} as to why the data should be changed, or {@link null} if nothing changed
     */
    private CheckDelta checkMaxDelta(final Data oldData, final Data newData, final CEtsi maxDelta) {
        final CheckDelta checkDelta = new CheckDelta();

        if (maxDelta.maxInterval != null) {
            final long lDelta = newData.time - oldData.time;
            // did we exceed the maximum time limit?
            if (lDelta >= maxDelta.maxInterval) {
                checkDelta.longDelta = lDelta;
                checkDelta.reason = Reason.MAX_INTERVAL;
                return checkDelta;
            }
        }

        if (maxDelta.headingChange != null) {
            // did the heading change?
            final double dDelta = Math.abs(newData.heading - oldData.heading);
            if (dDelta > maxDelta.headingChange) {
                checkDelta.doubleDelta = dDelta;
                checkDelta.reason = Reason.HEADING_CHANGE;
                return checkDelta;
            }
        }

        if (maxDelta.velocityChange != null) {
            // did the velocity change?
            final double dDelta = (newData.velocity - oldData.velocity);
            if (dDelta > maxDelta.velocityChange) {
                checkDelta.doubleDelta = dDelta;
                checkDelta.reason = Reason.VELOCITY_CHANGE;
                return checkDelta;
            }
        }

        if (maxDelta.positionChange != null) {
            // did the position change?
            double positionalDelta = 0d;
            if (newData.projectedPosition != null && oldData.projectedPosition != null) {
                positionalDelta = oldData.projectedPosition.distanceTo(newData.projectedPosition);
            } else if (newData.position != null && oldData.position != null) {
                positionalDelta = oldData.position.distanceTo(newData.position);
            }
            if (positionalDelta > maxDelta.positionChange) {
                checkDelta.doubleDelta = positionalDelta;
                checkDelta.reason = Reason.POSITION_CHANGE;
                return checkDelta;
            }
        }

        return null;
    }

    @Override
    public void onShutdown() {
        getLog().debugSimTime(this, "Shutdown application");
    }

    @Override
    public void processEvent(Event event) throws Exception {
        // nop
    }

    public static class Data {
        /**
         * Heading of the ETSI-unit in °.
         */
        public double heading;
        /**
         * Velocity of the ETSI-unit in m/s.
         */
        public double velocity;
        /**
         * Position of the ETSI-unit as {@link GeoPoint}.
         */
        public GeoPoint position;
        /**
         * Position projected onto cartesian space.
         */
        public CartesianPoint projectedPosition;
        /**
         * Time of the data in ns.
         */
        public long time = 0;
    }

    enum Reason {

        MAX_INTERVAL {
            public String toString() {
                return "Maximum interval was exceeded";
            }
        },
        HEADING_CHANGE {
            public String toString() {
                return "Heading changed";
            }
        },
        VELOCITY_CHANGE {
            public String toString() {
                return "Velocity changed";
            }
        },
        POSITION_CHANGE {
            public String toString() {
                return "Position changed";
            }
        }
    }

    /**
     * Return class for a delta check.
     */
    private static class CheckDelta {

        public Reason reason;
        Long longDelta;
        Double doubleDelta;

        String getDeltaValue() {
            if (longDelta != null) {
                return longDelta.toString();
            } else {
                if (doubleDelta != null) {
                    return doubleDelta.toString();
                }
                return null;
            }
        }
    }
}
