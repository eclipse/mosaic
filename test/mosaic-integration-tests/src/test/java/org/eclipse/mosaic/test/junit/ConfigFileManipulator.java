/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.test.junit;

import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;

import com.google.gson.GsonBuilder;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang3.Validate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * This manipulator allows to change the contents of an arbitrary JSON based config file inside the scenario.
 */
public class ConfigFileManipulator<T> implements Consumer<Path> {

    private final String configFile;
    private final Class<T> configClass;
    private final Consumer<T> manipulator;

    public ConfigFileManipulator(String configFile, Class<T> configClass, Consumer<T> manipulator) {
        this.configFile = configFile;
        this.manipulator = manipulator;
        this.configClass = configClass;
    }

    @Override
    public void accept(Path path) {
        Path configPath = path.resolve(configFile);
        Validate.isTrue(Files.exists(configPath), configFile + " does not exist in " + path);

        try {
            T configFile = new ObjectInstantiation<T>(configClass).readFile(configPath.toFile());
            manipulator.accept(configFile);
            try (Writer w = new OutputStreamWriter(new FileOutputStream(configPath.toFile()))) {
                new GsonBuilder().setPrettyPrinting().create().toJson(configFile, w);
            }
        } catch (InstantiationException | IOException e) {
            throw new AssertionFailedError("Could not load or write " + configFile);
        }
    }
}
