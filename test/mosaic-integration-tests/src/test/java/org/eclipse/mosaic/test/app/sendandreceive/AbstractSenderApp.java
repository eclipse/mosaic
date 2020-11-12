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

import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;

public abstract class AbstractSenderApp extends AbstractApplication<VehicleOperatingSystem> {

    private final long sendingInterval;
    private final long sendingDuration;

    private long endTime;

    AbstractSenderApp(long sendingInterval, long sendingDuration) {
        this.sendingInterval = sendingInterval;
        this.sendingDuration = sendingDuration;
    }

    @Override
    public void onStartup() {
        // Adds an sending offset in order to avoid collisions of messages
        long sendingOffset = getRandom().nextInt(0, 100) * TIME.MILLI_SECOND;

        this.endTime = getOs().getSimulationTime() + sendingDuration + sendingOffset;
        if (this.endTime < 0) {
            this.endTime = Long.MAX_VALUE;
        }

        configureCommunication();
        printCommunicationState();

        sample(sendingInterval + sendingOffset);
    }

    private void sample(long timeFromNow) {
        getOs().getEventManager().addEvent(
                getOs().getSimulationTime() + timeFromNow, this
        );
    }

    protected abstract void configureCommunication();

    protected abstract void disableCommunication();

    @Override
    public void onShutdown() {
        disableCommunication();
        printCommunicationState();
    }

    @Override
    public void processEvent(Event event) {
        if (!isValidStateAndLog()) {
            return;
        }
        if (getOs().getSimulationTime() > endTime) {
            disableCommunication();
            return;
        }

        sendMessage();
        sample(sendingInterval);
    }

    protected abstract void sendMessage();

    private void printCommunicationState() {
        getLog().debugSimTime(this, "communicationState - adhocEnabled={}, cellEnabled={}",
                getOs().getAdHocModule().isEnabled(),
                getOs().getCellModule().isEnabled()
        );
    }
}
