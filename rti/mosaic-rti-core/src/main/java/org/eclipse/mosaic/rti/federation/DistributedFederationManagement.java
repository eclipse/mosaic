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

package org.eclipse.mosaic.rti.federation;

import org.eclipse.mosaic.rti.api.ComponentProvider;
import org.eclipse.mosaic.rti.api.FederationManagement;
import org.eclipse.mosaic.rti.api.parameters.FederateDescriptor;
import org.eclipse.mosaic.rti.config.CLocalHost;
import org.eclipse.mosaic.rti.config.CRemoteHost;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

/**
 * This extension of <code>LocalFederationManagement</code> allows additionally
 * to the local administration an administration on remote hosts using SSH.
 *
 * @see FederationManagement
 */
public class DistributedFederationManagement extends LocalFederationManagement {

    private static final String THIS = ".";
    private static final String PARENT = "..";

    /**
     * SSH client used to connect to remote hosts.
     */
    private final JSch sshClient = new JSch();

    /**
     * Mapping between hosts and SSH sessions.
     */
    private final HashMap<CLocalHost, Session> hostSessionMapping = new HashMap<>();

    /**
     * Constructor with string identifying a federation as parameter.
     *
     * @param federation federation
     */
    public DistributedFederationManagement(ComponentProvider federation) {
        super(federation);
    }

    @Override
    public void stopFederation() throws Exception {

        super.stopFederation();

        // close all open ssh sessions
        for (Session session : this.hostSessionMapping.values()) {
            session.disconnect();
        }
    }

    /**
     * Deploys a federate represented by its handle on a remote machine using
     * SSH.
     *
     * @param descriptor federate descriptor consisting all necessary data to deploy a federate
     * @throws Exception if the federate could not be deployed
     */
    @Override
    protected void deployFederate(FederateDescriptor descriptor) throws Exception {

        CLocalHost host = descriptor.getHost();
        if (!(host instanceof CRemoteHost)) {
            super.deployFederate(descriptor);
            return;
        }

        this.log.info("Connect to " + host.address + " and create channel ...");
        Session session = this.connect((CRemoteHost) host);

        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        this.log.info("Connected.");

        this.log.info("Transfer directories and files.");
        channel.cd(host.workingDirectory);
        try {
            channel.mkdir(descriptor.getId());
        } catch (SftpException e) {
            log.warn("Removing existing run-Directory: " + descriptor.getId());
            removeDirectory(channel, descriptor.getId());
            channel.mkdir(descriptor.getId());
        }
        channel.cd(descriptor.getId());

        if (descriptor.getBinariesDir().isDirectory()) {
            this.copyDirectory(descriptor.getBinariesDir(), channel);
        }

        if (descriptor.getConfigDir() != null) {
            this.copyDirectory(descriptor.getConfigDir(), channel);
        }

        this.log.info("Transferred.");

        this.log.info("Close channel ...");
        channel.disconnect();
        this.log.info("Closed.");
    }

