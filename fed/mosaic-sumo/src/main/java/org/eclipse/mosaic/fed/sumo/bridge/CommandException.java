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

package org.eclipse.mosaic.fed.sumo.bridge;

import org.eclipse.mosaic.fed.sumo.bridge.api.complex.Status;

public class CommandException extends Exception {

    private final Status status;

    public CommandException(String errorMessage) {
        this(errorMessage, new Status(Status.STATUS_ERR, errorMessage));
    }

    public CommandException(Status status) {
        this(status.getDescription(), status);
    }

    public CommandException(String errorMessage, Status status) {
        super(errorMessage);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
