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

package org.eclipse.mosaic.fed.sumo.traci.writer;

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciParameterWriter;

import java.io.DataOutputStream;
import java.io.IOException;

public class IntegerTraciWriter extends AbstractTraciParameterWriter<Integer> {

    public IntegerTraciWriter() {
        super(4);
    }

    public IntegerTraciWriter(int commandIdentifier) {
        super(4, commandIdentifier);
    }

    @Override
    public int getVariableLength(Integer argument) {
        return getLength();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(value);
    }

    @Override
    public void writeVariableArgument(DataOutputStream out, Integer argument) throws IOException {
        out.writeInt(argument);
    }
}
