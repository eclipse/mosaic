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

package org.eclipse.mosaic.lib.database.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;

/**
 * This is a detector for files of version 3.
 */
public class SQLiteTypeDetector extends FileTypeDetector {

    public final static String MIME_TYPE = "application/x-sqlite3";

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final static String forComparison = "SQLite format 3";

    @Override
    public String probeContentType(Path path) throws IOException {
        File file = path.toFile();

        // failsafe for basic access
        if (!file.exists()) {
            log.debug("file '{}': does not exist, invalid path set?", file);
            return null;
        }

        if (!file.canRead()) {
            log.debug("file '{}': cannot read file", file);
            return null;
        }

        if (file.length() < forComparison.length()) {
            log.debug("file '{}': file size indicates it can't be a sqlite file", file.getAbsolutePath());
            return null;
        }

        // prepare access
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            // read in the first identLength chars
            int counter = 0;
            StringBuilder toCheck = new StringBuilder();
            while (reader.ready() && (counter++ < forComparison.length())) {
                toCheck.append((char) reader.read());
            }

            // check if that chars equal forComparison
            if (forComparison.equals(toCheck.toString())) {
                return MIME_TYPE;
            }

            log.debug("file '{}': didn't contain the check string at the beginning", file.getAbsolutePath());
        }

        return null;
    }

}
