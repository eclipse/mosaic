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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class FileUtils {

    private static final int MAX_DEPTH = 256;

    private FileUtils() {
        // static method only
    }

    public static Collection<File> searchForFiles(File rootFile, Predicate<File> acceptPredicate) {
        return searchForFiles(rootFile, acceptPredicate, MAX_DEPTH);
    }

    public static Collection<File> searchForFiles(File rootFile, Predicate<File> acceptPredicate, int maxDepth) {
        return searchForFiles(Collections.singleton(rootFile), acceptPredicate, maxDepth);
    }

    public static Collection<File> searchForFiles(Collection<File> rootFiles, Predicate<File> acceptPredicate) {
        return searchForFiles(rootFiles, acceptPredicate, MAX_DEPTH);
    }

    public static Collection<File> searchForFiles(Collection<File> rootFiles, Predicate<File> acceptPredicate, int maxDepth) {
        Set<File> searchSet = new HashSet<>(rootFiles);

        final Set<File> matchingSet = new HashSet<>();
        while (maxDepth-- > 0) {
            if (searchSet.size() > 0) {
                Set<File> newDirectorySet = new HashSet<>();
                for (File f : searchSet) {
                    if (f.canRead() && acceptPredicate.test(f)) {
                        if (f.isDirectory()) {
                            File[] listFiles = f.listFiles();
                            if (listFiles != null) {
                                newDirectorySet.addAll(Arrays.asList(listFiles));
                            }
                        } else if (f.isFile()) {
                            matchingSet.add(f);
                        }
                    }
                }
                searchSet = newDirectorySet;
            } else {
                return matchingSet;
            }
        }
        return matchingSet;
    }

    public static Collection<File> searchForFilesOfType(File rootFile, String fileEnding) {
        return searchForFilesOfType(Collections.singleton(rootFile), fileEnding);
    }

    public static Collection<File> searchForFilesOfType(Collection<File> rootFiles, String fileEnding) {
        final String fileEndingForSearch = fileEnding.startsWith(".")
                ? fileEnding.toLowerCase()
                : "." + fileEnding.toLowerCase();
        return searchForFiles(rootFiles, f ->
                f.canRead() && (f.isDirectory() || (f.isFile() && f.getName().toLowerCase().endsWith(fileEndingForSearch)))
        );
    }

    public static Collection<File> searchForFilesWithName(File rootFile, String fileName) {
        return searchForFilesWithName(Collections.singleton(rootFile), fileName);
    }

    public static Collection<File> searchForFilesWithName(Collection<File> rootFiles, String fileName) {
        return searchForFiles(rootFiles, f ->
                f.canRead() && (f.isDirectory() || (f.isFile() && f.getName().toLowerCase().equals(fileName.toLowerCase())))
        );
    }
}
