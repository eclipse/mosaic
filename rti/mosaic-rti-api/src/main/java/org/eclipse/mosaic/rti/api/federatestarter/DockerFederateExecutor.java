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

package org.eclipse.mosaic.rti.api.federatestarter;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.docker.DockerClient;
import org.eclipse.mosaic.lib.docker.DockerContainer;
import org.eclipse.mosaic.lib.docker.DockerRun;
import org.eclipse.mosaic.rti.api.FederateExecutor;
import org.eclipse.mosaic.rti.config.CLocalHost;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * A federate executor which starts a docker container which contains the federate.
 */
public class DockerFederateExecutor implements FederateExecutor {

    private final String image;
    private final String sharedDirectoryPath;
    private final String imageVolume;
    private String containerName;

    private final Map<String, Object> parameters = new HashMap<>();
    private DockerClient dockerClient;
    private DockerContainer container;

    /**
     * Creates a new DockerFederateExecutor instance.
     *
     * @param image               the name of the docker image to create and start a container from
     * @param sharedDirectoryPath the local sub directory to share with container, relative to the working directory
     * @param imageVolume         the path to the image volume which is bound with the sharedDirectoryPath
     */
    public DockerFederateExecutor(String image, String sharedDirectoryPath, String imageVolume) {
        this.image = image;
        this.containerName = StringUtils.substringBefore(image, ":");
        this.sharedDirectoryPath = sharedDirectoryPath;
        this.imageVolume = imageVolume;
    }

    /**
     * Adds an execution parameter which is passed to the docker container.
     */
    public DockerFederateExecutor addParameter(String key, Object value) {
        parameters.put(key, value);
        return this;
    }

    public DockerFederateExecutor setContainerName(String name) {
        containerName = name;
        return this;
    }

    @Override
    public Process startLocalFederate(File workingDir) {
        this.dockerClient = new DockerClient();

        final DockerRun run = this.dockerClient
                .run(image)
                .name(containerName)
                .removeAfterRun()
                .currentUser()
                .volumeBinding(new File(workingDir, sharedDirectoryPath), imageVolume);

        for (Map.Entry<String, Object> param : parameters.entrySet()) {
            run.parameter(param.getKey(), param.getValue());
        }

        this.container = run.execute();

        return this.container.getAppendedProcess();
    }

    /**
     * Returns an instance to the currently running docker container.
     * Returns <code>null</code> if the container has not been started yet.
     *
     * @return the currently running docker container, or <code>null</code> if no container is running
     */
    @Nullable
    public DockerContainer getRunningContainer() {
        return this.container;
    }

    @Override
    public void stopLocalFederate() {
        if (dockerClient != null) {
            dockerClient.killContainer(this.container, true);
            dockerClient.close();
        }
    }

    @Override
    public int startRemoteFederate(CLocalHost host, PrintStream sshStream, InputStream sshStreamIn) {
        throw new UnsupportedOperationException("Starting docker containers remotely is not supported yet.");
    }

    @Override
    public void stopRemoteFederate(PrintStream sshStreamOut) {
        throw new UnsupportedOperationException("Stopping docker containers remotely is not supported yet.");
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("image", image)
                .append("containerName", containerName)
                .append("imageVolume", imageVolume)
                .append("containerSharePath", sharedDirectoryPath)
                .toString();
    }
}
