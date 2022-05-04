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

package org.eclipse.mosaic.fed.application.ambassador.simulation.communication;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.ambassador.simulation.AbstractSimulationUnit;
import org.eclipse.mosaic.fed.application.app.api.communication.CommunicationModule;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.objects.addressing.IpResolver;
import org.eclipse.mosaic.lib.objects.addressing.NetworkAddress;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.objects.v2x.etsi.Cam;
import org.eclipse.mosaic.rti.TIME;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

import java.net.Inet4Address;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generic {@link AbstractCommunicationModule} for a {@link AbstractSimulationUnit}.
 * (Can be either a {@link AdHocModule} or a {@link CellModule})
 */
@SuppressWarnings("checkstyle:ClassTypeParameterName")
public abstract class AbstractCommunicationModule<ConfigT extends AbstractCommunicationModuleConfiguration>
        implements CommunicationModule<ConfigT> {

    public static final long CAM_DEFAULT_MINIMAL_PAYLOAD_LENGTH = 200L;

    private final AtomicInteger sequenceNumberGenerator;

    protected ConfigT configuration;

    /**
     * Actual node, which has this communication module.
     * (Can be any kind of {@link AbstractSimulationUnit})
     */
    protected final CommunicationModuleOwner owner;

    /**
     * A logging facility inherited from the creator of this module.
     */
    protected final Logger log;

    /**
     * IP information (address and subnet mask) for this particular node.
     */
    protected final Inet4Address address;
    protected final Inet4Address subnet;

    protected AbstractCommunicationModule(CommunicationModuleOwner owner, Logger log) {
        this.log = log;
        this.owner = owner;
        // Get an address at creation time
        address = IpResolver.getSingleton().registerHost(owner.getId());
        subnet = IpResolver.getSingleton().getNetMask();
        sequenceNumberGenerator = new AtomicInteger();
    }

    protected AbstractCommunicationModule(CommunicationModuleOwner owner, AtomicInteger idGenerator, Logger log) {
        this.log = log;
        this.owner = owner;
        // Get an address at creation time
        address = IpResolver.getSingleton().registerHost(owner.getId());
        subnet = IpResolver.getSingleton().getNetMask();
        this.sequenceNumberGenerator = idGenerator;
    }

    public CommunicationModuleOwner getOwner() {
        return owner;
    }

    /**
     * Returns solely the IP address of the current node.
     *
     * @return SourceAddress
     */
    public NetworkAddress getSourceAddress() {
        return new NetworkAddress(address);
    }

    @Override
    public void sendV2xMessage(final V2xMessage msg) {

        // put the message in the cache
        SimulationKernel.SimulationKernel.getV2xMessageCache().putItem(
                SimulationKernel.SimulationKernel.getCurrentSimulationTime(), msg
        );
        // set the sequence number for the V2XMessage. The sequence numbers are
        // unique per SimulationUnit that generates the CommunicationModule object.
        try {
            msg.setSequenceNumber(sequenceNumberGenerator.incrementAndGet());
        } catch (IllegalStateException e) {
            log.error(e.getMessage());
            throw e;
        }
        V2xMessageTransmission v2xMessageTransmission = new V2xMessageTransmission(
                SimulationKernel.SimulationKernel.getCurrentSimulationTime(),
                msg
        );
        owner.sendInteractionToRti(v2xMessageTransmission);
        owner.triggerOnSendMessage(v2xMessageTransmission);
    }

    @Override
    public void enable(ConfigT configuration) {
        if (configuration == null) {
            log.warn("Configuration provided to enable CommunicationModule is null");
            return;
        }
        if (this.configuration != null) {
            log.info("This communication module has already been activated.");
            if (configuration.camMinimalPayloadLength == null) {
                // re-use if already set by another application on the same unit
                configuration.camMinimalPayloadLength = this.configuration.camMinimalPayloadLength;
            }
        }
        this.configuration = configuration;
    }

    @Override
    public void disable() {
        this.configuration = null;
    }

    /**
     * Finalize the process of sending a CAM, with already filled routing information.
     *
     * @param routing routing information compound
     * @return message id of the CAM
     */
    protected Integer sendCam(MessageRouting routing) {
        final CamBuilder camBuilder = owner.assembleCamMessage(new CamBuilder());
        final Cam cam = new Cam(routing,
                camBuilder.create(owner.getSimulationTime(), owner.getId()),
                ObjectUtils.defaultIfNull(configuration.camMinimalPayloadLength, CAM_DEFAULT_MINIMAL_PAYLOAD_LENGTH)
        );

        if (log.isDebugEnabled()) {
            log.debug("Sending CAM from {} with time {}", owner.getId(),
                    TIME.format(SimulationKernel.SimulationKernel.getCurrentSimulationTime()));
        }
        sendV2xMessage(cam);
        return cam.getId();
    }
}
