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
import org.eclipse.mosaic.rti.TIME;

public class SendCamAppAdHoc extends AbstractSenderApp {

    private final AdHocModuleConfiguration adHocModuleConfiguration;

    @SuppressWarnings("unused") // used by mapping
    public SendCamAppAdHoc() {
        super(TIME.SECOND, Long.MAX_VALUE);
        adHocModuleConfiguration = null;
    }

    @SuppressWarnings("unused") // used by mapping
    public SendCamAppAdHoc(int power) {
        super(TIME.SECOND, Long.MAX_VALUE);

        this.adHocModuleConfiguration = new AdHocModuleConfiguration()
                .addRadio().power(power).create();
    }

    public SendCamAppAdHoc(int power, double distance) {
        super(TIME.SECOND, Long.MAX_VALUE);

        this.adHocModuleConfiguration = new AdHocModuleConfiguration()
                .addRadio().power(power).distance(distance).create();
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
        if (getOs().getAdHocModule().isEnabled()) {
            getOs().getAdHocModule().disable();
        }
    }

    @Override
    protected void sendMessage() {
        Integer camId = getOs().getAdHocModule().sendCam();

        getLog().debugSimTime(this, "Sent CAM (id={}) with {} mW on AdHoc channel {}",
                camId,
                (adHocModuleConfiguration == null) ? -1 : adHocModuleConfiguration.getRadios().get(0).getPower(),
                (adHocModuleConfiguration == null) ? "null" : adHocModuleConfiguration.getRadios().get(0).getChannel0()
        );
    }

}
