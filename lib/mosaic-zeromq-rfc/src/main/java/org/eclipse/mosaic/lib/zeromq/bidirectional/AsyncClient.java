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

import java.util.Formatter;

import org.zeromq.*;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

public class AsyncClient {
    private ZContext ctx = new ZContext();
    private ZMQ.Socket client;

    private Poller poller;
    private String frontendAddr;
    private Formatter log = new Formatter(System.out);
    private ZMsg request;

    public AsyncClient(String frontendAddr, String identity) {
        Socket client = ctx.createSocket(SocketType.DEALER);
        client.setIdentity(identity.getBytes(ZMQ.CHARSET));
        client.connect(frontendAddr);
        Poller poller = ctx.createPoller(1);
        poller.register(client, Poller.POLLIN);
    }

    public ZMsg recv(ZMsg reply){
        //  The DEALER socket gives us the address envelope and message
        poller.poll(1);
        if (poller.pollin(0)) {
            request = ZMsg.recvMsg(client);
        } 
        reply.send(client);
        return request;
    }

    public void destroy(){
        client.disconnect(frontendAddr);
        client.close();
        ctx.close();
    }
}
