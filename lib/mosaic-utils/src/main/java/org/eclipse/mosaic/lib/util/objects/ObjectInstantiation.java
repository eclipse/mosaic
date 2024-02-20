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

package org.eclipse.mosaic.lib.util.objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import javax.annotation.Nonnull;

/**
 * Instantiate Java objects using a file contains a JSON encoded object.
 */
public class ObjectInstantiation<T> {

    private final static String NEWLINE = System.lineSeparator();

    /**
     * The class to instantiate.
     */
    private final Class<T> clazz;

    private final Logger logger;

    /**
     * Creates a new {@link ObjectInstantiation} which creates an object based on the given
     * {@link Class}. No debug information or warnings are logged during object instantiation.
     *
     * @param clazz The class to instantiate.
     */
    public ObjectInstantiation(Class<T> clazz) {
        this(clazz, null);
    }

    /**
     * Creates a new {@link ObjectInstantiation} which creates an object based on the given
     * {@link Class}. Debug information and warnings are logged to the given {@link Logger} during
     * object instantiation.
     *
     * @param clazz  The class to instantiate.
     * @param logger The logger which is used to log debug information and warnings.
     */
    public ObjectInstantiation(Class<T> clazz, Logger logger) {
        this.clazz = clazz;
        this.logger = logger;
    }

    /**
     * Read the file. If no file was found, the default constructor should be used.
     *
     * @param fileHandle The file to read.
     * @return The created object.
     * @throws InstantiationException The object could not be instantiated.
     */
    public T readFile(File fileHandle) throws InstantiationException {
        return readFile(fileHandle, new GsonBuilder());
    }

    /**
     * Read the file. If no file was found, the default constructor should be used.
     *
     * @param fileHandle  The file to read.
     * @param gsonBuilder the GsonBuilder to use
     * @return The created object.
     * @throws InstantiationException The object could not be instantiated.
     */
    public T readFile(File fileHandle, @Nonnull GsonBuilder gsonBuilder) throws InstantiationException {
        if (!fileHandle.exists()) {
            warn("The file {} does not exist.", fileHandle.getPath());
        } else if (!fileHandle.canRead()) {
            warn("The file {} could not be read.", fileHandle.getPath());
        } else {
            try (InputStream inputStream = new FileInputStream(fileHandle)) {
                return read(inputStream, gsonBuilder);
            } catch (IOException e) {
                warn("The file {} does not exist.", fileHandle.getPath());
            }
        }
        return createWithDefaultDefaultConstructor();
    }

    /**
     * Read the file. If no file was found, the default constructor should be used.
     *
     * @return The created object.
     * @throws InstantiationException The object could not be instantiated.
     */
    public T read(InputStream inputStream) throws InstantiationException {
        return read(inputStream, new GsonBuilder());
    }

    /**
     * Read the file. If no file was found, the default constructor should be used.
     *
     * @param inputStream The input stream to read.
     * @param gsonBuilder the GsonBuilder to use
     * @return The created object.
     * @throws InstantiationException The object could not be instantiated.
     */
    public T read(InputStream inputStream, @Nonnull GsonBuilder gsonBuilder) throws InstantiationException {
        if (inputStream == null) {
            warn("No input data available.");
            return createWithDefaultDefaultConstructor();
        }

        final ByteArrayOutputStream copyStream = new ByteArrayOutputStream();
        try {
            copyStream(inputStream, copyStream);
        } catch (IOException e) {
            warn("Could not read data from file.");
        }
        byte[] jsonAsByteArray = copyStream.toByteArray();

        InputStream streamScheme = clazz.getClassLoader().getResourceAsStream(clazz.getSimpleName() + "Scheme.json");
        if (streamScheme != null) {
            validateFile(new ByteArrayInputStream(jsonAsByteArray), streamScheme);
        }

        final Gson gson = gsonBuilder.create();

        T obj = null;
        //get the Json from the File
        try (final Reader reader = new InputStreamReader(new ByteArrayInputStream(jsonAsByteArray), StandardCharsets.UTF_8)) {
            final JsonReader readerJson = new JsonReader(reader);
            obj = gson.fromJson(readerJson, clazz);
        } catch (IOException e) {
            warn("Could not read JSON data from file.");
        }

        obj = handleParserResult(obj);

        appendObjectToLog(gsonBuilder, obj);

        return obj;
    }

    private T handleParserResult(T obj) throws InstantiationException {
        if (obj != null) {
            debug("File has been loaded into the destination object.");
        } else {
            obj = createWithDefaultDefaultConstructor();
            if (obj == null) {
                throw new InstantiationException("Could not read or instantiate the object.");
            }
        }
        return obj;
    }

    private void appendObjectToLog(GsonBuilder gsonBuilder, T obj) {
        if (logger == null) {
            return;
        }

        final Gson gsonPretty = gsonBuilder.setPrettyPrinting().create();

        String prettyString = shorten(gsonPretty.toJson(obj), 5000);

        String logResult = "The following object was created "
                + "(This is the created, internal JSON data object "
                + "and might differ from the initial JSON-String read):"
                + NEWLINE
                + prettyString;
        debug(logResult);
    }

    private T createWithDefaultDefaultConstructor() throws InstantiationException {
        debug("Try to instantiate using the default constructor.");
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            debug("Object instantiated using the default constructor.");
            return obj;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {

            throw new InstantiationException(e.getMessage());
        }
    }

    private String shorten(String json, int maxLength) {
        if (json.length() < maxLength) {
            return json;
        }
        boolean containsLineseperator = json.contains("\n");
        return new StringBuilder()
                .append(json.substring(0, maxLength - 100))
                .append(containsLineseperator ? NEWLINE : "")
                .append(" ... ")
                .append(containsLineseperator ? NEWLINE : "")
                .append(json.substring(json.length() - 100))
                .toString();
    }

    private void validateFile(InputStream input, InputStream schemaInput) throws InstantiationException {
        final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        final JsonSchema jsonSchema = factory.getSchema(schemaInput);
        final Collection<ValidationMessage> problems;
        try {
            problems = new HashSet<>(jsonSchema.validate(new ObjectMapper().readTree(input)));
        } catch (IOException e) {
            throw new InstantiationException("The input JSON is not valid: " + e.getMessage());
        }

        if (!problems.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();
            problems.forEach((p) -> {
                errorMessage.append(p);
                errorMessage.append(NEWLINE);
            });
            throw new InstantiationException("The " + clazz.getSimpleName() + " config is not valid: " + errorMessage);
        }
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int c;
        while ((c = in.read(buffer)) != -1) {
            out.write(buffer, 0, c);
        }
    }

    private void debug(String msg, Object... objects) {
        if (logger == null) {
            return;
        }
        logger.debug(msg, objects);
    }

    private void warn(String msg, Object... objects) {
        if (logger == null) {
            return;
        }
        logger.warn(msg, objects);
    }

}
