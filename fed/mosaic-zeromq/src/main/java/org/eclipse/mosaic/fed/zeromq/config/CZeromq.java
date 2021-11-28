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

package org.eclipse.mosaic.fed.zeromq.config;

import org.eclipse.mosaic.fed.zeromq.device.AmbassadorWorker;
import org.eclipse.mosaic.fed.zeromq.device.AmbassadorBroker;

/**
 * Zeromq configuration.
 */
public class CZeromq {

    private String backend;
    private String frontend;
    private AmbassadorBroker ambassadorBroker;

    public CZeromq(String backend, String frontend, AmbassadorBroker ambassadorBroker,
            AmbassadorWorker ambassadorWorker) {
        this.backend = backend;
        this.frontend = frontend;
        this.ambassadorBroker = new AmbassadorBroker(frontend, backend);
    }

    public String getBackend() {
        return backend;
    }
    public String getFrontend() {
        return frontend;
    }
    public AmbassadorBroker getAmbassadorBroker() {
        return ambassadorBroker;
    }
    public Thread getAmbassadorBrokerThread(){
        return new Thread(ambassadorBroker);
    }
}
