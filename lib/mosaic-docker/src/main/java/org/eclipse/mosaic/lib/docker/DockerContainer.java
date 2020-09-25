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

package org.eclipse.mosaic.lib.docker;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Wraps information about a running docker container instance.
 */
public class DockerContainer {

    /**
     * The name of the image this container is based on.
     */
    private final String image;

    /**
     * The name of the container.
     */
    private final String name;

    /**
     * The actual port binding of the running container.
     */
    private final List<Pair<Integer, Integer>> portBindings;

    /**
     * The process which corresponds with the running container.
     */
    private Process process = null;

    DockerContainer(String image, String name, List<Pair<Integer, Integer>> portBindings) {
        this.image = image;
        this.name = name;
        this.portBindings = portBindings;
    }

    void appendProcess(Process process) {
        this.process = process;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public List<Pair<Integer, Integer>> getPortBindings() {
        return portBindings;
    }

    public Process getAppendedProcess() {
        return this.process;
    }
}
