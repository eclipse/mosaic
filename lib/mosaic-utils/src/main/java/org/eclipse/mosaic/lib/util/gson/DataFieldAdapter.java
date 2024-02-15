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

import org.eclipse.mosaic.rti.DATA;

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
 * actual long values representing the number of bits, e.g.
 * <br>for size: "10 Bytes" -> 80, "2kbit" -> 2000
 * <br>for bandwidth: "10 MBps km/h" -> 80000000, "5 kbps" -> 5000
 * <br><br>
 * Note: using "b" or "B" is a difference, as former stands for bits and later for bytes.
 * <br><br>
 * Usage:
 * <pre>
 *
 *  &#64;JsonAdapter(DataFieldAdapter.Size.class)
 *  public long size;
 *
 * </pre>
 */
public class DataFieldAdapter extends TypeAdapter<Long> {

    private static final Logger log = LoggerFactory.getLogger(DataFieldAdapter.class);

    private final static String UNLIMITED = "unlimited";

    private final static Pattern BANDWIDTH_PATTERN = Pattern.compile("^([0-9]+\\.?[0-9]*) ?(|(|k|M|G|T|Ki|Mi|Gi|Ti)(B|b|bit|Bit|bits|Bits|byte|Byte|bytes|Bytes)(|ps))$");
    private final static Map<String, Long> MULTIPLIERS = ImmutableMap.<String, Long>builder()
            .put("", DATA.BIT)
            .put("k", DATA.KILOBIT)
            .put("m", DATA.MEGABIT)
            .put("g", DATA.GIGABIT)
            .put("t", DATA.TERABIT)
            .put("ki", DATA.KIBIBIT)
            .put("mi", DATA.MEBIBIT)
            .put("gi", DATA.GIBIBIT)
            .put("ti", DATA.TEBIBIT)
            .build();
    public static final String PS = "ps";

    private final String unitSuffix;
    private final boolean failOnError;

    private DataFieldAdapter(boolean failOnError) {
        this("", failOnError);
    }

    private DataFieldAdapter(String unitSuffix, boolean failOnError) {
        this.unitSuffix = unitSuffix;
        this.failOnError = failOnError;
    }

    @Override
    public void write(JsonWriter out, Long param) throws IOException {
        if (param != null && param == Long.MAX_VALUE) {
            out.value(UNLIMITED);
        } else {
            long value = ObjectUtils.defaultIfNull(param, 0L);
            String unitPrefix = "";

            if (value == 0) {
                unitPrefix = "";
            } else if (value % DATA.TERABIT == 0) {
                unitPrefix = "T";
                value /= DATA.TERABIT;
            } else if (value % DATA.GIGABIT == 0) {
                unitPrefix = "G";
                value /= DATA.GIGABIT;
            } else if (value % DATA.MEGABIT == 0) {
                unitPrefix = "M";
                value /= DATA.MEGABIT;
            } else if (value % DATA.KILOBIT == 0) {
                unitPrefix = "k";
                value /= DATA.KILOBIT;
            }
            out.value(value + " " + unitPrefix + "b" + this.unitSuffix);
        }
    }

    @Override
    public Long read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            return 0L;
        } else if (in.peek() == JsonToken.NUMBER) {
            return in.nextLong();
        } else if (in.peek() == JsonToken.STRING) {
            String bandwidth = in.nextString().trim();
            return UNLIMITED.equals(bandwidth)
                    ? Long.valueOf(Long.MAX_VALUE)
                    : parseBandwidth(bandwidth);
        } else {
            return 0L;
        }
    }

    private Long parseBandwidth(String bandwidthDesc) {
        Matcher m = BANDWIDTH_PATTERN.matcher(bandwidthDesc);
        if (m.matches()) {
            double value = Double.parseDouble(m.group(1));
            long multiplier;
            if (StringUtils.isBlank(m.group(2))) {
                multiplier = 1;
            } else {
                if (!unitSuffix.equals(m.group(5))) {
                    if (failOnError) {
                        throw new IllegalArgumentException("Suffix \"" + m.group(5) + "\" does not match with \"" + unitSuffix + "\"");
                    }
                    log.warn("Suffix \"{}\" does not match with \"{}\". Ignoring.", m.group(5), unitSuffix);
                }
                multiplier = determineMultiplier(m.group(3), m.group(4));
            }
            return (long) (value * multiplier);
        }
        if (failOnError) {
            throw new IllegalArgumentException("Could not resolve \"" + bandwidthDesc + "\"");
        }
        log.error("Could not resolve \"{}\"", bandwidthDesc);
        return 0L;
    }

    private long determineMultiplier(String prefix, String unit) {
        if (prefix == null || unit == null) {
            return DATA.BIT;
        }

        long multiplier = Validate.notNull(MULTIPLIERS.get(prefix.toLowerCase()), "Invalid unit " + prefix + unit);
        if ("bytes".equalsIgnoreCase(unit) || "byte".equalsIgnoreCase(unit) || "B".equals(unit)) {
            return multiplier * DATA.BYTE;
        } else {
            return multiplier;
        }
    }

    public static class Bandwidth implements TypeAdapterFactory {

        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return (TypeAdapter<T>) new DataFieldAdapter(PS, true);
        }
    }

    public static class Size implements TypeAdapterFactory {

        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return (TypeAdapter<T>) new DataFieldAdapter(true);
        }
    }

    public static class SizeQuiet implements TypeAdapterFactory {

        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return (TypeAdapter<T>) new DataFieldAdapter(false);
        }
    }

    public static class BandwidthQuiet implements TypeAdapterFactory {

        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return (TypeAdapter<T>) new DataFieldAdapter(PS, false);
        }
    }

}

