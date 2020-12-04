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

import java.io.DataInputStream;
import java.io.IOException;

public class CommandLengthReader extends AbstractTraciResultReader<Integer> {

    public CommandLengthReader() {
        super(null);
    }

    public CommandLengthReader(Matcher<Integer> matcher) {
        super(matcher);
    }

    @Override
    protected Integer readFromStream(DataInputStream in) throws IOException {
        int commandLength = readUnsignedByte(in);
        if (commandLength == 0) {
            // read extended message
            commandLength = readInt(in);
        }
        return commandLength;
    }
}
