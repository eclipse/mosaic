/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

package com.csvreader;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Replaces com.csvreader.CsvReader from javacsv with an implementation which uses CSVReader from opencsv.
 * <br><br>
 * By excluding javacsv jar and providing this class with the same full qualified name and same API as a supplement
 * we are able to use opencsv instead without touching the code of graphhopper-reader-gtfs.
 * Therefore, even though the IDE thinks this class is unused, it is actually used by graphhopper-reader-gtfs.
 */ //TODO: remove this once the PR https://github.com/graphhopper/graphhopper/pull/3084 got accepted and we upgrade graphhopper
public class CsvReader {

    private final CSVReader openCsvReader;
    private final Map<String, Integer> headerIndex = new HashMap<>();

    private String[] currenRecord;

    public CsvReader(InputStream inputStream, char delimiter, Charset charset) {
        this(new InputStreamReader(inputStream, charset), delimiter);
    }

    public CsvReader(Reader openCsvReader) {
        this(openCsvReader, ',');
    }

    public CsvReader(Reader openCsvReader, char delimiter) {
        this.openCsvReader = new CSVReaderBuilder(openCsvReader)
                .withCSVParser(new CSVParserBuilder().withSeparator(delimiter).build())
                .build();
    }

    public boolean readRecord() {
        try {
            do {
                currenRecord = this.openCsvReader.readNextSilently();
                if (currenRecord == null) {
                    return false;
                }
            } while (currenRecord.length == 1 && StringUtils.isEmpty(currenRecord[0]));
            return true;
        } catch (IOException e) {
            currenRecord = null;
            return false;
        }
    }

    public boolean readHeaders() {
        Validate.isTrue(currenRecord == null, "Reader has already been used.");
        if (readRecord()) {
            setHeaders(currenRecord);
            return true;
        }
        return false;
    }

    public void setHeaders(String[] strings) {
        int index = 0;
        for (String entry : strings) {
            headerIndex.put(entry, index++);
        }
    }

    public String get(String column) {
        Validate.isTrue(!headerIndex.isEmpty(), "No header defined.");
        final Integer index = headerIndex.get(column);
        return index != null && index < currenRecord.length
                ? currenRecord[index]
                : "";
    }
}
