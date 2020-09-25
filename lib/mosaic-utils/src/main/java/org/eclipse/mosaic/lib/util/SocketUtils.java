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

package org.eclipse.mosaic.lib.util;

import java.io.IOException;
import java.net.ServerSocket;

public final class SocketUtils {

    private SocketUtils() {
        // static methods only
    }

    /**
     * Returns a free port.
     *
     * @throws IllegalStateException if no free port could be found
     */
    public static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            // throw exception later
        }
        throw new IllegalStateException("Could not find a free TCP/IP port.");
    }
}
