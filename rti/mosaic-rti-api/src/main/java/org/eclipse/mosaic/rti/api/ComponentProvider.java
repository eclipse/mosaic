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

package org.eclipse.mosaic.rti.api;

import org.eclipse.mosaic.lib.math.RandomNumberGenerator;

import javax.annotation.Nonnull;

/**
 * Provides access to all components required for the simulation, such as the {@link TimeManagement},
 * {@link InteractionManagement} or {@link FederationManagement} instances.
 */
public interface ComponentProvider {

    /**
     * Returns the identifier of this federation for logging purposes.
     *
     * @return the identifier of this federation
     */
    @Nonnull String getFederationId();

    /**
     * Provides access to the {@link TimeManagement} implementation of this federation.
     *
     * @return the {@link TimeManagement} implementation
     */
    @Nonnull TimeManagement getTimeManagement();

    /**
     * Provides access to the {@link FederationManagement} implementation of this federation.
     * The {@link FederationManagement} takes care of deploying, starting, and stopping of federates.
     *
     * @return the {@link FederationManagement} implementation
     */
    @Nonnull FederationManagement getFederationManagement();

    /**
     * Provides access to the {@link InteractionManagement} implementation of this federation.
     * The {@link InteractionManagement} takes care of distributing interactions to subscribed
     * ambassadors.
     *
     * @return the {@link InteractionManagement} implementation
     */
    @Nonnull InteractionManagement getInteractionManagement();

    /**
     * Provides access to the {@link Monitor} implementation of this federation. The {@link Monitor} can
     * be use to monitor certain events, e.g. the begin of the simulation, time of interactions, and the like.
     *
     * @return the {@link Monitor} implementation
     */
    @Nonnull Monitor getMonitor();

    /**
     * Creates a new instance of the {@link RtiAmbassador} for the bridge between RTI and the ambassador.
     *
     * @param federateId the unique ID of the federate
     * @return a new instance of the {@link RtiAmbassador}
     */
    @Nonnull RtiAmbassador createRtiAmbassador(String federateId);

    /**
     * Creates a new instance of a {@link RandomNumberGenerator}.
     *
     * @return a new instance of the {@link RandomNumberGenerator}
     */
    @Nonnull RandomNumberGenerator createRandomNumberGenerator();
}
