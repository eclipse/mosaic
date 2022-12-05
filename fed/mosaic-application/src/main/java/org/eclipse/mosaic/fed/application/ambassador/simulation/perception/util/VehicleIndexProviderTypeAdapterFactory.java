/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception.util;

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.VehicleIndex;
import org.eclipse.mosaic.lib.gson.AbstractTypeAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

public class VehicleIndexProviderTypeAdapterFactory implements TypeAdapterFactory {
    public static class VehicleIndexProviderTypeAdapter extends AbstractTypeAdapterFactory<VehicleIndex> {

        private VehicleIndexProviderTypeAdapter(TypeAdapterFactory parentFactory, Gson gson) {
            super(parentFactory, gson);
        }

        @Override
        protected Class<?> fromTypeName(String type) {
            try {
                return Class.forName(VehicleIndex.class.getPackage().getName() + "." + type);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(
                        "Cannot deserialize Vehicle Index Provider named " + type + "; Vehicle Index Provider doesn't exist.");
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
        return (TypeAdapter<T>) new VehicleIndexProviderTypeAdapter(this, gson).nullSafe();
    }
}