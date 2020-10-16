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

package org.eclipse.mosaic.fed.cell.config.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;

/**
 * Adapter which allows to configure the string "unlimited" for the capacity of
 * regions. In such case, the capacity is set to {@link Long#MAX_VALUE}.
 */
public final class CapacityTypeAdapter extends TypeAdapter<Long> {

    private final static String UNLIMITED = "unlimited";

    @Override
    public void write(JsonWriter out, Long param) throws IOException {
        if (param != null && param == Long.MAX_VALUE) {
            out.value(UNLIMITED);
        } else {
            out.value(ObjectUtils.defaultIfNull(param, 0L));
        }
    }

    @Override
    public Long read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            return 0L;
        } else if (in.peek() == JsonToken.NUMBER) {
            return in.nextLong();
        } else if (in.peek() == JsonToken.STRING) {
            return UNLIMITED.equals(in.nextString()) ? Long.MAX_VALUE : 0;
        } else {
            return 0L;
        }
    }
}

