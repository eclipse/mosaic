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

package org.eclipse.mosaic.rti;

import org.eclipse.mosaic.rti.api.ComponentProvider;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This watchdog thread, once started, can be accessed remotely to close the simulation.
 * To kill the simulation, a socket connection to this thread has to be created using the
 * provided port number. If the integer number {@link ExternalWatchDog#CLOSE_STATUS} (= 3) is
 * sent to the socket, the simulation is closed.
 */
public class ExternalWatchDog extends Thread {

    private final static int CLOSE_STATUS = 3;

    private final ComponentProvider federation;
    private final ServerSocket serverSocket;

    /**
     * Creates an Watchdog thread which can be access via the given port
     * to close the simulation remotely.
     *
     * @throws IllegalArgumentException if the port number is already in use
     */
    public ExternalWatchDog(ComponentProvider federation, int port) {
        this.federation = federation;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @SuppressWarnings(value = "DM_EXIT", justification = "That's the purpose of the Watchdog")
    @Override
    public void run() {
        try {
            final Socket socket = serverSocket.accept();
            final DataInputStream in = new DataInputStream(socket.getInputStream());
            int status = in.readInt();
            if (status == CLOSE_STATUS) {
                try {
                    federation.getFederationManagement().stopFederation();
                } catch (Exception e) {
                    System.err.format("Could not stop federation. Stacktrace: %n%s%n", ExceptionUtils.getStackTrace(e));
                }
                System.out.println("External Watchdog kills the Simulation");
                System.exit(333);
            }
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }
}
