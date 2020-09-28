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

package org.eclipse.mosaic.lib.util.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public abstract class AbstractEnumDefaultValueTypeAdapter<E extends Enum<E>> extends TypeAdapter<E> {

    private final E defaultValue;

    public AbstractEnumDefaultValueTypeAdapter(E defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public void write(JsonWriter out, E value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public E read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        try {
            return (E) E.valueOf(defaultValue.getClass(), in.nextString());
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}