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

package org.eclipse.mosaic.rti.federation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.mosaic.lib.util.SocketUtils;
import org.eclipse.mosaic.rti.MosaicComponentParameters;
import org.eclipse.mosaic.rti.MosaicComponentProvider;
import org.eclipse.mosaic.rti.api.ComponentProvider;
import org.eclipse.mosaic.rti.api.FederateAmbassador;
import org.eclipse.mosaic.rti.api.FederationManagement;
import org.eclipse.mosaic.rti.api.federatestarter.ExecutableFederateExecutor;
import org.eclipse.mosaic.rti.api.parameters.FederateDescriptor;
import org.eclipse.mosaic.rti.api.parameters.FederatePriority;
import org.eclipse.mosaic.rti.config.CRemoteHost;
import org.eclipse.mosaic.rti.junit.DockerCheckRule;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Tests the distributed federation management using a ssh-server which runs inside
 * a docker container. This does only work if docker is installed on the
 * machine this test is running on.
 */
@Ignore // FIXME
public class DistributedFederationManagementTest {


    private static int freePort = SocketUtils.findFreePort();

    private static File binaryDir;

    @ClassRule
    public static DockerCheckRule dockerCheckRule = new DockerCheckRule();

    @ClassRule
    public static TemporaryFolder tempFolder = new TemporaryFolder();
    private boolean stopped;

    @BeforeClass
    public static void runSshServerInDockerContainer() throws IOException, InterruptedException {
        try (InputStream stream = DistributedFederationManagementTest.class.getClassLoader().getResourceAsStream("ssh-server-docker/Dockerfile"); FileOutputStream target = new FileOutputStream(tempFolder.newFile("Dockerfile"))) {
            IOUtils.copy(stream, target);
        }
        Runtime.getRuntime().exec(new String[]{
                "docker", "build", "-t", "ssh-server", "."
        }, null, tempFolder.getRoot()).waitFor();

        Runtime.getRuntime().exec(new String[]{
                "docker", "rm", "-f", "ssh-server"
        }, null, tempFolder.getRoot()).waitFor();

        Runtime.getRuntime().exec(new String[]{
                "docker", "run", "-d", "-p", freePort + ":22", "-v", tempFolder.newFolder("share").getAbsolutePath().replace("\\", "/") + ":/home", "--name", "ssh-server", "ssh-server"
        }, null, tempFolder.getRoot()).waitFor();
    }

    @BeforeClass
    public static void createBinaryDir() throws IOException {
        binaryDir = tempFolder.newFolder("binaries");
        FileUtils.touch(new File(binaryDir, "testfile.deploy"));
    }

    @AfterClass
    public static void removeDockerContainer() throws IOException, InterruptedException {
        Runtime.getRuntime().exec(new String[]{
                "docker", "rm", "-f", "ssh-server"
        }, null, tempFolder.getRoot()).waitFor();
    }

    private final ComponentProvider componentProvider;
    private final FederationManagement fedManagement;

    public DistributedFederationManagementTest() {
        final MosaicComponentParameters simulationParameters = new MosaicComponentParameters()
                .setRealTimeBreak(1)
                .setFederationId("testFederation");

        this.componentProvider = new MosaicComponentProvider(simulationParameters);
        this.fedManagement = new DistributedFederationManagement(componentProvider);
    }

    @Before
    public void setup() throws IOException {
        fedManagement.createFederation();
    }

    @After
    public void teardown() throws Exception {
        if (!stopped) {
            fedManagement.stopFederation();
        }
    }

    @Test
    public void joinFederateDeployAndStartFederate_federateStartedAndDeployedRemotely() throws Exception {
        //SETUP
        String FEDERATION_ID = "fed";
        final FederateAmbassador ambassadorMock = mock(FederateAmbassador.class);
        final FederateDescriptor federateDescriptor = new FederateDescriptor(FEDERATION_ID, ambassadorMock, FederatePriority.DEFAULT);
        federateDescriptor.setStartAndStop(true);
        federateDescriptor.setDeployAndUndeploy(true);
        federateDescriptor.setFederateExecutor(new ExecutableFederateExecutor(federateDescriptor, "touch", "testfile.created.by.federate"));
        federateDescriptor.setHost(new CRemoteHost("localhost", freePort, "root", "screencast", "/home"));
        federateDescriptor.setBinariesDir(binaryDir);

        //RUN
        fedManagement.addFederate(federateDescriptor);

        //ASSERT

        // This file is created if the binary dir has been deployed successfully
        assertTrue(new File(tempFolder.getRoot(), "share" + File.separator + FEDERATION_ID + File.separator + "testfile.deploy").exists());

        //wait until file is created by federate, but not longer than 20 sec
        long start = System.currentTimeMillis();
        boolean fileFound = false;
        while (!fileFound && (System.currentTimeMillis() - start) < 20000) {
            Thread.sleep(100);
            fileFound = new File(tempFolder.getRoot(), "share" + File.separator + FEDERATION_ID + File.separator + "testfile.created.by.federate").exists();
        }
        assertTrue(fileFound);

        //RUN stop
        fedManagement.stopFederation();
        stopped = true;

        assertFalse(new File(tempFolder.getRoot(), "share" + File.separator + FEDERATION_ID + File.separator + "testfile.deploy").exists());
        assertFalse(new File(tempFolder.getRoot(), "share" + File.separator + FEDERATION_ID + File.separator + "testfile.created.by.federate").exists());
    }

}