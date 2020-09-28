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

package org.eclipse.mosaic.fed.sumo.traci;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class AbstractTraciParameterWriter<T> {

    private final int length;

    protected final T value;

    /**
     * Creates a new {@link AbstractTraciParameterWriter} which allows
     * to write a variable TraCI command parameter value to the socket connection.
     *
     * @param length the length of the a typical value in bytes when writing.
     */
    protected AbstractTraciParameterWriter(int length) {
        this(length, null);
    }

    /**
     * Creates a new {@link AbstractTraciParameterWriter} which allows
     * to write a predefined TraCI command parameter value to the socket connection.
     *
     * @param length the length of the given value in bytes when writing
     * @param value  the predefined constant value to write
     */
    protected AbstractTraciParameterWriter(int length, T value) {
        this.length = length;
        this.value = value;
    }

    /**
     * States if this command variable holds a constant value to write or
     * if it needs to get passed a variable value.
     *
     * @return {@code true}, if the actual value is passed later.
     */
    public boolean isVariable() {
        return value == null;
    }

    /**
     * Returns the length of this command parameter in bytes, e.g. 4 for Integer.
     *
     * @return the length of the parameter in bytes
     */
    public int getLength() {
        return length;
    }

    /**
     * If the value of the parameter has a variable length depending on its actual value (e.g. Strings),
     * then this method must return the length in bytes based on the given argument.
     *
     * @param argument the argument to calculate the length in bytes from.
     * @return the length in bytes of this command parameter.
     */
    public abstract int getVariableLength(T argument);

    /**
     * Writes the predefined, constant command parameter value to the given {@link DataOutputStream}.
     *
     * @param out the {@link DataOutputStream} to write the actual bytes to
     * @throws IOException if something went wrong during writing
     */
    public abstract void write(DataOutputStream out) throws IOException;

    /**
     * Writes the given variable command parameter value to the given {@link DataOutputStream}.
     *
     * @param out      the {@link DataOutputStream} to write the actual bytes to
     * @param argument the actual value to write to the {@link DataOutputStream}
     * @throws IOException if something went wrong during writing
     */
    public abstract void writeVariableArgument(DataOutputStream out, T argument) throws IOException;
}