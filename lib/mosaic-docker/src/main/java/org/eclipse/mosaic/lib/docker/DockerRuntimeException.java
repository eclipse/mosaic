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

package org.eclipse.mosaic.lib.docker;

/**
 * Extension of {@link RuntimeException}, thrown for example if connection to docker process is interrupted.
 */
public class DockerRuntimeException extends RuntimeException {

    public DockerRuntimeException(String message) {
        super(message);
    }

    public DockerRuntimeException(Throwable cause) {
        super(cause);
    }

    public DockerRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
