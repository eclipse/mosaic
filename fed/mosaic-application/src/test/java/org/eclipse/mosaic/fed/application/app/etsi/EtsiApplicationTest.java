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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernelRule;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.AdHocModule;
import org.eclipse.mosaic.fed.application.ambassador.util.UnitLoggerImpl;
import org.eclipse.mosaic.fed.application.app.api.os.OperatingSystem;
import org.eclipse.mosaic.fed.application.config.CEtsi;
import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.util.junit.TestFileRule;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.lib.util.scheduling.EventManager;
import org.eclipse.mosaic.rti.DATA;
import org.eclipse.mosaic.rti.TIME;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

/**
 * Tests the interval sampling of the {@link AbstractCamSendingApp} and
 * the decision if a CAM should be sent based on deltas of time, velocity, position,
 * and heading.
 */
public class EtsiApplicationTest {

    private final AbstractCamSendingApp.Data testData = new AbstractCamSendingApp.Data();

    private Event lastEvent = null;

    private CEtsi etsiConfiguration;

    private AbstractCamSendingApp<OperatingSystem> app;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private final EventManager eventManagerMock = this::captureEvent;

    @Mock
    public OperatingSystem osMock;

    @Mock
    public AdHocModule adHocModuleMock;

    @Rule
    public TestFileRule testFileRule = new TestFileRule()
            .with("/EtsiApplication.json");

    @Rule
    @InjectMocks
    public SimulationKernelRule simKernelRule = new SimulationKernelRule(eventManagerMock, null, null, null);

    @Before
    public void setup() throws NoSuchFieldException {
        app = spy(new AbstractCamSendingApp<OperatingSystem>() {
            @Override
            public Data generateEtsiData() {
                Data newTestData = new Data();
                newTestData.time = testData.time;
                newTestData.heading = testData.heading;
                newTestData.position = testData.position;
                newTestData.velocity = testData.velocity;
                return newTestData;
            }
        });
        when(osMock.getConfigurationPath()).thenReturn(testFileRule.getRoot());
        when(osMock.getAdHocModule()).thenReturn(adHocModuleMock);
        when(osMock.getEventManager()).thenReturn(eventManagerMock);
        when(osMock.getSimulationTime()).thenAnswer((Answer<Long>) invocation -> lastEvent == null ? 0 : lastEvent.getTime());

        // set RNG manually since Ap
        simKernelRule.setRandomNumberGenerator();
        // LOAD APP
        app.setUp(osMock, new UnitLoggerImpl("veh_0", EtsiApplicationTest.class.getSimpleName()));

        etsiConfiguration = app.getConfiguration();
        assertNotNull(etsiConfiguration);
    }

    @Test
    public void etsiConfigurationCorrectFromFile() {
        assertEquals(2 * DATA.KILOBYTE, etsiConfiguration.minimalPayloadLength);
        assertEquals((long) (7.5 * TIME.SECOND), etsiConfiguration.maxInterval.longValue());
        assertEquals(50 * TIME.MILLI_SECOND, etsiConfiguration.minInterval.longValue());
        assertEquals(7 * TIME.SECOND, etsiConfiguration.maxStartOffset);
    }

    private void captureEvent(Event event) {
        lastEvent = event;
    }

    @Test
    public void adhocModuleIsConfiguredCorrectly() {
        verify(adHocModuleMock, times(1)).enable(argThat(
                argument -> (argument).getNrOfRadios() == 1
                        && (argument).getRadios().get(0).getChannel0() == AdHocChannel.CCH
                        && (argument).getRadios().get(0).getPower() == 50));
    }

    @Test
    public void firstSampleEvent_betweenMinMaxOffset() {
        // ASSERT
        assertNotNull(lastEvent);
        assertTrue(
                lastEvent.getTime() >= etsiConfiguration.minInterval
                        && lastEvent.getTime() <= etsiConfiguration.minInterval + etsiConfiguration.maxStartOffset
        );
    }

    @Test
    public void checkDataAndSendCam_nextSampleTimeEqualsToMinTimeLimit() {
        // SETUP
        long lastEventTime = lastEvent.getTime();

        // RUN
        app.checkDataAndSendCam(lastEvent);

        // ASSERT
        assertNotNull(lastEvent);
        assertEquals(lastEventTime + etsiConfiguration.minInterval, lastEvent.getTime());
    }

