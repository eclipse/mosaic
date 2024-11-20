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

package org.eclipse.mosaic.fed.cell.chain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.fed.cell.config.CNetwork;
import org.eclipse.mosaic.fed.cell.config.model.CNetworkProperties;
import org.eclipse.mosaic.fed.cell.data.ConfigurationData;
import org.eclipse.mosaic.fed.cell.junit.CellSimulationRule;
import org.eclipse.mosaic.fed.cell.message.CellModuleMessage;
import org.eclipse.mosaic.fed.cell.module.GeocasterModule;
import org.eclipse.mosaic.fed.cell.module.streammodules.DownstreamModule;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.junit.IpResolverRule;
import org.eclipse.mosaic.lib.math.DefaultRandomNumberGenerator;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.objects.addressing.AdHocMessageRoutingBuilder;
import org.eclipse.mosaic.lib.objects.addressing.CellMessageRoutingBuilder;
import org.eclipse.mosaic.lib.objects.addressing.IpResolver;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.Interaction;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.RtiAmbassador;
import org.eclipse.mosaic.rti.api.parameters.AmbassadorParameter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;

public class ChainManagerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

    @Mock
    private V2xMessage v2XMessage;

    @Mock
    public RtiAmbassador rti;

    private AmbassadorParameter ambassadorParameter;

    @Rule
    public IpResolverRule ipResolverRule = new IpResolverRule();

    @Rule
    public CellSimulationRule simulationRule = new CellSimulationRule();

    private final RandomNumberGenerator rng = new DefaultRandomNumberGenerator(182931861823L);
    private final List<Event> eventsAdded = new ArrayList<>();
    private final List<Interaction> rtiInteractionsSent = new ArrayList<>();
    private final AtomicReference<MessageRouting> routing = new AtomicReference<>();

    private ChainManager chainManager;

    @Before
    public void setup() throws IllegalValueException, InternalFederateException {
        File ambassadorConfiguration = new File("cell_config.json");
        ambassadorParameter = new AmbassadorParameter("Cell", ambassadorConfiguration);
        chainManager = new ChainManager(rti, rng, ambassadorParameter) {
            @Override
            public void addEvent(@Nonnull Event event) {
                eventsAdded.add(event);
            }
        };
        when(v2XMessage.getRouting()).thenAnswer(x -> routing.get());

        doAnswer(
                invocationOnMock -> rtiInteractionsSent.add((Interaction) invocationOnMock.getArguments()[0])
        ).when(rti).triggerInteraction(ArgumentMatchers.isA(Interaction.class));

        CNetwork region = new CNetwork();
        region.globalNetwork = new CNetworkProperties();
        region.globalNetwork.id = "Global Region";

        ConfigurationData.INSTANCE.setNetworkConfig(region);
        IpResolver.getSingleton().registerHost("veh_0");
    }

    @Test
    public void testStartEvent_cellRouting() {
        //SETUP
        routing.set(new CellMessageRoutingBuilder("veh_0", null)
                .destination(new byte[]{1, 2, 3, 4})
                .topological()
                .build());

        V2xMessageTransmission sendV2xMsg = new V2xMessageTransmission(12 * TIME.SECOND, v2XMessage);

        //RUN
        chainManager.startEvent(sendV2xMsg);

        //ASSERT
        assertEquals(1, eventsAdded.size());
        assertEquals(12 * TIME.SECOND, eventsAdded.get(0).getTime());
    }

    @Test
    public void testStartEvent_adhocRouting() {
        //SETUP
        routing.set(new AdHocMessageRoutingBuilder("veh_0", null).broadcast().topological().build());

        V2xMessageTransmission sendV2xMsg = new V2xMessageTransmission(12 * TIME.SECOND, v2XMessage);

        //RUN
        chainManager.startEvent(sendV2xMsg);

        //ASSERT
        assertEquals(0, eventsAdded.size());
    }

    @Test
    public void testFinishEvent_notifyOnFinish() {
        //SETUP
        CellModuleMessage cellModuleMessage = new CellModuleMessage.Builder("Upstream", "Geocaster")
                .endTime(43 * TIME.SECOND)
                .build();

        //RUN
        chainManager.finishEvent(cellModuleMessage);

        //ASSERT
        assertEquals(1, eventsAdded.size());
        assertTrue(eventsAdded.get(0).getProcessors().get(0) instanceof GeocasterModule);
        assertEquals(43 * TIME.SECOND, eventsAdded.get(0).getTime());
    }

    @Test
    public void testFinishEvent_NoNotifyOnFinish() {
        //SETUP
        CellModuleMessage cellModuleMessage = new CellModuleMessage.Builder("Upstream", "Geocaster")
                .endTime(43 * TIME.SECOND)
                .build();

        //RUN
        chainManager.finishEvent(cellModuleMessage);

        //ASSERT
        assertEquals(1, eventsAdded.size());
        assertTrue(eventsAdded.get(0).getProcessors().get(0) instanceof GeocasterModule);
        assertEquals(43 * TIME.SECOND, eventsAdded.get(0).getTime());
        assertEquals(0, rtiInteractionsSent.size());
    }

    @Test
    public void testFinishEvent_NoEvent() {
        //SETUP
        CellModuleMessage cellModuleMessage = new CellModuleMessage.Builder("Upstream", null)
                .endTime(43 * TIME.SECOND)
                .build();

        //RUN
        chainManager.finishEvent(cellModuleMessage);

        //ASSERT
        assertEquals(0, eventsAdded.size());
        assertEquals(0, rtiInteractionsSent.size());
    }

    @Test
    public void testFinishEvent_forwardToMultipleProcessors() {
        //SETUP
        CellModuleMessage cellModuleMessageGeocaster = new CellModuleMessage.Builder("Upstream", "Geocaster")
                .endTime(43 * TIME.SECOND)
                .build();

        CellModuleMessage cellModuleMessageDownstream = new CellModuleMessage.Builder("Upstream", "Downstream")
                .endTime(43 * TIME.SECOND)
                .build();

        //RUN
        chainManager.finishEvent(cellModuleMessageGeocaster);
        chainManager.finishEvent(cellModuleMessageDownstream);

        //ASSERT
        assertEquals(2, eventsAdded.size());
        assertTrue(eventsAdded.get(0).getProcessors().get(0) instanceof GeocasterModule);
        assertEquals(43 * TIME.SECOND, eventsAdded.get(0).getTime());
        assertEquals(43 * TIME.SECOND, eventsAdded.get(1).getTime());
        assertTrue(eventsAdded.get(1).getProcessors().get(0) instanceof DownstreamModule);
        assertEquals(0, rtiInteractionsSent.size());
    }
}