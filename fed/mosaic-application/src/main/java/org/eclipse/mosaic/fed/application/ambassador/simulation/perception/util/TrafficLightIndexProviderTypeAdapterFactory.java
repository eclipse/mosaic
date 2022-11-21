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

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.TrafficLightIndexProvider;
import org.eclipse.mosaic.lib.gson.AbstractTypeAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

public class TrafficLightIndexProviderTypeAdapterFactory implements TypeAdapterFactory {
    public static class TrafficLightIndexProviderTypeAdapter extends AbstractTypeAdapterFactory<TrafficLightIndexProvider> {

        private TrafficLightIndexProviderTypeAdapter(TypeAdapterFactory parentFactory, Gson gson) {
            super(parentFactory, gson);
        }

        @Override
        protected Class<?> fromTypeName(String type) {
            try {
                return Class.forName(TrafficLightIndexProvider.class.getPackage().getName() + "." + type);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(
                        "Cannot deserialize Traffic Light Index Provider named " + type + "; Traffic Light Index Provider doesn't exist.");
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
        return (TypeAdapter<T>) new TrafficLightIndexProviderTypeAdapterFactory
                .TrafficLightIndexProviderTypeAdapter(this, gson).nullSafe();
    }
}
