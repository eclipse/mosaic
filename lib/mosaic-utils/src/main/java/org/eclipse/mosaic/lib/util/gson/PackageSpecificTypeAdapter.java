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

import java.util.LinkedList;
import java.util.List;

public class PackageSpecificTypeAdapter<T> extends AbstractTypeAdapterFactory<T> {
    private final List<String> packageNames = new LinkedList<>();

    public PackageSpecificTypeAdapter(TypeAdapterFactory parentFactory, Gson gson) {
        super(parentFactory, gson);
    }

    public PackageSpecificTypeAdapter<T> searchInPackageOfClass(Class<?> clazz) {
        return searchInPackage(clazz.getPackage());
    }

    public PackageSpecificTypeAdapter<T> searchInPackage(Package searchPackage) {
        return searchInPackage(searchPackage.getName());
    }

    public PackageSpecificTypeAdapter<T> searchInPackage(String packageName) {
        packageNames.add(packageName);
        return this;
    }

    @Override
    protected Class<?> fromTypeName(String type) {
        Class<?> returnClass = null;
        for (String searchPackage : packageNames) {
            try {
                returnClass = Class.forName(searchPackage + "." + type);
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