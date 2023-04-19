/*
 * Copyright (c) 2023 Fraunhofer FOKUS and others. All rights reserved.
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

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.TrafficLightIndex;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.providers.WallIndex;
import org.eclipse.mosaic.lib.gson.AbstractTypeAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

public class WallIndexTypeAdapterFactory implements TypeAdapterFactory {
    public static class WallIndexTypeAdapter extends AbstractTypeAdapterFactory<WallIndex> {

        private WallIndexTypeAdapter(TypeAdapterFactory parentFactory, Gson gson) {
            super(parentFactory, gson);
        }

        @Override
        protected Class<?> fromTypeName(String type) {
            try {
                return Class.forName(WallIndex.class.getPackage().getName() + "." + type);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(
                        "Cannot deserialize Wall Index named " + type + "; Wall Index doesn't exist.");
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
        return (TypeAdapter<T>) new WallIndexTypeAdapter(this, gson).nullSafe();
    }
}
