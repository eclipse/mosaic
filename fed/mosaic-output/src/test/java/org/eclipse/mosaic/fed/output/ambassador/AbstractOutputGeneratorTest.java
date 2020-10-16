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

package org.eclipse.mosaic.fed.output.ambassador;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;

import org.eclipse.mosaic.interactions.communication.V2xMessageAcknowledgement;
import org.eclipse.mosaic.interactions.communication.V2xMessageReception;
import org.eclipse.mosaic.interactions.communication.V2xMessageRemoval;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.interactions.vehicle.VehicleResume;
import org.eclipse.mosaic.lib.objects.v2x.V2xReceiverInformation;
import org.eclipse.mosaic.rti.api.Interaction;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test suite for {@link AbstractOutputGenerator}.
 */
public class AbstractOutputGeneratorTest {

    @Test
    public void generatorWithAnnotations_registeredInteractionByAnnotation() {
        //SETUP
        TestOutputGenerator generator = spy(new TestOutputGenerator());

        //RUN
        generator.handleInteraction(new V2xMessageTransmission(0, null));
        generator.handleInteraction(new V2xMessageReception(0, "", 0, new V2xReceiverInformation(0)));

        //ASSERT
        verify(generator, never()).handleUnregisteredInteraction(any());
        assertEquals(1, generator.callsV2xMessageTransmission.get());
        assertEquals(1, generator.callsV2xMessageReception.get());
        assertEquals(0, generator.callsVehicleResume.get());
        assertEquals(0, generator.callsV2xMessageAcknowledgement.get());
    }

    @Test
    public void generatorWithAnnotations_registeredMessageByExtendedAnnotation() {
        //SETUP
        TestOutputGenerator generator = spy(new TestOutputGenerator());

        //RUN
        generator.handleInteraction(new VehicleResume(0, null));

        //ASSERT
        verify(generator, never()).handleUnregisteredInteraction(any());
        assertEquals(0, generator.callsV2xMessageTransmission.get());
        assertEquals(0, generator.callsV2xMessageReception.get());
        assertEquals(1, generator.callsVehicleResume.get());
        assertEquals(0, generator.callsV2xMessageAcknowledgement.get());
    }

    @Test
    public void generatorWithAnnotations_registeredMessageByManualRegistration() {
        //SETUP
        TestOutputGenerator generator = spy(new TestOutputGenerator());

        //RUN
        generator.handleInteraction(new V2xMessageAcknowledgement(0, 0, null));

        //ASSERT
        verify(generator, never()).handleUnregisteredInteraction(any());
        assertEquals(0, generator.callsV2xMessageTransmission.get());
        assertEquals(0, generator.callsV2xMessageReception.get());
        assertEquals(0, generator.callsVehicleResume.get());
        assertEquals(1, generator.callsV2xMessageAcknowledgement.get());
    }

    @Test
    public void generatorWithAnnotations_unregisteredMessage() {
        //SETUP
        TestOutputGenerator generator = spy(new TestOutputGenerator());

        //RUN
        generator.handleInteraction(new V2xMessageRemoval(0, Lists.newArrayList()));

        //ASSERT
        verify(generator, times(1)).handleUnregisteredInteraction(any());
        assertEquals(0, generator.callsV2xMessageTransmission.get());
        assertEquals(0, generator.callsV2xMessageReception.get());
        assertEquals(0, generator.callsVehicleResume.get());
        assertEquals(0, generator.callsV2xMessageAcknowledgement.get());
    }

    /**
     * Extension of {@link AbstractOutputGenerator} for testing purposes.
     */
    static class TestOutputGenerator extends AbstractOutputGenerator {

        private AtomicInteger callsV2xMessageTransmission = new AtomicInteger();
        private AtomicInteger callsV2xMessageReception = new AtomicInteger();
        private AtomicInteger callsVehicleResume = new AtomicInteger();
        private AtomicInteger callsV2xMessageAcknowledgement = new AtomicInteger();

        TestOutputGenerator() {
            registerInteractionForOutputGeneration("V2xMessageAcknowledgement", this::handleV2xMessageAcknowledgement);
        }

        @Handle
        public void handleV2xMessageTransmission(V2xMessageTransmission v2xMessageTransmission) {
            callsV2xMessageTransmission.incrementAndGet();
        }

        @Handle
        public void handleV2xMessageReception(V2xMessageReception v2xMessageReception) {
            callsV2xMessageReception.incrementAndGet();
        }

        @Handle("VehicleResume")
        public void handleVehicleResume(Interaction vehicleResume) {
            callsVehicleResume.incrementAndGet();
        }

        public void handleV2xMessageAcknowledgement(Interaction dummyInteraction) {
            callsV2xMessageAcknowledgement.incrementAndGet();
        }

    }

}