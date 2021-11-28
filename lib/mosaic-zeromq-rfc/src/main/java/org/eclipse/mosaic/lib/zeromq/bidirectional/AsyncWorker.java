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

    public AsyncWorker(String backendAddr, String contract) {
        Socket worker = ctx.createSocket(SocketType.DEALER);
        worker.setIdentity(contract.getBytes(ZMQ.CHARSET));
        worker.connect(backendAddr);
        this.backendAddr = backendAddr;

        this.worker = worker;
    }

    public ZMsg recvAndSend(String data){
        //  The DEALER socket gives us the address envelope and message
        ZMsg msg = ZMsg.recvMsg(worker, ZMQ.DONTWAIT);
        
        if (msg == null)
            return null;

        identity = msg.pop();
        content = msg.pop();
        msg.clear();

        // Reply using the identity of the client
        String contract = identity.getString(ZMQ.CHARSET);
        ZFrame reply = new ZFrame(data);
        if (contract.startsWith("req.")){
            // Content is ignored, ZFrame request needs to be filled with data
            identity.send(worker, ZFrame.MORE);
            reply.send(worker, 0);
            return createMsg(identity, reply);
        } else if (contract.startsWith("service.")){
            // Content will have some data
            identity.send(worker, ZFrame.MORE);
            reply = new ZFrame("1".getBytes());
            reply.send(worker, 0);
            return createMsg(new ZFrame(data));
        } else{
            return null;
        }
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