    @Test
    public void timeDelta() {
        // SETUP+RUN+ASSERT (first sample)
        testData.time = lastEvent.getTime();
        app.checkDataAndSendCam(lastEvent);
        verify(adHocModuleMock, never()).sendCam();

        // SETUP+RUN+ASSERT (second sample, delta not large enough)
        // just ensuring that we NOT reach the minimum interval
        testData.time = lastEvent.getTime() + etsiConfiguration.minInterval;
        app.checkDataAndSendCam(lastEvent);
        verify(adHocModuleMock, never()).sendCam();

        // SETUP+RUN+ASSERT (fourth sample, delta reached the maximum)
        testData.time = lastEvent.getTime() + etsiConfiguration.maxInterval;
        app.checkDataAndSendCam(lastEvent);
        verify(adHocModuleMock, times(1)).sendCam();
    }

    @Test
    public void positionDelta() {
        // SETUP+RUN+ASSERT (first sample)
        testData.time = lastEvent.getTime();
        testData.position = GeoPoint.latLon(52.511089, 13.320422);
        app.checkDataAndSendCam(lastEvent);
        verify(adHocModuleMock, never()).sendCam();

        // SETUP+RUN+ASSERT (second sample, distance not large enough)
        testData.time = lastEvent.getTime();
        testData.position = GeoPoint.latLon(52.511094, 13.320431);
        app.checkDataAndSendCam(lastEvent);
        verify(adHocModuleMock, never()).sendCam();

        // SETUP+RUN+ASSERT (third sample, distance is large enough)
        testData.time = lastEvent.getTime();
        testData.position = GeoPoint.latLon(52.511135, 13.321000);
        app.checkDataAndSendCam(lastEvent);
        verify(adHocModuleMock, times(1)).sendCam();
    }

    @Test
    public void velocityDelta() {
        // SETUP+RUN+ASSERT (first sample)
        testData.time = lastEvent.getTime();
        testData.velocity = 10.1;
        app.checkDataAndSendCam(lastEvent);
        verify(adHocModuleMock, never()).sendCam();

        // SETUP+RUN+ASSERT (second sample, velocity delta not large enough)
        testData.time = lastEvent.getTime();
        testData.velocity = 9.9;
        app.checkDataAndSendCam(lastEvent);
        verify(adHocModuleMock, never()).sendCam();

        // SETUP+RUN+ASSERT (third sample, velocity delta is large enough)
        testData.time = lastEvent.getTime();
        testData.velocity = 10.7;
        app.checkDataAndSendCam(lastEvent);
        verify(adHocModuleMock, times(1)).sendCam();
    }

    @Test
    public void headingDelta() {
        // SETUP+RUN+ASSERT (first sample)
        testData.time = lastEvent.getTime();
        testData.heading = 179.2;
        app.checkDataAndSendCam(lastEvent);
        verify(adHocModuleMock, never()).sendCam();

        // SETUP+RUN+ASSERT (second sample, heading delta not large enough)
        testData.time = lastEvent.getTime();
        testData.heading = 179.4;
        app.checkDataAndSendCam(lastEvent);
        verify(adHocModuleMock, never()).sendCam();

        // SETUP+RUN+ASSERT (third sample, heading delta is large enough)
        testData.time = lastEvent.getTime();
        testData.heading = 183.3;
        app.checkDataAndSendCam(lastEvent);
        verify(adHocModuleMock, times(1)).sendCam();
    }

    @Test

    public void doNotSampleAfterTearDown() {
        app.checkDataAndSendCam(lastEvent);

        long previousTime = lastEvent.getTime();
        app.checkDataAndSendCam(lastEvent);
        assertTrue(previousTime < lastEvent.getTime());

        previousTime = lastEvent.getTime();
        app.checkDataAndSendCam(lastEvent);
        assertTrue(previousTime < lastEvent.getTime());

        // RUN
        app.tearDown();

        previousTime = lastEvent.getTime();
        app.checkDataAndSendCam(lastEvent);

        // ASSERT: now additional sample should be triggered after tearDown
        assertEquals(previousTime, lastEvent.getTime());
    }


}
