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

package org.eclipse.mosaic.rti.api;

import org.eclipse.mosaic.rti.api.parameters.InteractionDescriptor;

import com.google.common.collect.ImmutableCollection;

import java.util.Collection;

/**
 * The <code>InteractionManagement</code> is responsible for the exchange of
 * data among federates using instances of {@link Interaction}. The RTI
 * and its federates are decoupled through a publish-subscribe pattern
 * implemented here.
 */
public interface InteractionManagement {

    /**
     * This method is used by a federate to subscribe interactions.
     *
     * @param federateId     unique string identifying a federate
     * @param interactionIds list of strings representing interactions of interest
     * @throws IllegalArgumentException if the given federate is not known
     */
    void subscribeInteractions(String federateId, Collection<InteractionDescriptor> interactionIds) throws IllegalArgumentException;

    /**
     * Allows a federate to remove its interest in interactions.
     *
     * @param federateId     unique integer identifying a federate
     * @param interactionIds list of strings representing the interactions to be unregistered
     */
    void cancelInteractionSubscription(String federateId, Collection<String> interactionIds);

    /**
     * Provides a list the subscribed interactions of a federate.
     *
     * @param federateId unique string identifying a federate
     * @return set of subscribed interactions
     */
    ImmutableCollection<String> getSubscribedInteractions(String federateId);

    /**
     * A published interaction is forwarded to each subscribed federate directly
     * after it has been published.
     *
     * @param interaction An interaction contains its creation time, an identifier
     *                    describing its type and optional data.
     */
    void publishInteraction(Interaction interaction) throws IllegalValueException, InternalFederateException;
}
