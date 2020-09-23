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

package org.eclipse.mosaic.starter;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.addressing.IpResolver;
import org.eclipse.mosaic.lib.transform.GeoProjection;
import org.eclipse.mosaic.lib.util.junit.TestUtils;
import org.eclipse.mosaic.rti.api.ComponentProvider;
import org.eclipse.mosaic.rti.api.FederationManagement;
import org.eclipse.mosaic.rti.api.InteractionManagement;
import org.eclipse.mosaic.rti.api.TimeManagement;
import org.eclipse.mosaic.rti.api.parameters.FederateDescriptor;
import org.eclipse.mosaic.rti.api.parameters.InteractionDescriptor;
import org.eclipse.mosaic.rti.config.CHosts;
import org.eclipse.mosaic.rti.config.CIpResolver;
import org.eclipse.mosaic.rti.config.CProjection;
import org.eclipse.mosaic.starter.config.CRuntime;
import org.eclipse.mosaic.starter.config.CScenario;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MosaicSimulationTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final ComponentProvider componentProviderMock = mock(ComponentProvider.class);
    private final FederationManagement federationManagementMock = mock(FederationManagement.class);
    private final TimeManagement timeManagementMock = mock(TimeManagement.class);
    private final InteractionManagement interactionManagementMock = mock(InteractionManagement.class);

    private CRuntime runtimeConfiguration;
    private CHosts hostsConfiguration;

    @Before
    public void setup() {
        hostsConfiguration = new CHosts();
        hostsConfiguration.addDefaultLocalHost();

        runtimeConfiguration = new CRuntime();
        CRuntime.CFederate federate1 = new CRuntime.CFederate();
        federate1.id = "test";
        federate1.start = true;
        federate1.deploy = true;
        federate1.configuration = "test_config.json";
        federate1.classname = TestFederate.class.getCanonicalName();
        federate1.subscriptions.add("interaction1");

        CRuntime.CFederate federate2 = new CRuntime.CFederate();
        federate2.id = "test2";
        federate2.classname = "invalid.class.but.is.not.active.in.CMosaic";

        runtimeConfiguration.federates.add(federate1);
        runtimeConfiguration.federates.add(federate2);

        when(componentProviderMock.getFederationManagement()).thenReturn(federationManagementMock);
        when(componentProviderMock.getTimeManagement()).thenReturn(timeManagementMock);
        when(componentProviderMock.getInteractionManagement()).thenReturn(interactionManagementMock);
    }

    @After
    public void tearDown() throws NoSuchFieldException {
        TestUtils.setPrivateField(GeoProjection.class, "instance", null);
        TestUtils.setPrivateField(IpResolver.class, "singleton", null);
    }

    @Test
    public void successfulSimulation() throws Exception {
        // SETUP
        Path scenario = temporaryFolder.newFolder("scenario").toPath();
        MosaicSimulation simulation = createValidSimulation();
        CScenario scenarioConfiguration = createValidConfiguration();

        // RUN
        MosaicSimulation.SimulationResult result =
                simulation.runSimulation(scenario, scenarioConfiguration);

        // VERIFY
        if (!result.success) {
            result.exception.printStackTrace();
            fail(result.exception.getMessage());
        }

        // assert calls of basic start/stop
        verify(timeManagementMock).runSimulation();
        verify(federationManagementMock).stopFederation();

        // assert watchdog initialization
        verify(timeManagementMock).startWatchDog(eq("MyScenario"), eq(20));
        verify(timeManagementMock).startExternalWatchDog(eq("MyScenario"), eq(1337));

        // assert interaction subscription
        ArgumentCaptor<List<InteractionDescriptor>> interactions = ArgumentCaptor.forClass(List.class);
        verify(interactionManagementMock).subscribeInteractions(eq("test"), interactions.capture());
        assertEquals(1, interactions.getValue().size());
        assertEquals("interaction1", interactions.getValue().stream().findFirst().get().interactionId);

        // assert initialized singletonss
        assertNotNull(GeoProjection.getInstance());
        assertNotNull(IpResolver.getSingleton());

        // assert initialized ambassador
        ArgumentCaptor<FederateDescriptor> descriptorCaptor = ArgumentCaptor.forClass(FederateDescriptor.class);
        verify(federationManagementMock).addFederate(descriptorCaptor.capture());

        FederateDescriptor descriptor = descriptorCaptor.getValue();
        assertNotNull(descriptor);
        assertEquals("test", descriptor.getId());
        assertNotNull(descriptor.getConfigDir());
        assertEquals(scenario.resolve("test"), descriptor.getConfigDir().toPath());
        assertNotNull(descriptor.getAmbassador());
        assertTrue(descriptor.getAmbassador() instanceof TestFederate);
    }

    @Test
    public void failSimulation_noEndTimeGiven() throws Exception {
        // SETUP
        Path scenario = temporaryFolder.newFolder("scenario").toPath();
        MosaicSimulation simulation = createValidSimulation();
        CScenario scenarioConfiguration = createValidConfiguration();
        scenarioConfiguration.simulation.duration = 0;

        // RUN
        MosaicSimulation.SimulationResult result =
                simulation.runSimulation(scenario, scenarioConfiguration);

        assertFalse(result.success);
        assertNotNull(result.exception);
        assertTrue(result.exception instanceof IllegalArgumentException);
    }

    @Test
    public void failSimulation_noTransformationConfigGiven() throws Exception {
        // SETUP
        Path scenario = temporaryFolder.newFolder("scenario").toPath();
        MosaicSimulation simulation = createValidSimulation();
        CScenario scenarioConfiguration = createValidConfiguration();
        scenarioConfiguration.simulation.projectionConfig = null;

        // RUN
        MosaicSimulation.SimulationResult result =
                simulation.runSimulation(scenario, scenarioConfiguration);

        assertFalse(result.success);
        assertNotNull(result.exception);
        assertTrue(result.exception instanceof IllegalArgumentException);
    }

    private MosaicSimulation createValidSimulation() {
        return new MosaicSimulation()
                .setRuntimeConfiguration(runtimeConfiguration)
                .setHostsConfiguration(hostsConfiguration)
                .setExternalWatchdogPort(1337)
                .setWatchdogInterval(20)
                .setLogLevelOverride("DEBUG")
                .setLogbackConfigurationFile(Paths.get("logback-test.xml"))
                .setComponentProviderFactory(((simulationParams) -> componentProviderMock));
    }

    private CScenario createValidConfiguration() {
        CScenario scenarioConfiguration = new CScenario();
        scenarioConfiguration.simulation = new CScenario.Simulation();
        scenarioConfiguration.simulation.id = "MyScenario";
        scenarioConfiguration.simulation.duration = 100;
        scenarioConfiguration.simulation.projectionConfig = new CProjection();
        scenarioConfiguration.simulation.projectionConfig.centerCoordinates = GeoPoint.latLon(52.63,13.56);
        scenarioConfiguration.simulation.projectionConfig.cartesianOffset = CartesianPoint.xy(-395635.35,-5826456.24);
        scenarioConfiguration.simulation.networkConfig = new CIpResolver();
        scenarioConfiguration.federates.put("test", true);
        scenarioConfiguration.federates.put("mapping", false);
        return scenarioConfiguration;
    }

}