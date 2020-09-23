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

package org.eclipse.mosaic.rti.interaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.rti.MosaicRtiAmbassador;
import org.eclipse.mosaic.rti.api.AbstractFederateAmbassador;
import org.eclipse.mosaic.rti.api.ComponentProvider;
import org.eclipse.mosaic.rti.api.FederateAmbassador;
import org.eclipse.mosaic.rti.api.FederationManagement;
import org.eclipse.mosaic.rti.api.Interaction;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.Monitor;
import org.eclipse.mosaic.rti.api.parameters.AmbassadorParameter;
import org.eclipse.mosaic.rti.api.parameters.FederateDescriptor;
import org.eclipse.mosaic.rti.api.parameters.InteractionDescriptor;
import org.eclipse.mosaic.rti.federation.LocalFederationManagement;
import org.eclipse.mosaic.rti.junit.TestDummyInteraction;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class InteractionPriorityTest {

    private static int counter = 0; // Used to keep track of message receive order

    class TestTypeBasedInteractionManagement extends TypeBasedInteractionManagement {
        private TestTypeBasedInteractionManagement(ComponentProvider federation) {
            super(federation);
        }

        private Map<String, List<FederateAmbassador>> getInteractionMap() {
            return interactionAmbassadorMap;
        }
    }

    class TestFederateAmbassador extends AbstractFederateAmbassador {
        private int instanceCounter = -1;

        private TestFederateAmbassador(AmbassadorParameter ambassadorParameter, byte prio) {
            super(ambassadorParameter);
            this.descriptor = new FederateDescriptor(ambassadorParameter.ambassadorId, this, prio);
        }

        @Override
        public void processInteraction(Interaction interaction) throws InternalFederateException {
            super.processInteraction(interaction);
            instanceCounter = counter++;
        }

        @Override
        public boolean isTimeConstrained() {
            return false;
        }

        @Override
        public boolean isTimeRegulating() {
            return false;
        }
    }

    private final Pair<String, Byte> lowFed = new ImmutablePair<>("FedLow", Byte.MAX_VALUE);
    private final Pair<String, Byte> highFed = new ImmutablePair<>("FedHigh", Byte.MIN_VALUE);
    private final FederateAmbassador ambassadorLowPrio =
            new TestFederateAmbassador(new AmbassadorParameter(lowFed.getKey(), new File("")), lowFed.getValue());
    private final FederateAmbassador ambassadorHighPrio =
            new TestFederateAmbassador(new AmbassadorParameter(highFed.getKey(), new File("")), highFed.getValue());
    private final static int interactionTime = 1337;
    private final static String interactionName = "type1";
    private final Collection<InteractionDescriptor> interactions = Lists.newArrayList(new InteractionDescriptor(interactionName));

    private final ComponentProvider componentProvider = mock(ComponentProvider.class);

    @Before
    public void setup() {
        when(componentProvider.getMonitor()).thenReturn(new Monitor() {});
        doAnswer((invocation) -> new MosaicRtiAmbassador(componentProvider, invocation.getArgument(0)))
                .when(componentProvider).createRtiAmbassador(anyString());
    }

    @Test
    public void messageOrderTest() throws Exception {

        FederationManagement fedMgmt = new LocalFederationManagement(componentProvider);
        fedMgmt.addFederate(new FederateDescriptor(lowFed.getKey(), ambassadorLowPrio, ambassadorLowPrio.getPriority()));
        fedMgmt.addFederate(new FederateDescriptor(highFed.getKey(), ambassadorHighPrio, ambassadorHighPrio.getPriority()));

        when(componentProvider.getFederationManagement()).thenReturn(fedMgmt);
        TestTypeBasedInteractionManagement mgmt = new TestTypeBasedInteractionManagement(componentProvider);

        mgmt.subscribeInteractions(lowFed.getKey(), interactions);
        mgmt.subscribeInteractions(highFed.getKey(), interactions);

        final Map<String, List<FederateAmbassador>> interactionMap = mgmt.getInteractionMap();
        final List<FederateAmbassador> orderedByPriority = interactionMap.get(interactionName);
        assertEquals(orderedByPriority.get(0).getId(), highFed.getKey()); // Federate with the highest priority should be head of the list
        assertEquals(orderedByPriority.get(1).getId(), lowFed.getKey());
    }

    @Test
    public void messagePriorityTest() throws Exception {

        FederationManagement fedMgmt = new LocalFederationManagement(componentProvider);
        fedMgmt.addFederate(new FederateDescriptor(lowFed.getKey(), ambassadorLowPrio, highFed.getValue()));
        fedMgmt.addFederate(new FederateDescriptor(highFed.getKey(), ambassadorHighPrio, highFed.getValue()));

        when(componentProvider.getFederationManagement()).thenReturn(fedMgmt);
        TestTypeBasedInteractionManagement mgmt = new TestTypeBasedInteractionManagement(componentProvider);

        mgmt.subscribeInteractions(lowFed.getKey(), interactions);
        mgmt.subscribeInteractions(highFed.getKey(), interactions);

        assertEquals((byte) lowFed.getValue(), fedMgmt.getAmbassador(lowFed.getKey()).getPriority());
        assertEquals((byte) highFed.getValue(), fedMgmt.getAmbassador(highFed.getKey()).getPriority());
    }

    @Test
    public void messageDeliveryTest() throws Exception {
        FederationManagement fedMgmt = new LocalFederationManagement(componentProvider);
        fedMgmt.addFederate(new FederateDescriptor(lowFed.getKey(), ambassadorLowPrio, lowFed.getValue()));
        fedMgmt.addFederate(new FederateDescriptor(highFed.getKey(), ambassadorHighPrio, highFed.getValue()));

        when(componentProvider.getFederationManagement()).thenReturn(fedMgmt);
        TestTypeBasedInteractionManagement mgmt = new TestTypeBasedInteractionManagement(componentProvider);

        // Make sure both messages have the same time stamp in order to test if the priorities work
        final Interaction interactionType1 = new TestDummyInteraction(interactionTime, "type1");

        mgmt.subscribeInteractions(lowFed.getKey(), interactions);
        mgmt.subscribeInteractions(highFed.getKey(), interactions);
        mgmt.publishInteraction(interactionType1);
        boolean highPriorityEarlier = ((TestFederateAmbassador) ambassadorHighPrio).instanceCounter
                < ((TestFederateAmbassador) ambassadorLowPrio).instanceCounter;
        assertTrue(highPriorityEarlier);
    }
}
