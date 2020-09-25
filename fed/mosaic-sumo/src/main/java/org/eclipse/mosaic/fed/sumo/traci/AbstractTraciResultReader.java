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

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;

/**
 * Abstract TraCI result reader.
 *
 * @param <T> The data type of the result
 */
public abstract class AbstractTraciResultReader<T> {

    /**
     * Matcher to verify result.
     */
    private final Matcher<T> matcher;

    /**
     * Number of bytes that have benn read.
     */
    protected int numBytesRead = 0;

    /**
     * Total number od bytes.
     */
    protected int totalBytesLeft = 0;

    /**
     * Creates a new AbstractTraciResultReader.
     *
     * @param matcher a matcher which will be used to check the value of the read value.
     *                <code>null</code>, if the value read from the stream should be returned by {@link #read(DataInputStream, int)}
     */
    protected AbstractTraciResultReader(@Nullable Matcher<T> matcher) {
        this.matcher = matcher;
    }

    /**
     * Getter of numBytesRead.
     *
     * @return number of read bytes
     */
    public int getNumberOfBytesRead() {
        return numBytesRead;
    }

    /**
     * Reads data from {@param inputStream}, verifies it with the {@link #matcher} (if it was set)
     * and returns the result (if it is valid).
     *
     * @param inputStream the stream which is used to read the bytes from
     * @param bytesLeft   the total number of bytes left on the stream to read
     * @return <code>null</code> if a matcher exists and succeeded. if no matcher is set, the value read from the stream is returned
     * @throws IOException if the matcher failed
     */
    public T read(DataInputStream inputStream, int bytesLeft) throws IOException {
        totalBytesLeft = bytesLeft;
        numBytesRead = 0;
        T result = readFromStream(inputStream);
        if (matcher != null && !matcher.matches(result)) {
            throw new IOException("Matcher of " + this.getClass().getSimpleName() + " failed.");
        } else if (matcher != null) {
            return null;
        }
        return result;
    }

    protected abstract T readFromStream(DataInputStream in) throws IOException;

    protected byte[] readFullyByLength(DataInputStream in, int len) throws IOException {
        byte[] b = new byte[len];
        in.readFully(b);
        numBytesRead += len;
        return b;
    }

    protected byte readByte(DataInputStream in) throws IOException {
        numBytesRead += 1;
        return in.readByte();
    }

    protected int readUnsignedByte(DataInputStream in) throws IOException {
        numBytesRead += 1;
        return in.readUnsignedByte();
    }

    protected int readInt(DataInputStream in) throws IOException {
        numBytesRead += 4;
        return in.readInt();
    }

    protected double readDouble(DataInputStream in) throws IOException {
        numBytesRead += 8;
        return in.readDouble();
    }

    protected float readFloat(DataInputStream in) throws IOException {
        numBytesRead += 4;
        return in.readFloat();
    }

    protected String readString(DataInputStream in) throws IOException {
        int len = readInt(in);
        byte[] bytes = readFullyByLength(in, len);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public interface Matcher<T> {
        boolean matches(T actual);
    }
}
