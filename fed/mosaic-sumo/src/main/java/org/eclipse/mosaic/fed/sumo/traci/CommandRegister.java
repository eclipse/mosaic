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

package org.eclipse.mosaic.fed.sumo.traci;

import java.util.HashMap;
import java.util.Map;

public class CommandRegister {

    private final Map<Class<?>, AbstractTraciCommand<?>> commands = new HashMap<>();

    /**
     * Stores a TraCI command once it is created returns only one instance per command class.
     *
     * @param commandClass the command class wanted.
     * @return the command instance.
     * @throws IllegalArgumentException if the given class could not be instantiated.
     */
    public <T extends AbstractTraciCommand<?>> T getOrCreate(Class<T> commandClass) {
        T command = (T) commands.get(commandClass);
        if (command == null) {
            try {
                command = commandClass.newInstance();
                register(commandClass, command);
            } catch (Exception e) {
                throw new IllegalArgumentException("No default constructor accessible in command.", e);
            }
        }
        return command;
    }

    /**
     * Pre-register commands.
     */
    public <T extends AbstractTraciCommand<?>> T register(Class<? extends T> clazz, T command) {
        commands.put(clazz, command);
        return command;
    }
}
