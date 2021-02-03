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

package org.eclipse.mosaic.rti.api.federatestarter;

import org.eclipse.mosaic.rti.api.FederateAmbassador;
import org.eclipse.mosaic.rti.api.FederateExecutor;
import org.eclipse.mosaic.rti.config.CLocalHost;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * A {@link FederateExecutor} implementation which does nothing when called (no-operation).
 * This should be used mainly by {@link FederateAmbassador}
 * implementations which do not connect with a separate federate but which are
 * directly coupled with the RTI.
 */
public class NopFederateExecutor implements FederateExecutor {

    @Override
    public Process startLocalFederate(File workingDir) {
        //nop
        return null;
    }

    @Override
    public int startRemoteFederate(CLocalHost host, PrintStream sshStream, InputStream sshStreamIn) {
        //nop
        return -1;
    }

    @Override
    public void stopLocalFederate() {
        //nop
    }

    @Override
    public void stopRemoteFederate(PrintStream sshStreamOut) {
        //nop
    }

    @Override
    public String toString() {
        return "No-Operation Executor";
    }
}
