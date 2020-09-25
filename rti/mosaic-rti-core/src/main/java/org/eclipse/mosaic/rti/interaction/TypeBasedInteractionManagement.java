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

package org.eclipse.mosaic.rti.interaction;

import org.eclipse.mosaic.rti.api.ComponentProvider;
import org.eclipse.mosaic.rti.api.FederateAmbassador;
import org.eclipse.mosaic.rti.api.Interaction;
import org.eclipse.mosaic.rti.api.InteractionManagement;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.parameters.InteractionDescriptor;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This implementation of {@link InteractionManagement} allows a type based message subscription.
 */
public class TypeBasedInteractionManagement implements InteractionManagement {

    private final static Logger LOG = LoggerFactory.getLogger(TypeBasedInteractionManagement.class);

    /**
     * mapping between an interaction type id and a list of subscribed ambassadors.
     */
    protected final Map<String, List<FederateAmbassador>> interactionAmbassadorMap = new HashMap<>();

    protected final ComponentProvider federation;

    public TypeBasedInteractionManagement(ComponentProvider federation) {
        this.federation = federation;
    }

    @Override
    public void subscribeInteractions(String federateId, Collection<InteractionDescriptor> interactionIds) throws IllegalArgumentException {
        if (!federation.getFederationManagement().isFederateJoined(federateId)) {
            throw new IllegalArgumentException("Federate with id \"" + federateId + "\" is unknown.");
        }

        final FederateAmbassador ambassador = federation.getFederationManagement().getAmbassador(federateId);

        for (InteractionDescriptor interaction : interactionIds) {
            List<FederateAmbassador> subscribedAmbassadors =
                    interactionAmbassadorMap.computeIfAbsent(interaction.interactionId, (k) -> new ArrayList<>());

            if (!subscribedAmbassadors.contains(ambassador)) {
                subscribedAmbassadors.add(ambassador);
            }
        }
        // sort order of ambassadors according to priority for each message
        for (List<FederateAmbassador> ambassadorMessageMapping : interactionAmbassadorMap.values()) {
            Collections.sort(ambassadorMessageMapping);
        }
    }

    @Override
    public void cancelInteractionSubscription(String federateId, Collection<String> interactionIds) {
        if (interactionIds != null) {
            for (String intId : interactionIds) {
                if (interactionAmbassadorMap.containsKey(intId)) {
                    interactionAmbassadorMap.get(intId).remove(federation.getFederationManagement().getAmbassador(federateId));
                }
            }
        }
    }

    @Override
    public ImmutableCollection<String> getSubscribedInteractions(String federateId) {
        final ArrayList<String> subscribedInteractions = new ArrayList<>();
        FederateAmbassador ambassador = federation.getFederationManagement().getAmbassador(federateId);
        for (Map.Entry<String, List<FederateAmbassador>> entry : interactionAmbassadorMap.entrySet()) {
            String interactionId = entry.getKey();
            if (entry.getValue().contains(ambassador)) {
                subscribedInteractions.add(interactionId);
            }
        }
        return ImmutableList.copyOf(subscribedInteractions);
    }

    @Override
    public void publishInteraction(Interaction interaction) throws InternalFederateException {
        federation.getMonitor().onInteraction(interaction);

        List<FederateAmbassador> ambassadors = this.interactionAmbassadorMap.get(interaction.getTypeId());
        if (ambassadors == null || ambassadors.isEmpty()) {
            return;
        }

        for (FederateAmbassador ambassador : ambassadors) {
            try {
                federation.getMonitor().onReceiveInteraction(ambassador.getId(), interaction);
                ambassador.receiveInteraction(interaction);
            } catch (InternalFederateException e) {
                LOG.error("Error during interaction distribution", e);
                throw e;
            }
        }
    }
}
