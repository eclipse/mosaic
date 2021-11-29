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

package org.eclipse.mosaic.lib.zeromq.bidirectional;

import org.zeromq.*;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

public class AsyncBroker implements Runnable {

    private ZContext ctx = new ZContext();
    Socket frontend, backend;

    public AsyncBroker(String frontendAddr, String backendAddr){
        //  Frontend socket talks to clients over TCP
        this.frontend = ctx.createSocket(SocketType.ROUTER);
        this.frontend.bind(frontendAddr);

        //  Backend socket talks to workers over TCP
        this.backend = ctx.createSocket(SocketType.ROUTER);
        this.backend.bind(backendAddr);
    }
    
    @Override
    public void run()
    {
        ZMQ.proxy(this.frontend, this.backend, null);
    }
}
