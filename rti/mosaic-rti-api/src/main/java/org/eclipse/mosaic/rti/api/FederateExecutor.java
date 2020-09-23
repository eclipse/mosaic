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

package org.eclipse.mosaic.rti.api;

import org.eclipse.mosaic.rti.config.CLocalHost;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * <p>The {@link FederateExecutor} is used to start the federate the ambassador
 * is associated with. The {@link FederateExecutor} should be able to start the
 * federate and close it locally, as well as remotely.</p>
 */
public interface FederateExecutor {

    /**
     * Starts the federate locally in the given working dir.
     *
     * @param workingDir the working directory
     *
     * @return the local process of the federate
     * @throws FederateStarterException if something went wrong during starting the federate
     */
    Process startLocalFederate(File workingDir) throws FederateStarterException;

    /**
     * Stops the previously locally started federate.
     *
     * @throws FederateStarterException if the start of the federate failed
     */
    void stopLocalFederate() throws FederateStarterException;

    /**
     * Starts the federate locally in the given working dir.
     *
     * @param host information about the remote host the federate is started on
     * @param sshStreamOut the ssh output stream to send commands to the host
     * @param sshStreamIn the ssh input stream to read the console output of the remote host shell
     *
     * @return the local process of the federate
     * @throws FederateStarterException if something went wrong during starting the federate
     */
    int startRemoteFederate(CLocalHost host, PrintStream sshStreamOut, InputStream sshStreamIn) throws FederateStarterException;

    /**
     * Stops the previously remotely started federate.
     *
     * @param sshStreamOut the ssh output stream to send commands to the host
     * @throws FederateStarterException if something went wrong during starting the federate
     */
    void stopRemoteFederate(PrintStream sshStreamOut) throws FederateStarterException;

    class FederateStarterException extends Exception {

        private static final long serialVersionUID = 1L;

        public FederateStarterException(Throwable cause) {
            super(cause);
        }

        public FederateStarterException(String message) {
            super(message);
        }
    }
}
