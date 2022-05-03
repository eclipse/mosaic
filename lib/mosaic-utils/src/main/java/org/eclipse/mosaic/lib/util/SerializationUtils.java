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

package org.eclipse.mosaic.lib.util;

import org.eclipse.mosaic.lib.geo.GeoPoint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Default Java serialization for objects. The main advantage of this approach
 * is the easy way to handle the marshalling: it is all already done. So the
 * developer can focus on the main facts.
 *
 * @param <T> type of the class for the serialization.
 */
public final class SerializationUtils<T> {

    public static void encodeGeoPoint(DataOutput output, GeoPoint geoPoint) throws IOException {
        output.writeDouble(geoPoint.getLatitude());
        output.writeDouble(geoPoint.getLongitude());
        output.writeDouble(geoPoint.getAltitude());
    }

    public static GeoPoint decodeGeoPoint(DataInput input) throws IOException {
        return GeoPoint.latLon(
                input.readDouble(),
                input.readDouble(),
                input.readDouble()
        );
    }

    @SuppressWarnings("unchecked")
    public T fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes); ObjectInput in = new ObjectInputStream(bis)) {
            return (T) in.readObject();
        }
    }

    @SuppressWarnings("unchecked")
    public T fromBytes(byte[] bytes, ClassLoader classLoader) throws IOException, ClassNotFoundException {
        try (
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInput in = new ObjectInputStreamWithClassLoader(bis, classLoader)
        ) {
            return (T) in.readObject();
        }
    }

    public byte[] toBytes(T t) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(t);
            out.flush();
            return bos.toByteArray();
        }
    }
}
