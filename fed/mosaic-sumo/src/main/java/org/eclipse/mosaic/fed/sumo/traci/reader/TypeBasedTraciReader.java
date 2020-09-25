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

package org.eclipse.mosaic.fed.sumo.traci.reader;

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciResultReader;
import org.eclipse.mosaic.fed.sumo.traci.constants.TraciDatatypes;
import org.eclipse.mosaic.lib.util.objects.Position;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

public class TypeBasedTraciReader extends AbstractTraciResultReader<Object> {

    private final Position2dTraciReader position2dReader = new Position2dTraciReader();
    private final Position3dTraciReader position3dReader = new Position3dTraciReader();
    private final ListTraciReader<String> stringListReader = new ListTraciReader<>(new StringTraciReader());

    private AbstractTraciResultReader<?> compoundReader = null;

    protected TypeBasedTraciReader() {
        this(null);
    }

    public TypeBasedTraciReader(Matcher<Object> matcher) {
        super(matcher);
    }

    public void registerCompoundReader(AbstractTraciResultReader<?> compoundReader) {
        this.compoundReader = compoundReader;
    }

    @Override
    protected Object readFromStream(DataInputStream in) throws IOException {
        int varReturnType = readByte(in);

        switch (varReturnType) {
            case TraciDatatypes.FLOAT:
                return readFloat(in);
            case TraciDatatypes.POSITION2D:
                Position pos = position2dReader.read(in, totalBytesLeft - numBytesRead);
                numBytesRead += position2dReader.getNumberOfBytesRead();
                return pos;
            case TraciDatatypes.POSITION3D:
                pos = position3dReader.read(in, totalBytesLeft - numBytesRead);
                numBytesRead += position3dReader.getNumberOfBytesRead();
                return pos;
            case TraciDatatypes.INTEGER:
                return readInt(in);
            case TraciDatatypes.STRING:
                return readString(in);
            case TraciDatatypes.STRING_LIST:
                List<String> result = stringListReader.read(in, totalBytesLeft - numBytesRead);
                numBytesRead += stringListReader.getNumberOfBytesRead();
                return result;
            case TraciDatatypes.DOUBLE:
                return readDouble(in);
            case TraciDatatypes.UBYTE:
                return readUnsignedByte(in);
            case TraciDatatypes.COMPOUND:
                readInt(in); // required at least for reading inductionloop vehicle data compound
                if (compoundReader != null) {
                    Object complex = compoundReader.read(in, totalBytesLeft - numBytesRead);
                    numBytesRead += compoundReader.getNumberOfBytesRead();
                    return complex;
                }
            default:
                throw new RuntimeException("Subscribed variable type " + varReturnType + " not known.");
        }
    }
}
