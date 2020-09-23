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
 */

package org.eclipse.mosaic.fed.application.app;

import org.eclipse.mosaic.fed.application.app.api.os.OperatingSystem;
import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import javax.annotation.Nonnull;

/**
 * An extended {@link AbstractApplication}, which automatically loads a configuration from a json file,
 * which is called the same as the implementing class (e.g. {@code MyVehicleApplication.json},
 * if <code>MyVehicleApplication extends ConfigurableApplication</code>).
 */
@SuppressWarnings("checkstyle:ClassTypeParameterName")
public abstract class ConfigurableApplication<ConfigT, OS extends OperatingSystem> extends AbstractApplication<OS> {

    private final Class<? extends ConfigT> configClass;
    private final String configFileName;

    private ConfigT configuration;

    /**
     * Calls the constructor for {@link ConfigurableApplication},
     * without a configuration file.
     *
     * @param configClass The class of the config.
     */
    public ConfigurableApplication(Class<? extends ConfigT> configClass) {
        this(configClass, null);
    }

    /**
     * The constructor for {@link ConfigurableApplication}.
     *
     * @param configClass           The class of the config.
     * @param configurationFileName The name identifying the configuration file.
     */
    public ConfigurableApplication(Class<? extends ConfigT> configClass, String configurationFileName) {
        this.configClass = configClass;
        this.configFileName = ObjectUtils.defaultIfNull(configurationFileName, this.getClass().getSimpleName());
    }

    @Nonnull
    public final ConfigT getConfiguration() {
        if (configuration == null) {
            try {
                File configFile = new File(getOs().getConfigurationPath(), String.format("%s_%s.json", configFileName, this.getOs().getId()));
                if (!configFile.exists()) {
                    configFile = new File(getOs().getConfigurationPath(), configFileName + ".json");
                }

                configuration = new ObjectInstantiation<>(configClass, getLog())
                        .readFile(configFile);
            } catch (InstantiationException e) {
                throw new RuntimeException("Could not instantiate " + configClass, e);
            }
        }
        return Validate.notNull(configuration, "No configuration found.");
    }
}
