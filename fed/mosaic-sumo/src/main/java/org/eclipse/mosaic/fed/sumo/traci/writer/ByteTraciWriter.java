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
 */

package org.eclipse.mosaic.fed.sumo.traci.writer;

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciParameterWriter;

import org.apache.commons.lang3.Validate;

import java.io.DataOutputStream;
import java.io.IOException;

public class ByteTraciWriter extends AbstractTraciParameterWriter<Integer> {

    public ByteTraciWriter() {
        super(1);
    }

    public ByteTraciWriter(int value) {
        super(1, value);
    }

    @Override
    public int getVariableLength(Integer argument) {
        return getLength();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        writeVariableArgument(out, value);
    }

    @Override
    public void writeVariableArgument(DataOutputStream out, Integer argument) throws IOException {
        // Java bytes are always signed (-127..127); however, we want to write unsigned
        // bytes from 0 to 255 which is the reason for using int instead
        Validate.inclusiveBetween(Integer.valueOf(0), Integer.valueOf(255), argument);
        out.writeByte(argument);
    }
}
