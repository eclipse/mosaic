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
package org.eclipse.mosaic.fed.sumo.bridge;


import org.eclipse.mosaic.fed.sumo.config.CSumo;

import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.Map;

public class CommandRegister {
    private final Map<Class<?>, Object> commands = new HashMap<>();
    private final CSumo sumoConfiguration;
    private final String implPackage;

    private Bridge bridge;

    public CommandRegister(CSumo sumoConfiguration, String implPackage) {
        this.sumoConfiguration = sumoConfiguration;
        this.implPackage = implPackage;
    }

    public void setBridge(Bridge bridge) {
        this.bridge = bridge;
    }

    /**
     * Stores a TraCI command once it is created returns only one instance per command class.
     *
     * @param commandClass the command class wanted.
     * @return the command instance.
     * @throws IllegalArgumentException if the given class could not be instantiated.
     */
    public <T extends Object> T getOrCreate(Class<T> commandClass) {
        T command = (T) commands.get(commandClass);
        if (command == null) {
            try {
                Class<? extends T> implementationClass = Validate.notNull(
                        getImplementationClass(commandClass), "Could not find a implementation for command " + commandClass.getSimpleName()
                );
                try {
                    command = implementationClass.getDeclaredConstructor(Bridge.class, CSumo.class).newInstance(bridge, sumoConfiguration);
                } catch (Exception e1) {
                    try {
                        command = implementationClass.getDeclaredConstructor(Bridge.class).newInstance(bridge);
                    } catch (Exception e2) {
                        command = implementationClass.newInstance();
                    }
                }
                register(commandClass, command);
            } catch (Exception e) {
                throw new IllegalArgumentException("No valid command implementation for " + commandClass.getSimpleName(), e);
            }
        }
        return command;
    }

    private <T> Class<? extends T> getImplementationClass(Class<T> commandClass) {
        String className = commandClass.getCanonicalName();
        if (className.contains(".api.")) {
            String newClassName = className.replace(".api.",
                    implPackage == null
                            ? "."
                            : "." + implPackage + "."
            );
            try {
                return (Class<T>) Class.forName(newClassName);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Pre-register commands.
     */
    public <T extends Object> T register(Class<? extends T> clazz, T command) {
        commands.put(clazz, command);
        return command;
    }
}
