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

import org.eclipse.mosaic.rti.api.RtiAmbassador;

import ch.qos.logback.classic.LoggerContext;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;

/**
 * A template class for runtime loadable output generators config classes, also serves as a factory
 * for creating actual {@link AbstractOutputGenerator} instances).
 */
public abstract class OutputGeneratorLoader {

    private String id;
    private RtiAmbassador rti;
    private int updateInterval;
    private long handleStartTime;
    private long handleEndTime;
    private Collection<String> interactionTypes;
    private File configurationDirectory;

    /**
     * this method is called just after a new instance of a derived output generator config was created.
     * subclasses should call this method at first, and then proceed with reading custom parameters from the configuration
     *
     * @param rti                    the {@link RtiAmbassador} of the federation
     * @param config                 output generator configuration
     * @param configurationDirectory output generator configuration directory path
     */
    public void initialize(RtiAmbassador rti, HierarchicalConfiguration<ImmutableNode> config, File configurationDirectory) throws Exception {
        this.rti = rti;
        this.id = ConfigHelper.getId(config);
        this.updateInterval = ConfigHelper.getUpdateInterval(config);
        this.handleStartTime = ConfigHelper.getHandleStartTime(config);
        this.handleEndTime = ConfigHelper.getHandleEndTime(config);
        this.interactionTypes = ConfigHelper.getSubscriptions(config);
        this.configurationDirectory = configurationDirectory;
    }


    /**
     * Returns the output generator identifier.
     *
     * @return output generator identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the {@link RtiAmbassador} of the federation for additional interaction with RTI.
     *
     * @return the {@link RtiAmbassador} of the federation.
     */
    protected RtiAmbassador getRti() {
        return rti;
    }

    /**
     * Returns the update interval.
     *
     * @return update interval of the output generator in seconds
     */
    public int getUpdateIntervalInSeconds() {
        return updateInterval;
    }

    /**
     * Returns the start time of the output generator.
     *
     * @return start time of the output generator
     */
    public long getHandleStartTime() {
        return handleStartTime;
    }

    /**
     * Returns the end time of the output generator.
     *
     * @return end time of the output generator
     */
    public long getHandleEndTime() {
        return handleEndTime;
    }

    /**
     * Returns a collection of supported interaction types.
     *
     * @return collection of supported interaction types.
     */
    public Collection<String> getInteractionTypes() {
        return interactionTypes;
    }

    /**
     * Returns the path to the directory of the output generator.
     *
     * @return path to the directory of the output generator
     */
    public File getConfigurationDirectory() {
        return configurationDirectory;
    }

    /**
     * Factory method for creating actual output generator.
     *
     * @return the actual output generator
     */
    public abstract AbstractOutputGenerator createOutputGenerator() throws Exception;

    /**
     * Returns the path of the output directory for logging as a string.
     *
     * @return path of the output directory
     */
    public static String loggerDirectory() {
        return ((LoggerContext) LoggerFactory.getILoggerFactory()).getProperty("logDirectory");
    }

}
