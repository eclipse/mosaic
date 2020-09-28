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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;

/**
 * De/Serializes objects by adding a "__class" field to the JSON output.
 */
public class PolymorphismTypeAdapterFactory implements TypeAdapterFactory {

    private final static String classFieldName = "__class";

    public static class PolymorphismTypeAdapter<R> extends TypeAdapter<R> {

        private final Gson gson;
        private final PolymorphismTypeAdapterFactory parentFactory;

        private PolymorphismTypeAdapter(PolymorphismTypeAdapterFactory parentFactory, Gson gson) {
            this.gson = gson;
            this.parentFactory = parentFactory;
        }

        @Override
        public R read(JsonReader in) {
            JsonElement jsonElement = Streams.parse(in);
            JsonElement classJsonElement = jsonElement.getAsJsonObject().remove(classFieldName);
            if (classJsonElement == null) {
                throw new JsonParseException("cannot deserialize because it does not define a field named " + classFieldName);
            }
            String classname = classJsonElement.getAsString();
            try {

                @SuppressWarnings("unchecked")
                TypeAdapter<R> delegate = (TypeAdapter<R>) gson.getDelegateAdapter(parentFactory, TypeToken.get(Class.forName(classname)));
                if (delegate == null) {
                    throw new JsonParseException("cannot deserialize subtype named " + classname + "; No such type adapter.");
                }
                return delegate.fromJsonTree(jsonElement);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        @Override
        public void write(JsonWriter out, R value) throws IOException {
            Class<?> srcType = value.getClass();
            @SuppressWarnings("unchecked")
            TypeAdapter<R> delegate = (TypeAdapter<R>) gson.getDelegateAdapter(parentFactory, TypeToken.get(srcType));
            if (delegate == null) {
                throw new JsonParseException("cannot serialize subtype; No such type adapter?");
            }
            JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();
            if (jsonObject.has(classFieldName)) {
                throw new JsonParseException("cannot serialize " + srcType.getName()
                        + " because it already defines a field named " + classFieldName);
            }
            JsonObject clone = new JsonObject();
            clone.add(classFieldName, new JsonPrimitive(srcType.getCanonicalName()));
            for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
                clone.add(e.getKey(), e.getValue());
            }
            Streams.write(clone, out);
        }
    }

    public <R> TypeAdapter<R> create(final Gson gson, TypeToken<R> type) {
        return new PolymorphismTypeAdapter<R>(this, gson).nullSafe();
    }

}
