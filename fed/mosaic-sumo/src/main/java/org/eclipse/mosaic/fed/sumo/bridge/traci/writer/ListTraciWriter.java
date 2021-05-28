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
import java.util.List;

public class ListTraciWriter<T> extends AbstractTraciParameterWriter<List<T>> {

    private final AbstractTraciParameterWriter<T> itemWriter;

    public ListTraciWriter(AbstractTraciParameterWriter<T> itemWriter) {
        super(4);
        this.itemWriter = itemWriter;
    }

    public ListTraciWriter(AbstractTraciParameterWriter<T> itemWriter, List<T> fixedValue) {
        super(4 + calcLengthOfList(itemWriter, fixedValue), fixedValue);
        this.itemWriter = itemWriter;
    }

    private static <T> int calcLengthOfList(AbstractTraciParameterWriter<T> itemWriter, List<T> list) {
        int length = 0;
        for (T item : list) {
            length += itemWriter.getVariableLength(item);
        }
        return length;
    }

    @Override
    public int getVariableLength(List<T> argument) {
        return getLength() + calcLengthOfList(itemWriter, argument);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        writeVariableArgument(out, super.value);
    }

    @Override
    public void writeVariableArgument(DataOutputStream out, List<T> list) throws IOException {
        out.writeInt(list.size());
        for (T item : list) {
            itemWriter.writeVariableArgument(out, item);
        }
    }
}
