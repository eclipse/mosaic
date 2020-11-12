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

package org.eclipse.mosaic.test.app.sendandreceive;

import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.AdHocModuleConfiguration;
import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.test.app.sendandreceive.messages.TestMessage;

import java.nio.charset.StandardCharsets;

public class SendV2xAppAdHoc extends AbstractSenderApp {

    private final AdHocModuleConfiguration adHocModuleConfiguration;
    private final SendMode sendMode;

    private int sequenceNbr = 0;

    @SuppressWarnings("unused") // used in mapping
    public SendV2xAppAdHoc() {
        this(SendMode.topo.toString());
    }

    @SuppressWarnings("unused") // used in mapping
    public SendV2xAppAdHoc(int power) {
        this(SendMode.topo.toString(), power);
    }

    public SendV2xAppAdHoc(String sendMode) {
        super(TIME.SECOND, Long.MAX_VALUE);

        this.adHocModuleConfiguration = null;
        this.sendMode = SendMode.valueOf(sendMode);
    }

    public SendV2xAppAdHoc(String sendMode, int power) {
        this(sendMode, AdHocChannel.CCH.toString(), power);
    }

    public SendV2xAppAdHoc(String sendMode, String channel, int power) {
        super(TIME.SECOND, Long.MAX_VALUE);

        this.adHocModuleConfiguration = new AdHocModuleConfiguration()
                .addRadio().power(power).channel(AdHocChannel.valueOf(channel)).create();
        this.sendMode = SendMode.valueOf(sendMode);
    }

    @Override
    protected void configureCommunication() {
        if (adHocModuleConfiguration == null) {
            getOs().getAdHocModule().enable();
        } else {
            getOs().getAdHocModule().enable(adHocModuleConfiguration);
        }
    }

    @Override
    protected void disableCommunication() {
        getOs().getAdHocModule().disable();
    }

    @Override
    protected void sendMessage() {
        long time = getOs().getSimulationTime();

        final String roadId = getOs().getNavigationModule().getRoadPosition().getEdgeId();
        final byte[] additionalPayload = ("" + sequenceNbr + ";" + roadId).getBytes(StandardCharsets.UTF_8);

        // Send Message to Ernst-Reuter Platz
        final GeoPoint center = GeoPoint.lonLat(13.321974277496338, 52.512547338366325);
        final GeoCircle dest = new GeoCircle(center, 500);

        final String logInfo;
        final MessageRouting routing;
        switch (sendMode) {
            case geobroad:
                logInfo = "geobroadcasting message #" + sequenceNbr;
                routing = getOs().getAdHocModule().createMessageRouting().viaChannel(AdHocChannel.CCH).geoBroadCast(dest);
                break;
            case geouni:
                logInfo = "geounicasting message #" + sequenceNbr;
                routing =
                        getOs().getAdHocModule().createMessageRouting().viaChannel(AdHocChannel.CCH).geoCast(dest, new byte[]{0, 0, 0, 1});
                break;
            case topo:
                logInfo = "topocasting message #" + sequenceNbr;
                routing = getOs().getAdHocModule().createMessageRouting().viaChannel(AdHocChannel.CCH).topoBroadCast();
                break;
            default:
                throw new RuntimeException("Unknown sendMode.");
        }

        getLog().debugSimTime(this, "{} with {} mW on AdHoc channel {}",
                logInfo,
                (adHocModuleConfiguration == null) ? -1 : adHocModuleConfiguration.getRadios().get(0).getPower(),
                (adHocModuleConfiguration == null) ? "null" : adHocModuleConfiguration.getRadios().get(0).getChannel0()
        );

        TestMessage testMessage = new TestMessage(routing, time, getOs().getId(), getOs().getPosition(), additionalPayload);
        getOs().getAdHocModule().sendV2xMessage(testMessage);

        sequenceNbr++;
    }

    enum SendMode {
        geobroad, geouni, topo
    }

}
