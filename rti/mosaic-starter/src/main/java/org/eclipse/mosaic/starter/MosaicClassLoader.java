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

package org.eclipse.mosaic.starter;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;

/**
 * This {@link ClassLoader} allows to load classes from a user specified location. Classes are located
 * in JAR files (use {@link MosaicClassLoader#includeJarFiles(Path)}). All JAR files are collected
 * based on the given root directory and added to this class loader.
 */
public class MosaicClassLoader extends URLClassLoader {

    protected MosaicClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    /**
     * Creates a class loader which is able to load compiled classes from the JAR files present in the
     * library ({@code libs}) directory of the MOSAIC bundle.
     *
     * @return the ClassLoader which is able to load all compiled classes from the MOSAIC module tree
     */
    public static ClassLoader includeJarFiles(Path libraryPath) {
        return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> {
            if (libraryPath != null) {
                return new MosaicClassLoader(collectJarFiles(libraryPath), ClassLoader.getSystemClassLoader());
            }
            return new MosaicClassLoader(new URL[0], ClassLoader.getSystemClassLoader());
        });
    }

    private static URL[] collectJarFiles(Path basePath) {
        try {
            return Files.walk(basePath)
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().endsWith(".jar"))
                    .map(MosaicClassLoader::toUrl)
                    .filter(Objects::nonNull)
                    .toArray(URL[]::new);
        } catch (Exception e) {
            return new URL[0];
        }
    }

    private static URL toUrl(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
