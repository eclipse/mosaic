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

public class ReceiveMsgAppAdHoc extends AbstractReceiverApp {

    @Override
    protected void configureCommunication() {
        getOs().getAdHocModule().enable(new AdHocModuleConfiguration()
                .addRadio().channel(AdHocChannel.CCH).power(50).create());
    }

    @Override
    protected void disableCommunication() {
        getOs().getAdHocModule().disable();
    }
}
