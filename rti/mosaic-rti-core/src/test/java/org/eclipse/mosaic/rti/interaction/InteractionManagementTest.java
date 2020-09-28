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

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.rti.api.ComponentProvider;
import org.eclipse.mosaic.rti.api.FederateAmbassador;
import org.eclipse.mosaic.rti.api.Interaction;
import org.eclipse.mosaic.rti.api.InteractionManagement;
import org.eclipse.mosaic.rti.api.Monitor;
import org.eclipse.mosaic.rti.api.parameters.InteractionDescriptor;
import org.eclipse.mosaic.rti.junit.FederationManagementRule;
import org.eclipse.mosaic.rti.junit.TestDummyInteraction;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collection;

public class InteractionManagementTest {

    private InteractionManagement interactionManagement;

    @Rule
    public FederationManagementRule fedManagement = new FederationManagementRule("ambassador1", "ambassador2");


    @Before
    public void setup() {
        ComponentProvider componentProviderMock = mock(ComponentProvider.class);
        when(componentProviderMock.getMonitor()).thenReturn(new Monitor() {});
        when(componentProviderMock.getFederationManagement()).thenReturn(fedManagement.getFederationManagementMock());
        interactionManagement = spy(new TypeBasedInteractionManagement(componentProviderMock));
    }

    /**
     * Two ambassadors publish various message types
     * Messages of different types are published.
     * It is verified that {@link FederateAmbassador#receiveInteraction(Interaction)} is called accordingly to the subscriptions.
     */
    @Test
    public void subscribe_publishInteraction_ambassadorReceivesMessage() throws Exception {
        //SETUP
        final Collection<InteractionDescriptor> subscribedMessagesAmbassador1 = Lists.newArrayList(new InteractionDescriptor("type1"),
                new InteractionDescriptor("type2"), new InteractionDescriptor("type3"));
        final Collection<InteractionDescriptor> subscribedMessagesAmbassador2 = Lists.newArrayList(new InteractionDescriptor("type1"), new InteractionDescriptor("type4"));
        interactionManagement.subscribeInteractions("ambassador1", subscribedMessagesAmbassador1);
        interactionManagement.subscribeInteractions("ambassador2", subscribedMessagesAmbassador2);

        final Interaction interactionType1 = new TestDummyInteraction(0, "type1");
        final Interaction interactionType2 = new TestDummyInteraction(1, "type2");
        final Interaction interactionType3 = new TestDummyInteraction(2, "type3");
        final Interaction interactionType4 = new TestDummyInteraction(3, "type4");
        final Interaction interactionType5 = new TestDummyInteraction(4, "type5");

        //RUN
        interactionManagement.publishInteraction(interactionType1);
        interactionManagement.publishInteraction(interactionType2);
        interactionManagement.publishInteraction(interactionType3);
        interactionManagement.publishInteraction(interactionType4);
        interactionManagement.publishInteraction(interactionType5);

        //ASSERT
        final FederateAmbassador ambassadorMock1 = fedManagement.getAmbassador("ambassador1");
        final FederateAmbassador ambassadorMock2 = fedManagement.getAmbassador("ambassador2");

        verify(ambassadorMock1, times(1)).receiveInteraction(same(interactionType1));
        verify(ambassadorMock2, times(1)).receiveInteraction(same(interactionType1));

        verify(ambassadorMock1, times(1)).receiveInteraction(same(interactionType2));
        verify(ambassadorMock2, never()).receiveInteraction(same(interactionType2));

        verify(ambassadorMock1, times(1)).receiveInteraction(same(interactionType3));
        verify(ambassadorMock2, never()).receiveInteraction(same(interactionType3));

        verify(ambassadorMock1, never()).receiveInteraction(same(interactionType4));
        verify(ambassadorMock2, times(1)).receiveInteraction(same(interactionType4));

        verify(ambassadorMock1, never()).receiveInteraction(same(interactionType5));
        verify(ambassadorMock2, never()).receiveInteraction(same(interactionType5));
    }

    /**
     * Two message types are subscribed by one ambassador
     * One message type is rescind afterwards.
     * It is verified that {@link FederateAmbassador#receiveInteraction(Interaction)} is called only for one of the subscriptions.
     */
    @Test
    public void subscribe_cancelSubscriptions_ambassadorReceivesInteraction() throws Exception {
        //SETUP
        final Collection<InteractionDescriptor> subscribedMessagesAmbassador1 = Lists.newArrayList(new InteractionDescriptor("type1"), new InteractionDescriptor("type2"));
        interactionManagement.subscribeInteractions("ambassador1", subscribedMessagesAmbassador1);

        final Interaction interactionType1 = new TestDummyInteraction(0, "type1");
        final Interaction interactionType2 = new TestDummyInteraction(0, "type2");

        //RUN        
        interactionManagement.cancelInteractionSubscription("ambassador1", Lists.newArrayList("type2"));
        interactionManagement.publishInteraction(interactionType1);
        interactionManagement.publishInteraction(interactionType2);

        //ASSERT
        final FederateAmbassador ambassadorMock1 = fedManagement.getAmbassador("ambassador1");

        verify(ambassadorMock1, times(1)).receiveInteraction(same(interactionType1));
        verify(ambassadorMock1, never()).receiveInteraction(same(interactionType2));
    }
}
