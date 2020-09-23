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

package org.eclipse.mosaic.lib.gson;

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

public abstract class AbstractTypeAdapterFactory<T> extends TypeAdapter<T> {

    private final static String TYPE_FIELD = "type";

    private final Gson gson;
    private final TypeAdapterFactory parentFactory;

    protected AbstractTypeAdapterFactory(TypeAdapterFactory parentFactory, Gson gson) {
        this.parentFactory = parentFactory;
        this.gson = gson;
    }

    /**
     * Translates the name of the type to the class to deserialize.
     *
     * @param type the name of the type
     * @return the class associated with the type
     */
    protected abstract Class<?> fromTypeName(String type);

    /**
     * Translates the class of the object to serialize to an unambiguous type name.
     *
     * @param typeClass the class to serialize
     * @return the type name which is associated with the class to serialize
     */
    protected abstract String toTypeName(Class<?> typeClass);

    @Override
    public T read(JsonReader in) {
        JsonElement jsonElement = Streams.parse(in);
        JsonElement typeJsonElement = jsonElement.getAsJsonObject().remove(TYPE_FIELD);
        if (typeJsonElement == null) {
            throw new JsonParseException("cannot deserialize because it does not define a field named " + TYPE_FIELD);
        }
        String typeName = typeJsonElement.getAsString();

        @SuppressWarnings("unchecked")
        TypeAdapter<T> delegate = (TypeAdapter<T>) gson.getDelegateAdapter(parentFactory, TypeToken.get(fromTypeName(typeName)));
        if (delegate == null) {
            throw new JsonParseException("Cannot deserialize subtype named " + typeName + "; No such type adapter.");
        }
        return delegate.fromJsonTree(jsonElement);
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        Class<?> srcType = value.getClass();
        @SuppressWarnings("unchecked")
        TypeAdapter<T> delegate = (TypeAdapter<T>) gson.getDelegateAdapter(parentFactory, TypeToken.get(srcType));
        if (delegate == null) {
            throw new JsonParseException("cannot serialize subtype; No such type adapter?");
        }
        JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();
        if (jsonObject.has(TYPE_FIELD)) {
            throw new JsonParseException("cannot serialize " + srcType.getName()
                    + " because it already defines a field named " + TYPE_FIELD);
        }
        JsonObject clone = new JsonObject();
        String typeName = toTypeName(srcType);
        if (typeName == null) {
            throw new JsonParseException("cannot serialize " + srcType.getName() + " because it's type name is not defined.");
        }
        clone.add(TYPE_FIELD, new JsonPrimitive(typeName));
        for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
            clone.add(e.getKey(), e.getValue());
        }
        Streams.write(clone, out);
    }
}
