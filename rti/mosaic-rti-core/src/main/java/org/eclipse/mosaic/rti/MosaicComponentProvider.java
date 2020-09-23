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
 */

package org.eclipse.mosaic.rti;

import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.rti.api.ComponentProvider;
import org.eclipse.mosaic.rti.api.FederationManagement;
import org.eclipse.mosaic.rti.api.InteractionManagement;
import org.eclipse.mosaic.rti.api.Monitor;
import org.eclipse.mosaic.rti.api.RtiAmbassador;
import org.eclipse.mosaic.rti.api.TimeManagement;
import org.eclipse.mosaic.rti.federation.DistributedFederationManagement;
import org.eclipse.mosaic.rti.federation.LocalFederationManagement;
import org.eclipse.mosaic.rti.interaction.TypeBasedInteractionManagement;
import org.eclipse.mosaic.rti.monitor.ActivityLoggingMonitor;
import org.eclipse.mosaic.rti.time.MultiThreadedTimeManagement;
import org.eclipse.mosaic.rti.time.SequentialTimeManagement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * <p>Provides access to component implementation for the
 * runtime infrastructure, such as the {@link TimeManagement},
 * {@link FederationManagement}, and {@link InteractionManagement}.
 * </p>
 * This default implementation creates instances of:
 * <ul>
 *  <li>{@link LocalFederationManagement}</li>
 *  <li>{@link TypeBasedInteractionManagement}</li>
 *  <li>{@link SequentialTimeManagement}</li>
 * </ul>
 */
public class MosaicComponentProvider implements ComponentProvider {

    private final String federationId;
    private final MosaicComponentParameters componentParameters;

    private final FederationManagement federationManagement;
    private final TimeManagement timeManagement;
    private final InteractionManagement interactionManagement;
    private final Monitor monitor;

    /**
     * Creates instances for the {@link FederationManagement}, {@link TimeManagement}, and {@link InteractionManagement}
     * which are provided by this class further on.
     *
     * @param componentParameters parameters for the components
     */
    public MosaicComponentProvider(MosaicComponentParameters componentParameters) {
        this.federationId = componentParameters.getFederationId();
        this.componentParameters = componentParameters;

        federationManagement = createFederationManagement(componentParameters);
        interactionManagement = createInteractionManagement(componentParameters);
        timeManagement = createTimeManagement(componentParameters);
        monitor = createMonitor(componentParameters);
    }

    protected TimeManagement createTimeManagement(MosaicComponentParameters componentParameters) {
        if (componentParameters.getNumberOfThreads() > 1) {
            return new MultiThreadedTimeManagement(this, componentParameters);
        } else {
            return new SequentialTimeManagement(this, componentParameters);
        }
    }

    protected InteractionManagement createInteractionManagement(MosaicComponentParameters componentParameters) {
        return new TypeBasedInteractionManagement(this);
    }

    protected FederationManagement createFederationManagement(MosaicComponentParameters componentParameters) {
        return new DistributedFederationManagement(this);
    }

    protected Monitor createMonitor(MosaicComponentParameters componentParameters) {
        Logger activityLog = LoggerFactory.getLogger("activities");
        if (activityLog != null && activityLog.isInfoEnabled()) {
            return new ActivityLoggingMonitor(activityLog);
        } else {
            return new Monitor() {};
        }
    }

    @Nonnull
    @Override
    public final String getFederationId() {
        return federationId;
    }

    @Nonnull
    @Override
    public final TimeManagement getTimeManagement() {
        return timeManagement;
    }

    @Nonnull
    @Override
    public final FederationManagement getFederationManagement() {
        return federationManagement;
    }

    @Nonnull
    @Override
    public final InteractionManagement getInteractionManagement() {
        return interactionManagement;
    }

    @Nonnull
    @Override
    public final Monitor getMonitor() {
        return monitor;
    }

    @Nonnull
    @Override
    public RtiAmbassador createRtiAmbassador(String federateId) {
        return new MosaicRtiAmbassador(this, federateId);
    }

    @Nonnull
    @Override
    public RandomNumberGenerator createRandomNumberGenerator() {
        if (componentParameters.getRandomSeed() == null) {
            return new DefaultRandomNumberGenerator();
        } else {
            return new DefaultRandomNumberGenerator(componentParameters.getRandomSeed());
        }
    }
}
