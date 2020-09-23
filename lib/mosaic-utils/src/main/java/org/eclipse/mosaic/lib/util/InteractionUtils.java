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

package org.eclipse.mosaic.lib.util;

import static com.google.common.reflect.ClassPath.ClassInfo;
import static com.google.common.reflect.ClassPath.from;

import org.eclipse.mosaic.rti.api.Interaction;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InteractionUtils {

    private final static Logger log = LoggerFactory.getLogger(InteractionUtils.class);

    private static Map<String, Class<?>> supportedInteractions = new HashMap<>();

    /**
     * Helper method, which returns all classes which are supported to be visualized. A class
     * is supported, if it is within the (sub)package of \"org.eclipse.mosaic\" and is a subclass
     * of {@link Interaction}. The key of the returned map refers to the type id of each message. The
     * type id is extracted from the class' constant field TYPE_ID, if present. Otherwise, the type
     * id is equal to the simple name of the class.
     *
     * @return the map with all supported message classes
     */
    @SuppressWarnings({"unchecked", "UnstableApiUsage"})
    public static Map<String, Class<?>> getAllSupportedInteractions(String... allowedPackages) {
        if (supportedInteractions.isEmpty()) {

            try {
                ImmutableSet<ClassInfo> topLevelClassesRecursive = from(InteractionUtils.class.getClassLoader()).getAllClasses();
                for (ClassInfo info : topLevelClassesRecursive) {
                    if (info.getName().equals(Interaction.class.getName())) {
                        continue;
                    }
                    boolean isValidPackage = info.getName().startsWith("org.eclipse.mosaic");;
                    for (String allowedPackageName: allowedPackages) {
                        isValidPackage |= info.getName().startsWith(allowedPackageName);
                    }
                    if (!isValidPackage || info.getName().contains("ClientServerChannelProtos")) {
                        continue;
                    }
                    try {
                        Class<?> messageClass = Class.forName(info.getName());
                        if (Interaction.class.isAssignableFrom(messageClass)) {
                            String msgType = extractTypeId(messageClass)
                                    .orElse(Interaction.createTypeIdentifier((Class<? extends Interaction>) messageClass));
                            Class<?> knownClass = supportedInteractions.putIfAbsent(msgType, messageClass);
                            if (knownClass != null && knownClass != messageClass) {
                                log.warn("Ambiguous interaction type '{}'. Already registered with class {}", msgType, knownClass);
                            }
                        }
                    } catch (Throwable e) {
                        //nop
                    }
                }
            } catch (Exception e) {
                log.error("Could not generate list of supported interaction types", e);
            }
        }
        return supportedInteractions;
    }

    /**
     * Extracts the type ID from the interaction class.
     *
     * @param interactionClass the interaction class
     * @return the type ID read from the field TYPE_ID of the interaction
     */
    public static Optional<String> extractTypeId(Class<?> interactionClass) {
        try {
            if (Modifier.isAbstract(interactionClass.getModifiers())) {
                return Optional.empty();
            }
            Field typeIdField = interactionClass.getDeclaredField("TYPE_ID");
            Class<?> cl = typeIdField.getType();
            if (cl.equals(String.class)) {
                return Optional.of((String) typeIdField.get(null));
            }
        } catch (Throwable e) {
            log.warn("Could not extract field TYPE_ID of class {}", interactionClass.getName());
        }
        return Optional.empty();
    }
}
