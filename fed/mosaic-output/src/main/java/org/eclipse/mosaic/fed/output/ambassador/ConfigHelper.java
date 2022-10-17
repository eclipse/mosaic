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

package org.eclipse.mosaic.fed.output.ambassador;

import org.eclipse.mosaic.rti.TIME;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * This class is intended to contain helper methods to read the output generator configuration file.
 */
public class ConfigHelper {

    private static final int DEFAULT_UPDATE_INTERVAL = 1;
    private static final int DEFAULT_HANDLE_START_TIME = 0;
    private static final int DEFAULT_HANDLE_END_TIME = Integer.MAX_VALUE;

    /**
     * Load attribute "id" from the given configuration.
     *
     * @param config configuration
     * @return `id` sent in the configuration
     */
    public static String getId(HierarchicalConfiguration<?> config) throws IllegalArgumentException {
        String identifier = config.getString("[@id]");
        Validate.isTrue(StringUtils.isNotBlank(identifier), "Mandatory attribute 'id' not given");
        return identifier;
    }

    /**
     * Load attribute "enabled". If not given, return true by default.
     *
     * @param config configuration
     * @return true, if enabled.
     */
    public static boolean isEnabled(HierarchicalConfiguration<?> config) {
        return config.getBoolean("[@enabled]", true);
    }

    /**
     * Load attribute "update". If not given, return defaultUpdate of 1
     * If the given update is invalid, an exception is thrown
     *
     * @param config configuration
     * @return the update interval as defined in the configuration
     * @throws Exception If less than or equal to 0
     */
    public static int getUpdateInterval(HierarchicalConfiguration<?> config) throws Exception {
        int updateInterval = config.getInt("[@update]", DEFAULT_UPDATE_INTERVAL);
        if (updateInterval < 0) {
            throw new Exception("The overwriting update value couldn't be a non-positive value.");
        }
        return updateInterval;
    }

    /**
     * Load attribute "start" using helper function {@link #loadAndCheckHandleStartOrEndTime(HierarchicalConfiguration, boolean)}.
     * If not given return default value of 0.
     * Throws exception if given start value is smaller than 0 or larger than given end value.
     *
     * @param config configuration
     * @return start time
     */
    public static long getHandleStartTime(HierarchicalConfiguration<?> config) throws Exception {
        return loadAndCheckHandleStartOrEndTime(config, true);
    }

    /**
     * Load attribute "end" using helper function {@link #loadAndCheckHandleStartOrEndTime(HierarchicalConfiguration, boolean)}.
     * If not given return default value of Integer.MAX_VALUE.
     * Throws exception if given end value is smaller than 0 or smaller than given start value.
     *
     * @param config configuration
     * @return end time
     */
    public static long getHandleEndTime(HierarchicalConfiguration<?> config) throws Exception {
        return loadAndCheckHandleStartOrEndTime(config, false);
    }

    /**
     * Helper function for loading "start" and "end" attributes.
     *
     * @param config   configuration
     * @param getStart true if [@start] field is wanted, false if [@end] field is wanted
     */
    private static long loadAndCheckHandleStartOrEndTime(HierarchicalConfiguration<?> config, boolean getStart) throws Exception {
        long handleStartTime = (long) config.getInt("[@start]", DEFAULT_HANDLE_START_TIME) * TIME.SECOND;
        long handleEndTime = (long) config.getInt("[@end]", DEFAULT_HANDLE_END_TIME) * TIME.SECOND;

        // only check value if it's wanted
        if (getStart && handleStartTime < 0) {
            throw new Exception("The value for start time can't be negative.");
        }
        if (!getStart && handleEndTime < 0) {
            // HINT: this is never thrown, since there will be a prior Exception from start time check
            // ex1: start = 0, end = -1 --> start > end
            // ex2: start = -2, end = -1 --> start < 0
            throw new Exception("The value for end time can't be negative.");
        }

        if (handleStartTime > handleEndTime) {
            throw new Exception("The value for start time can't be higher than the value for end time.");
        }

        return getStart ? handleStartTime : handleEndTime;
    }

    public static String getConfigLoader(HierarchicalConfiguration<?> config) {
        String configLoader = config.getString("[@loader]");
        Validate.isTrue(StringUtils.isNotBlank(configLoader), "Mandatory attribute 'loader' not given.");
        return configLoader;
    }

    public static Collection<String> getSubscriptions(HierarchicalConfiguration<ImmutableNode> config) {
        final Collection<String> messageTypes = new HashSet<>();
        List<HierarchicalConfiguration<ImmutableNode>> configuredMessageTypes = config.configurationsAt("subscriptions.subscription");
        for (HierarchicalConfiguration<ImmutableNode> configuredMessageType : configuredMessageTypes) {
            if (configuredMessageType.getBoolean("[@enabled]", true)) {
                messageTypes.add(configuredMessageType.getString("[@id]"));
            }
        }
        return messageTypes;
    }
}
