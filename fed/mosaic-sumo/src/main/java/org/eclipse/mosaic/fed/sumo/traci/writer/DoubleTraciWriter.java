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

import java.io.DataOutputStream;
import java.io.IOException;

public class DoubleTraciWriter extends AbstractTraciParameterWriter<Double> {

    public DoubleTraciWriter() {
        super(8);
    }

    public DoubleTraciWriter(double value) {
        super(8, value);
    }

    @Override
    public int getVariableLength(Double argument) {
        return getLength();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeDouble(value);
    }

    @Override
    public void writeVariableArgument(DataOutputStream out, Double argument) throws IOException {
        out.writeDouble(argument);
    }
}
