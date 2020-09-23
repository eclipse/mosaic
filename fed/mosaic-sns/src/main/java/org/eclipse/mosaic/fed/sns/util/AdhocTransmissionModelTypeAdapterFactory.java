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

package org.eclipse.mosaic.fed.sns.util;

import org.eclipse.mosaic.fed.sns.model.AdhocTransmissionModel;
import org.eclipse.mosaic.lib.gson.AbstractTypeAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

/**
 * Adapter factory for JSON to configure the two kinds of "simple" or "sophisticated" transmissionModels more comfortably.
 */
public final class AdhocTransmissionModelTypeAdapterFactory implements TypeAdapterFactory {

    public static class AdhocTransmissionModelTypeAdapter extends AbstractTypeAdapterFactory<AdhocTransmissionModel> {

        private AdhocTransmissionModelTypeAdapter(TypeAdapterFactory parentFactory, Gson gson) {
            super(parentFactory, gson);
        }

        @Override
        protected Class<?> fromTypeName(String type) {
            try {
                return Class.forName(AdhocTransmissionModel.class.getPackage().getName() + "." + type);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(
                        "Cannot deserialize AdhocTransmissionModel named " + type + "; AdhocTransmissionModel doesn't exist.");
            }
        }

        @Override
        protected String toTypeName(Class<?> typeClass) {
            return typeClass.getSimpleName();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        return (TypeAdapter<T>) new AdhocTransmissionModelTypeAdapter(this, gson).nullSafe();
    }
}
