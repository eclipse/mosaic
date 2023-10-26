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

package org.eclipse.mosaic.fed.sumo.bridge.traci.writer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StringTraciWriter extends AbstractTraciParameterWriter<String> {

    public StringTraciWriter() {
        super(4);
    }

    public StringTraciWriter(String fixedValue) {
        super(4 + fixedValue.length(), fixedValue);
    }

    @Override
    public int getVariableLength(String argument) {
        return getLength() + argument.getBytes(StandardCharsets.UTF_8).length;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    @Override
    public void writeVariableArgument(DataOutputStream out, String argument) throws IOException {
        byte[] bytes = argument.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }
}
