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

package org.eclipse.mosaic.rti.federation;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.mosaic.rti.MosaicComponentParameters;
import org.eclipse.mosaic.rti.MosaicComponentProvider;
import org.eclipse.mosaic.rti.WatchDogThread;
import org.eclipse.mosaic.rti.api.ComponentProvider;
import org.eclipse.mosaic.rti.api.FederateAmbassador;
import org.eclipse.mosaic.rti.api.FederationManagement;
import org.eclipse.mosaic.rti.api.RtiAmbassador;
import org.eclipse.mosaic.rti.api.federatestarter.JavaFederateExecutor;
import org.eclipse.mosaic.rti.api.parameters.FederateDescriptor;
import org.eclipse.mosaic.rti.api.parameters.FederatePriority;
import org.eclipse.mosaic.rti.api.parameters.JavaFederateParameters;
import org.eclipse.mosaic.rti.config.CLocalHost;
import org.eclipse.mosaic.rti.config.CLocalHost.OperatingSystem;
import org.eclipse.mosaic.rti.junit.TestFederateProcess;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;

public class LocalFederationManagementTest {
    /**
     * This jar is generated automatically by Maven and is placed in target/test-classes.
     * The jar file is executable and will create a file once called.
     */
    private static final String TEST_FEDERATE_JAR = "TestFederate-tests.jar";

    private static final String FEDERATION_ID = "fed";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Process federateProcess;

    private final ComponentProvider componentProvider;
    private final FederationManagement fedManagement;

    public LocalFederationManagementTest() {
        final MosaicComponentParameters simulationParameters = new MosaicComponentParameters()
                .setRealTimeBreak(1)
                .setFederationId("test-federation");

        this.componentProvider = new MosaicComponentProvider(simulationParameters);
        this.fedManagement = new LocalFederationManagement(componentProvider);
    }


    @Before
    public void setup() throws IOException {

        fedManagement.createFederation();

        WatchDogThread watchDogThread = mock(WatchDogThread.class);
        doAnswer((Answer<Void>) invocation -> {
            federateProcess = invocation.getArgument(0);
            return null;
        }).when(watchDogThread).attachProcess(isA(Process.class));
        fedManagement.setWatchdog(watchDogThread);

    }

    private void copyFederateJarToDir(File target) throws IOException {
        File federateJar = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(TEST_FEDERATE_JAR)).getFile());
        FileUtils.forceMkdir(target);
        try (InputStream in = this.getClass().getResourceAsStream("/"+TEST_FEDERATE_JAR)){
            Files.copy(in, new File(target, federateJar.getName()).toPath(), REPLACE_EXISTING);
        }
    }

    @After
    public void teardown() throws Exception {
        fedManagement.stopFederation();
    }

    /**
     * Join federate without deploy or start any process.
     */
    @Test
    public void joinFederateNoDeployNoStart() throws Exception {
        //SETUP
        final FederateAmbassador ambassadorMock = mock(FederateAmbassador.class);
        final FederateDescriptor federateDescriptor = new FederateDescriptor(FEDERATION_ID, ambassadorMock, FederatePriority.DEFAULT);

        //RUN
        fedManagement.addFederate(federateDescriptor);

        //ASSERT
        verify(ambassadorMock).setRtiAmbassador(isA(RtiAmbassador.class));

        verify(ambassadorMock, never()).createFederateExecutor(anyString(), anyInt(), any(OperatingSystem.class));
        verify(ambassadorMock, never()).connectToFederate(anyString(), anyInt());
    }

    /**
     * Start federate via java process and connect to it. Federate won't be deployed.
     */
    @Test
    public void joinFederateNoDeployStartFederate_federateStartedLocally() throws Exception {
        //SETUP
        copyFederateJarToDir(temporaryFolder.newFolder(FEDERATION_ID));

        final FederateAmbassador ambassadorMock = mock(FederateAmbassador.class);
        final FederateDescriptor federateDescriptor = new FederateDescriptor(FEDERATION_ID, ambassadorMock, FederatePriority.DEFAULT);
        federateDescriptor.setStartAndStop(true);
        federateDescriptor.setFederateExecutor(new JavaFederateExecutor(federateDescriptor, TestFederateProcess.class.getCanonicalName(), ""));
        federateDescriptor.setHost(new CLocalHost(temporaryFolder.getRoot().getAbsolutePath()));
        federateDescriptor.setJavaFederateParameters(JavaFederateParameters.defaultParameters());

        //RUN
        fedManagement.addFederate(federateDescriptor);

        //ASSERT
        verify(ambassadorMock, times(1)).connectToFederate(anyString(), isA(InputStream.class), isA(InputStream.class));

        // wait for federate to finish
        assertNotNull(federateProcess);
        federateProcess.waitFor();
        // This file is created if the federate could be started successfully
        assertTrue(new File(temporaryFolder.getRoot(), FEDERATION_ID + File.separator + "federate.test.file").exists());
    }


    /**
     * Start federate via java process and connect to it. Federate will be deployed beforehand.
     */
    @Test
    public void joinFederateDeployAndStartFederate_federateStartedOnDeployedLocation() throws Exception {
        //SETUP
        final File binaryDir = temporaryFolder.newFolder("binaries");
        final File deployDir = temporaryFolder.newFolder("deploy");
        copyFederateJarToDir(binaryDir);

        final FederateAmbassador ambassadorMock = mock(FederateAmbassador.class);
        final FederateDescriptor federateDescriptor = new FederateDescriptor(FEDERATION_ID, ambassadorMock, FederatePriority.DEFAULT);
        federateDescriptor.setStartAndStop(true);
        federateDescriptor.setDeployAndUndeploy(true);
        federateDescriptor.setFederateExecutor(new JavaFederateExecutor(federateDescriptor, TestFederateProcess.class.getCanonicalName(), ""));
        federateDescriptor.setHost(new CLocalHost(deployDir.getAbsolutePath()));
        federateDescriptor.setBinariesDir(binaryDir);
        federateDescriptor.setJavaFederateParameters(JavaFederateParameters.defaultParameters());

        //RUN
        fedManagement.addFederate(federateDescriptor);

        //ASSERT
        verify(ambassadorMock, times(1)).connectToFederate(anyString(), isA(InputStream.class), isA(InputStream.class));

        // wait for federate to finish
        assertNotNull(federateProcess);
        federateProcess.waitFor();
        // This file is created if the federate could be started successfully
        assertTrue(new File(deployDir, FEDERATION_ID + File.separator + "federate.test.file").exists());
    }
}
