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

package org.eclipse.mosaic.fed.sumo.bridge.traci.reader;

import org.eclipse.mosaic.fed.sumo.bridge.api.complex.Status;

import java.io.DataInputStream;
import java.io.IOException;

public class StatusReader extends AbstractTraciResultReader<Status> {

    public StatusReader() {
        super(null);
    }

    @Override
    public Status readFromStream(DataInputStream in) throws IOException {
        byte resultType = readByte(in);
        String description = readString(in);
        return new Status(resultType, description);
    }
}
