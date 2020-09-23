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

package org.eclipse.mosaic.fed.sumo.traci;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Interface of a TraCI connection.
 */
public interface TraciConnection {

    /**
     * Getter for the input stream.
     *
     * @return the input stream from the server for reading.
     */
    DataInputStream getIn();

    /**
     * Getter for the output stream.
     *
     * @return the output stream towards the server for writing.
     */
    DataOutputStream getOut();

    /**
     * Getter for the currently running SUMO version.
     *
     * @return the SUMO Version of the TraCI server.
     */
    SumoVersion getCurrentVersion();

    /**
     * Getter for the command register.
     *
     * @return the register where command instances are stored.
     */
    CommandRegister getCommandRegister();

    /**
     * Call this method if the traci-connection should be closed immediately.
     */
    void emergencyExit(Throwable e);
}
