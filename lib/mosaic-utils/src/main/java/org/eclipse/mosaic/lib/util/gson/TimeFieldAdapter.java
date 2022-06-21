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

import org.eclipse.mosaic.rti.TIME;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapter for JSON fields which translates values as string representatives to
 * actual long values (nanoseconds), e.g. "10 ms" -> 10_000_000, "20 ns" -> 20, "0.5h" -> 1_800_000_000_000
 * <br><br>
 * Usage:
 * <pre>
 *
 * &#64;JsonAdapter(TimeFieldAdapter.NanoSeconds.class)
 * public long startTime;
 *
 * </pre>
 */
public class TimeFieldAdapter extends TypeAdapter<Long> {


    private static final Logger log = LoggerFactory.getLogger(TimeFieldAdapter.class);

    private final static Pattern TIME_PATTERN = Pattern.compile("^([0-9]+[0-9_]*?\\.?[0-9]*) ?(min|minute|minutes|h|hour|hours|(|m|\\u00b5|n|milli|micro|nano)(?:|s|sec|second|seconds))$");
    private final static Map<String, Long> MULTIPLIERS = ImmutableMap.<String, Long>builder()
            .put("", TIME.SECOND)
            .put("n", TIME.NANO_SECOND)
            .put("\u00b5", TIME.MICRO_SECOND)
            .put("m", TIME.MILLI_SECOND)
            .put("nano", TIME.NANO_SECOND)
            .put("micro", TIME.MICRO_SECOND)
            .put("milli", TIME.MILLI_SECOND)
            .build();

    private final long legacyDivisor;
    private final boolean failOnError;

    private TimeFieldAdapter(boolean failOnError) {
        this(TIME.NANO_SECOND, failOnError);
    }

    private TimeFieldAdapter(long legacyDivisor, boolean failOnError) {
        this.legacyDivisor = legacyDivisor;
        this.failOnError = failOnError;
    }

    @Override
    public void write(JsonWriter out, Long param) throws IOException {
        String unit = "";
        param = ObjectUtils.defaultIfNull(param, 0l);
        if (param == 0) {
            unit = "s";
        } else if (param % TIME.HOUR == 0) {
            unit = "h";
            param /= TIME.HOUR;
        } else if (param % TIME.MINUTE == 0) {
            unit = "min";
            param /= TIME.MINUTE;
        } else if (param % TIME.SECOND == 0) {
            unit = "s";
            param /= TIME.SECOND;
        } else if (param % TIME.MILLI_SECOND == 0) {
            unit = "ms";
            param /= TIME.MILLI_SECOND;
        } else {
            unit = "ns";
        }
        out.value(param + " " + unit);
    }

    @Override
    public Long read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            return 0L;
        } else if (in.peek() == JsonToken.NUMBER) {
            return in.nextLong();
        } else if (in.peek() == JsonToken.STRING) {
            String time = StringUtils.lowerCase(in.nextString()).trim();
            return parseTime(time);
        } else {
            return 0L;
        }
    }

    private Long parseTime(String timeDesc) {
        Matcher m = TIME_PATTERN.matcher(timeDesc);
        if (m.matches()) {
            final double value = Double.parseDouble(StringUtils.remove(m.group(1), '_'));
            final long multiplier;
            if (StringUtils.isEmpty(m.group(2))) {
                multiplier = legacyDivisor;
            } else {
                multiplier = determineMultiplier(m.group(2), m.group(3));
                if (multiplier < legacyDivisor) {
                    log.warn("Given prefix in time description {} is lower than expected. This might result in wrong behavior.", timeDesc);
                }
            }
            return ((long) (value * multiplier)) / legacyDivisor;
        }
        if (failOnError) {
            throw new IllegalArgumentException("Could not resolve \"" + timeDesc + "\"");
        }
        log.warn("Could not resolve \"{}\"", timeDesc);
        return 0L;
    }

    private long determineMultiplier(String timeUnit, String secondPrefix) {
        if (secondPrefix != null) {
            return determineMultiplierFromSubsecond(secondPrefix);
        } else if (timeUnit.startsWith("m")) {
            return TIME.MINUTE;
        } else if (timeUnit.startsWith("h")) {
            return TIME.HOUR;
        }
        return 1;
    }

    private long determineMultiplierFromSubsecond(String prefix) {
        return Validate.notNull(MULTIPLIERS.get(prefix), "Invalid time prefix " + prefix);
    }

    public static class NanoSeconds implements TypeAdapterFactory {

        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return (TypeAdapter<T>) new TimeFieldAdapter(true);
        }
    }

    public static class LegacyMilliSeconds implements TypeAdapterFactory {

        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return (TypeAdapter<T>) new TimeFieldAdapter(TIME.MILLI_SECOND, true);
        }
    }

    public static class LegacySeconds implements TypeAdapterFactory {

        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return (TypeAdapter<T>) new TimeFieldAdapter(TIME.SECOND, true);
        }
    }

    public static class NanoSecondsQuiet implements TypeAdapterFactory {

        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return (TypeAdapter<T>) new TimeFieldAdapter(false);
        }
    }

    public static class LegacyMilliSecondsQuiet implements TypeAdapterFactory {

        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return (TypeAdapter<T>) new TimeFieldAdapter(TIME.MILLI_SECOND, false);
        }
    }

    public static class LegacySecondsQuiet implements TypeAdapterFactory {

        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return (TypeAdapter<T>) new TimeFieldAdapter(TIME.SECOND, false);
        }
    }
}

