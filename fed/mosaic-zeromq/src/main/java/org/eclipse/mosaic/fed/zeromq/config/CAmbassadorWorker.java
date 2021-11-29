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

public class CAmbassadorWorker {

    private String backend;
    private String contract;

    public CAmbassadorWorker(String backend, String contract) {
        this.backend = backend;
        this.contract = contract;
    }

    public String getContract() {
        return contract;
    }

    public String getBackend() {
        return backend;
    }
}
