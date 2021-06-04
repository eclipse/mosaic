/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.sumo.bridge.api.complex;

/**
 * Class representing the status of a request sent to TraCI. Each request is
 * responded with a status and specific additional data.
 */
public class Status {

    public static final int STATUS_OK = 0x00;

    public static final int STATUS_ERR = 0xff;
    /**
     * result type of the status.
     */
    private byte resultType;

    /**
     * description of the status in the case of an error.
     */
    private String description;

    /**
     * Constructor using fields.
     *
     * @param resultType  result type of the status
     * @param description description of the status in the case of an error
     */
    public Status(byte resultType, String description) {
        this.resultType = resultType;
        this.description = description;
    }

    /**
     * Getter for description.
     *
     * @return description of the status in the case of an error
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter for result type.
     *
     * @return result type of the status
     */
    public byte getResultType() {
        return resultType;
    }
}
