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

package org.eclipse.mosaic.lib.gson;

import org.eclipse.mosaic.lib.geo.GeoArea;
import org.eclipse.mosaic.lib.geo.GeoCircle;
import org.eclipse.mosaic.lib.geo.GeoPolygon;
import org.eclipse.mosaic.lib.geo.GeoRectangle;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

public final class GeoAreaAdapterFactory implements TypeAdapterFactory {

    public static class GeoAreaAdapter extends AbstractTypeTypeAdapter<GeoArea> {

        private final static String TYPE_RECTANGLE = "rectangle";
        private final static String TYPE_CIRCLE = "circle";
        private final static String TYPE_POLYGON = "polygon";

        private GeoAreaAdapter(TypeAdapterFactory parentFactory, Gson gson) {
            super(parentFactory, gson);
        }

        @Override
        protected Class<?> fromTypeName(String type) {
            return TYPE_CIRCLE.equalsIgnoreCase(type) ? GeoCircle.class
                    : TYPE_RECTANGLE.equalsIgnoreCase(type) ? GeoRectangle.class
                    : TYPE_POLYGON.equalsIgnoreCase(type) ? GeoPolygon.class
                    : null;
        }

        @Override
        protected String toTypeName(Class<?> typeClass) {
            return GeoCircle.class.isAssignableFrom(typeClass) ? TYPE_CIRCLE
                    : GeoRectangle.class.isAssignableFrom(typeClass) ? TYPE_RECTANGLE
                    : GeoPolygon.class.isAssignableFrom(typeClass) ? TYPE_POLYGON
                    : null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        return (TypeAdapter<T>) new GeoAreaAdapter(this, gson).nullSafe();
    }
}
