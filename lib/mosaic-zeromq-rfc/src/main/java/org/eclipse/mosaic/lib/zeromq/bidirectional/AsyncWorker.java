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

import java.util.Random;

import org.zeromq.*;
import org.zeromq.ZMQ.Socket;


public class AsyncWorker {

    private ZContext ctx = new ZContext();
    private ZMQ.Socket worker;
    private ZFrame identity;
    private String backendAddr;
    private ZFrame content;
    private ZFrame recordedContent;

    public AsyncWorker(String backendAddr, String contract) {
        Socket worker = ctx.createSocket(SocketType.DEALER);
        worker.setIdentity(contract.getBytes(ZMQ.CHARSET));
        worker.connect(backendAddr);
        this.backendAddr = backendAddr;

        this.worker = worker;
    }

    public ZMsg recvOnce(){
        ZMsg msg = ZMsg.recvMsg(worker, ZMQ.DONTWAIT);
        if (msg == null)
            return null;
        return msg;
    }

    public ZMsg sendOnce(ZFrame sendToIdentity, String data){
        
        ZFrame reply = new ZFrame(data);

        sendToIdentity.send(worker, ZFrame.MORE);
        reply.send(worker, 0);

        return createMsg(sendToIdentity, reply);
    }

    public ZMsg recvAndSend(String data){
        //  The DEALER socket gives us the address envelope and message
        ZMsg msg = ZMsg.recvMsg(worker, ZMQ.DONTWAIT);
        
        if (msg == null)
            return null;

        ZFrame identity = msg.pop();
        ZFrame content = msg.pop();
        msg.destroy();

        // Content is ignored, ZFrame request needs to be filled with data
        identity.send(worker, ZFrame.MORE);
        content.reset(data);
        content.send(worker, 0);
        return createMsg(identity, content);
    }

    private ZMsg createMsg(ZFrame... frames){
        ZMsg msg = new ZMsg();
        for (ZFrame frame : frames){
            msg.add(frame);
            frame.destroy();
        }
        return msg;
    }

    public void destroy(){
        worker.disconnect(backendAddr);
        worker.close();
        ctx.close();
    }
}