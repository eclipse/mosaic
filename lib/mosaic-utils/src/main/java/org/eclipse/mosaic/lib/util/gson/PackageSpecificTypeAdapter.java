/*
 * Copyright (c) 2023 Fraunhofer FOKUS and others. All rights reserved.
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

import org.eclipse.mosaic.lib.gson.AbstractTypeAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapterFactory;
import org.apache.commons.lang3.Validate;

import java.util.LinkedList;
import java.util.List;

/**
 * This {@link TypeAdapterFactory} allows to create an object from JSON definition
 * based on a "type" attribute. According to the value of the value in the "type" attribute,
 * a class is loaded and an object of this class is instantiated. Since this explicitly works
 * with simple class names only, a search space of possible packages need to be defined, which
 * are used to resolve the actual class.
 */
public final class PackageSpecificTypeAdapter<T> extends AbstractTypeAdapterFactory<T> {

    /**
     * Holds all package names which are used to search for suitable classes to instantiate based on the given type name.
     */
    private final List<String> packageNames = new LinkedList<>();

    /**
     * The class loader to load the given class from.
     */
    private ClassLoader classLoader = PackageSpecificTypeAdapter.class.getClassLoader();

    public PackageSpecificTypeAdapter(TypeAdapterFactory parentFactory, Gson gson) {
        super(parentFactory, gson);
    }

    /**
     * Adds the package of the given class to the search space. All classes within this package
     * are candidates to be loaded by a given type name.
     */
    public PackageSpecificTypeAdapter<T> searchInPackageOfClass(Class<?> clazz) {
        return searchInPackage(clazz.getPackage());
    }

    /**
     * Adds the given package to the search space. All classes within this package
     * are candidates to be loaded by a given type name.
     */
    public PackageSpecificTypeAdapter<T> searchInPackage(Package searchPackage) {
        return searchInPackage(searchPackage.getName());
    }

    /**
     * Adds the given fully qualified package name to the search space. All classes
     * within the given package are candidates to be loaded by a given type name.
     */
    public PackageSpecificTypeAdapter<T> searchInPackage(String packageName) {
        packageNames.add(packageName);
        return this;
    }

    /**
     * Defines a specific class loader from which the searched classes are loaded.
     */
    public PackageSpecificTypeAdapter<T> withClassLoader(ClassLoader classLoader) {
        this.classLoader = Validate.notNull(classLoader, "Given class loader must not be null");
        return this;
    }

    @Override
    protected Class<?> fromTypeName(String type) {
        Class<?> returnClass = null;
        for (String searchPackage : packageNames) {
            try {
                returnClass = classLoader.loadClass(searchPackage + "." + type);
            } catch (ClassNotFoundException ignored) {
                // nop
            }
            if (returnClass != null) {
                break;
            }
        }
        if (returnClass != null) {
            return returnClass;
        } else {
            throw new JsonParseException("Unknown type name " + type);
        }
    }

    @Override
    protected String toTypeName(Class<?> typeClass) {
        return typeClass.getSimpleName();
    }
}