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

import org.eclipse.mosaic.lib.geo.UtmPoint;
import org.eclipse.mosaic.lib.geo.UtmZone;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class UtmPointAdapter implements JsonDeserializer<UtmPoint>, JsonSerializer<UtmPoint> {

    @Override
    public UtmPoint deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject json = jsonElement.getAsJsonObject();

        if (!json.has("northing")) {
            throw new IllegalArgumentException("No member \"northing\" found");
        }
        if (!json.has("easting")) {
            throw new IllegalArgumentException("No member \"easting\" found");
        }
        if (!json.has("zone")) {
            throw new IllegalArgumentException("No member \"zone\" found");
        }

        return UtmPoint.northEast(
                UtmZone.from(json.get("zone").getAsString()),
                json.get("northing").getAsDouble(),
                json.get("easting").getAsDouble()
        );
    }

    @Override
    public JsonElement serialize(UtmPoint point, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = new JsonObject();
        object.add("easting", new JsonPrimitive(point.getEasting()));
        object.add("northing", new JsonPrimitive(point.getNorthing()));
        object.add("zone", new JsonPrimitive(point.getZone().getNumber() + "" + point.getZone().getLetter()));
        return object;
    }
}
