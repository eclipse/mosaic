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

package org.eclipse.mosaic.rti.time;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.rti.MosaicComponentParameters;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.ComponentProvider;
import org.eclipse.mosaic.rti.api.FederateAmbassador;
import org.eclipse.mosaic.rti.api.TimeManagement;
import org.eclipse.mosaic.rti.junit.FederationManagementRule;
import org.eclipse.mosaic.rti.monitor.ActivityLoggingMonitor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;


/**
 * Test which checks {@link SequentialTimeManagement}
 */
public class SequentialTimeManagementTest {

    @Rule
    public FederationManagementRule fedManagement = new FederationManagementRule("ambassador1", "ambassador2");

    private TimeManagement timeManagement;

    @Before
    public void setup() throws Exception {

        ComponentProvider componentProviderMock = mock(ComponentProvider.class);
        Logger logger = mock(Logger.class);
        when(componentProviderMock.getMonitor()).thenReturn(new ActivityLoggingMonitor(logger));
        when(componentProviderMock.getFederationManagement()).thenReturn(fedManagement.getFederationManagementMock());
        this.timeManagement = Mockito.spy(createTimeManagement(componentProviderMock));
        this.timeManagement.startWatchDog("test", 10_000); //in seconds
    }

    protected TimeManagement createTimeManagement(ComponentProvider componentProvider) {
        return new SequentialTimeManagement(componentProvider, new MosaicComponentParameters().setRealTimeBreak(0).setEndTime(20 * TIME.SECOND));
    }

    /**
     * Two ambassadors request various time advance.
     * Simulation is run.
     * It is verified that {@link FederateAmbassador#advanceTime(long)} is called accordingly to the requests.
     */
    @Test
    public void twoAmbassadorsDifferentAdvanceTimeRequests() throws Exception {
        //SETUP
        timeManagement.requestAdvanceTime("ambassador1", 0, 0, (byte) 1);
        timeManagement.requestAdvanceTime("ambassador2", 4 * TIME.SECOND + 132 * TIME.MILLI_SECOND, 0, (byte) 1);
        timeManagement.requestAdvanceTime("ambassador1", 10 * TIME.SECOND, 0, (byte) 1);
        timeManagement.requestAdvanceTime("ambassador2", 10 * TIME.SECOND, 0, (byte) 1);

        //RUN
        timeManagement.runSimulation();

        //ASSERT
        final FederateAmbassador ambassadorMock1 = fedManagement.getAmbassador("ambassador1");
        final FederateAmbassador ambassadorMock2 = fedManagement.getAmbassador("ambassador2");

        verify(ambassadorMock1).initialize(eq(0L), eq(20 * TIME.SECOND));
        verify(ambassadorMock2).initialize(eq(0L), eq(20 * TIME.SECOND));

        verify(ambassadorMock1).advanceTime(eq(0L));
        verify(ambassadorMock2, never()).advanceTime(eq(0L));

        verify(ambassadorMock1, never()).advanceTime(eq(4 * TIME.SECOND + 132 * TIME.MILLI_SECOND));
        verify(ambassadorMock2).advanceTime(eq(4 * TIME.SECOND + 132 * TIME.MILLI_SECOND));

        verify(ambassadorMock1).advanceTime(eq(10 * TIME.SECOND));
        verify(ambassadorMock2).advanceTime(eq(10 * TIME.SECOND));

        verify(ambassadorMock1).finishSimulation();
        verify(ambassadorMock2).finishSimulation();
    }

}
