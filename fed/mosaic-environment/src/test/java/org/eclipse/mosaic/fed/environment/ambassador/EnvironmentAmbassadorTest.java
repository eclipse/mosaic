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

package org.eclipse.mosaic.fed.environment.ambassador;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.fed.environment.config.CEventTime;
import org.eclipse.mosaic.interactions.environment.EnvironmentSensorActivation;
import org.eclipse.mosaic.interactions.environment.EnvironmentSensorUpdates;
import org.eclipse.mosaic.interactions.environment.GlobalEnvironmentUpdates;
import org.eclipse.mosaic.interactions.traffic.VehicleUpdates;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.IllegalValueException;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.RtiAmbassador;
import org.eclipse.mosaic.rti.api.parameters.AmbassadorParameter;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Tests for {@link EnvironmentAmbassador}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EnvironmentAmbassadorTest {

    @Mock
    private RtiAmbassador rtiMock;

    @Mock
    private VehicleData vehicleDataMock;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private EnvironmentAmbassador ambassador;

    private GlobalEnvironmentUpdates globalEnvironmentUpdates = null;
    private EnvironmentSensorUpdates environmentSensorUpdates = null;

    @Before
    public void before() throws Exception {
        Mockito.doAnswer((Answer<Void>) invocation -> {
            EnvironmentAmbassadorTest.this.globalEnvironmentUpdates = invocation.getArgument(0);
            return null;
        }).when(rtiMock).triggerInteraction(ArgumentMatchers.isA(GlobalEnvironmentUpdates.class));

        Mockito.doAnswer((Answer<Void>) invocation -> {
            EnvironmentAmbassadorTest.this.environmentSensorUpdates = invocation.getArgument(0);
            return null;
        }).when(rtiMock).triggerInteraction(ArgumentMatchers.isA(EnvironmentSensorUpdates.class));

        this.ambassador = initEventAmbassador("/environment_config.json");
    }

    private EnvironmentAmbassador initEventAmbassador(String path) throws IOException {
        final File configurationFile = tempFolder.newFile();
        try (InputStream in = getClass().getResourceAsStream(path);
             OutputStream out = new FileOutputStream(configurationFile)) {
            IOUtils.copy(in, out);
        }

        EnvironmentAmbassador esa = new EnvironmentAmbassador(new AmbassadorParameter("test", configurationFile));
        esa.setRtiAmbassador(rtiMock);
        return esa;
    }

    @Test
    public void firstCall_oneEvent() throws InternalFederateException, IllegalValueException {
        //RUN
        ambassador.processTimeAdvanceGrant(0);

        //ASSERT
        Mockito.verify(rtiMock).requestAdvanceTime(eq(2 * TIME.SECOND));
        assertNotNull(globalEnvironmentUpdates);
        assertEquals(1, globalEnvironmentUpdates.getCurrentEvents().size());
    }

    @Test
    public void secondCall_oneEvent() throws InternalFederateException, IllegalValueException {
        //RUN
        ambassador.processTimeAdvanceGrant(2 * TIME.SECOND);

        //ASSERT
        Mockito.verify(rtiMock).requestAdvanceTime(eq(5 * TIME.SECOND));
        assertNotNull(globalEnvironmentUpdates);
        assertEquals(2, globalEnvironmentUpdates.getCurrentEvents().size());
    }

    @Test
    public void thirdCall_twoEvents() throws InternalFederateException, IllegalValueException {
        //RUN
        ambassador.processTimeAdvanceGrant(5 * TIME.SECOND);

        //ASSERT
        Mockito.verify(rtiMock).requestAdvanceTime(eq(8 * TIME.SECOND));
        assertNotNull(globalEnvironmentUpdates);
        assertEquals(3, globalEnvironmentUpdates.getCurrentEvents().size());
    }

    @Test
    public void fourthCall_oneEvent() throws InternalFederateException, IllegalValueException {
        //RUN
        ambassador.processTimeAdvanceGrant(8 * TIME.SECOND);

        //ASSERT
        Mockito.verify(rtiMock).requestAdvanceTime(eq(10 * TIME.SECOND));
        assertNotNull(globalEnvironmentUpdates);
        assertEquals(2, globalEnvironmentUpdates.getCurrentEvents().size());
    }

    @Test
    public void fifthCall_noEvent_noTimeAdvance() throws InternalFederateException, IllegalValueException {
        //RUN
        ambassador.processTimeAdvanceGrant(3700 * TIME.SECOND);

        //ASSERT
        Mockito.verify(rtiMock, Mockito.never()).requestAdvanceTime(ArgumentMatchers.anyLong());
        assertNotNull(globalEnvironmentUpdates);
        assertTrue(globalEnvironmentUpdates.getCurrentEvents().isEmpty());
    }

    @Test
    public void emitSensorUpdates_oneActive() throws InternalFederateException, IllegalValueException {
        //PREPARE
        ambassador.processInteraction(new EnvironmentSensorActivation(0, "veh_0"));
        when(vehicleDataMock.getName()).thenReturn("veh_0");
        when(vehicleDataMock.getTime()).thenReturn(4 * TIME.SECOND);
        when(vehicleDataMock.getPosition()).thenReturn(GeoPoint.latLon(52.5,13.2));

        //RUN
        ambassador.processInteraction(new VehicleUpdates(4 * TIME.SECOND, Lists.newArrayList(), Lists.newArrayList(vehicleDataMock), Lists.newArrayList()));

        //ASSERT
        assertNotNull(environmentSensorUpdates);
        assertEquals(1, environmentSensorUpdates.getEvents().size());
    }

    @Test
    public void emitSensorUpdates_twoActive() throws InternalFederateException, IllegalValueException {
        //PREPARE
        ambassador.processInteraction(new EnvironmentSensorActivation(0, "veh_0"));
        when(vehicleDataMock.getName()).thenReturn("veh_0");
        when(vehicleDataMock.getTime()).thenReturn(6 * TIME.SECOND);
        when(vehicleDataMock.getPosition()).thenReturn(GeoPoint.latLon(52.5,13.2));

        //RUN
        ambassador.processInteraction(new VehicleUpdates(6 * TIME.SECOND, Lists.newArrayList(), Lists.newArrayList(vehicleDataMock), Lists.newArrayList()));

        //ASSERT
        assertNotNull(environmentSensorUpdates);
        assertEquals(2, environmentSensorUpdates.getEvents().size());
    }

    @Test
    public void emitSensorUpdates_noEvent() throws InternalFederateException, IllegalValueException {
        //PREPARE
        ambassador.processInteraction(new EnvironmentSensorActivation(0, "veh_0"));
        when(vehicleDataMock.getName()).thenReturn("veh_0");
        when(vehicleDataMock.getTime()).thenReturn(20 * TIME.SECOND);
        when(vehicleDataMock.getPosition()).thenReturn(GeoPoint.latLon(52.5,13.2));

        //RUN
        ambassador.processInteraction(new VehicleUpdates(20 * TIME.SECOND, Lists.newArrayList(), Lists.newArrayList(vehicleDataMock), Lists.newArrayList()));

        //ASSERT
        assertNull(environmentSensorUpdates);
    }


    @Test
    public void testTimeSliceWithinWindowBegin() {
        CEventTime timeWindow = new CEventTime();
        timeWindow.start = 10 * TIME.SECOND;
        timeWindow.end = 50 * TIME.SECOND;

        assertFalse(EnvironmentAmbassador.isInTimeFrame(timeWindow, 9 * TIME.SECOND));
        assertTrue(EnvironmentAmbassador.isInTimeFrame(timeWindow, 10 * TIME.SECOND));
        assertTrue(EnvironmentAmbassador.isInTimeFrame(timeWindow, 30 * TIME.SECOND));
        assertFalse(EnvironmentAmbassador.isInTimeFrame(timeWindow, 50 * TIME.SECOND));
        assertFalse(EnvironmentAmbassador.isInTimeFrame(timeWindow, 51 * TIME.SECOND));

    }

}