    /**
     * Starts a federate represented by its handle on a remote machine using
     * SSH.
     *
     * @param handle federate handle consisting all necessary data to start a
     *               federate
     * @throws Exception if the federate could not be started
     */
    @Override
    protected void startFederate(FederateDescriptor handle) throws Exception {
        PrintStream out = null;

        try {
            CLocalHost host = handle.getHost();

            if (!(host instanceof CRemoteHost)) {
                super.startFederate(handle);
                return;
            }

            this.log.info("Connect to " + host.address
                    + " and create channel ...");
            Session session = this.hostSessionMapping.get(host);
            if (session == null) {
                session = this.connect((CRemoteHost) host);
                this.hostSessionMapping.put(host, session);
            }
            ChannelShell channel = (ChannelShell) session.openChannel("shell");

            PipedOutputStream sshOut = new PipedOutputStream();
            channel.setOutputStream(sshOut);
            InputStream in = new PipedInputStream(sshOut);

            PipedInputStream sshIn = new PipedInputStream();
            channel.setInputStream(sshIn);
            out = new PrintStream(new PipedOutputStream(sshIn), false, "UTF8");

            channel.connect();
            this.log.info("Connected.");

            this.log.info("Start federate ... ");
            out.println("cd " + host.workingDirectory);
            out.println("cd " + handle.getId());

            int processId = handle.getFederateExecutor().startRemoteFederate(host, out, in);

            if (processId >= 0) {
                handle.getAmbassador().connectToFederate(host.address, in, null);

                this.log.info("Close channel ...");
                channel.disconnect();
                this.log.info("Closed.");
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Stops a federate represented by its handle on a remote machine using SSH.
     *
     * @param handle federate handle consisting all necessary data to stop a
     *               federate
     * @throws Exception if the federate could not be stopped
     */
    @SuppressWarnings("unused")
    protected void stopFederate(FederateDescriptor handle) throws Exception {

        PrintStream out = null;
        try {
            CLocalHost host = handle.getHost();
            if (!(host instanceof CRemoteHost)) {
                super.stopFederate(handle, true);
                return;
            }

            Session session = this.hostSessionMapping.get(host);
            ChannelShell channel = (ChannelShell) session.openChannel("shell");

            PipedInputStream sshIn = new PipedInputStream();
            channel.setInputStream(sshIn);
            out = new PrintStream(new PipedOutputStream(sshIn), false, "UTF-8");

            channel.connect();
            this.log.info("Connected.");

            this.log.info("Stop federate ... ");
            handle.getFederateExecutor().stopRemoteFederate(out);
            this.log.info("Stopped.");

            this.log.info("Close channel ...");
            channel.disconnect();
            this.log.info("Closed.");
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Undeploys a federate represented by its handle on a remote machine using
     * SSH.
     *
     * @param handle federate handle consisting all necessary data to undeploy a federate
     * @throws Exception if the federate could not be undeployed
     */
    @Override
    protected void undeployFederate(FederateDescriptor handle) throws Exception {
        CLocalHost host = handle.getHost();
        if (!(host instanceof CRemoteHost)) {
            super.undeployFederate(handle);
            return;
        }

        this.log.info("Connect to " + host.address + " ...");
        Session session = this.hostSessionMapping.get(host);
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        this.log.info("Connected.");

        this.log.info("Remove all deployed files ...");
        channel.cd(host.workingDirectory);
        this.removeDirectory(channel, handle.getId());
        this.log.info("Finished.");

        this.log.info("Close channel ...");
        channel.disconnect();
        this.log.info("Closed.");
    }

    /**
     * Copies a given local directory as sub directory into a given destination
     * directory on a remote host using SFTP.
     *
     * @param srcDir     directory that is to be copied
     * @param dstChannel channel to the remote host
     */
    private void copyDirectory(File srcDir, ChannelSftp dstChannel) throws SftpException, FileNotFoundException {
        File[] files = srcDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    dstChannel.mkdir(file.getName());
                    dstChannel.cd(file.getName());
                    copyDirectory(file, dstChannel);
                    dstChannel.cd(PARENT);
                } else {
                    dstChannel.put(new FileInputStream(file), file.getName());
                }
            }
        }
    }

    /**
     * Removes a given directory if it exists on a remote host.
     *
     * @param channel channel to the remote host
     * @param dirName directory that is to be removed
     */
    private void removeDirectory(ChannelSftp channel, String dirName) throws SftpException {
        Vector<LsEntry> entries = channel.ls(dirName);
        channel.cd(dirName);
        String file;
        for (LsEntry entry : entries) {
            file = entry.getFilename().trim();
            if (entry.getAttrs().isDir()) {
                if (!file.equals(PARENT) && !file.equals(THIS)) {
                    this.removeDirectory(channel, file);
                }
            } else {
                channel.rm(file);
            }
        }
        channel.cd(PARENT);
        channel.rmdir(dirName);
    }

    /**
     * Checks whether there is already an SSH session to the given host or not.
     * If not then a new session is created else the existing one is returned.
     *
     * @param params params including necessary values to create an SSH connection
     * @return an SSH session to the given host.
     * @throws JSchException if the SSH session could not be established
     */
    private Session connect(CRemoteHost params) throws JSchException {
        Session session = this.hostSessionMapping.get(params);
        if (session == null) {
            session = this.sshClient.getSession(
                    params.user,
                    params.address,
                    params.port
            );
            session.setPassword(params.password);
            session.setConfig(SSH_PROPS);
            session.connect(30000);

            this.hostSessionMapping.put(params, session);
        } else {
            if (!session.isConnected()) {
                session.connect(30000);
            }
        }
        return session;
    }

    private static Properties SSH_PROPS = new Properties();

    static {
        SSH_PROPS.setProperty("StrictHostKeyChecking", "no");
        SSH_PROPS.setProperty("PreferredAuthentications", "password,publickey");
    }
}
