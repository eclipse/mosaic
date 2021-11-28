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
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.proto.ZNeedle;


public class AsyncWorker {

    private ZContext ctx = new ZContext();
    private ZMQ.Socket worker;
    private ZFrame identity;
    private String backendAddr;
    private ZFrame content;
    private ZFrame recordedContent;

    public AsyncWorker(String backendAddr, String identity) {
        Socket worker = ctx.createSocket(SocketType.DEALER);
        worker.setIdentity(identity.getBytes(ZMQ.CHARSET));
        worker.connect(backendAddr);
        this.backendAddr = backendAddr;
    }

    public void recv(byte[] data){
        //  The DEALER socket gives us the address envelope and message
        ZMsg msg = ZMsg.recvMsg(worker);
        identity = msg.pop();
        content = msg.pop();
        msg.clear();

        // Reply using the identity of the client
        String contract = identity.getString(ZMQ.CHARSET);
        if (contract.startsWith("req.")){
            // Content is ignored, ZFrame request needs to be filled with data
            ZFrame request = new ZFrame(data);
            identity.sendAndDestroy(worker, ZFrame.MORE);
            request.sendAndDestroy(worker, 0);
        } else if (contract.startsWith("service.")){
            // Content will have some data
            identity.sendAndDestroy(worker, ZFrame.MORE);
            // Record content to get it later
            recordedContent = content;
            ZFrame serviceContent = new ZFrame("1".getBytes());
            serviceContent.sendAndDestroy(worker, 0);
        }
    }

    public void destroy(){
        worker.disconnect(backendAddr);
        worker.close();
        ctx.close();
    }

    public ZFrame getIdentity() {
        return identity;
    }
    public String getContract(){
        return identity.getString(ZMQ.CHARSET);
    }
    public ZFrame getRecordedContentFrame(){
        return recordedContent;
    }
    public byte[] getRecordedContentToBytes() {
        return recordedContent.getData();
    }
    public String getRecordedContentToString(){
        return recordedContent.toString();
    }
}