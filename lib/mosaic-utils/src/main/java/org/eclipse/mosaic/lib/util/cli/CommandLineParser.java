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

package org.eclipse.mosaic.lib.util.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Helper class, which automatically converts a list of arguments from the command line into a parameter object and vice versa.
 *
 * @param <T> class of the parameter object, which later holds the parsed parameter values
 */
public class CommandLineParser<T> {

    private final static Logger log = LoggerFactory.getLogger(CommandLineParser.class);

    private final org.apache.commons.cli.CommandLineParser parser = new PosixParser();
    private final Options options = new Options();
    private final List<Field> parameterFields;

    private String usageHint = " ";
    private String header = "Accepts the following arguments:\n";
    private String footer;

    /**
     * Constructs a new CLIParser with a class of the parameter object, which later holds the
     * parameter values. This must also declare all command line related properties, such
     * as the name of the option and its description. For this, the fields in the given class
     * need to be public and need to be annotated with {@link CommandLineOption}. Everything else
     * is done by this parser.
     */
    public CommandLineParser(final Class<T> parameterClass) {
        parameterFields = new LinkedList<>();

        options.addOption("h", "help", false, "Prints this help screen.\n");

        Map<String, OptionGroup> optionGroups = new HashMap<>();

        // build Options out of declared fields of the parameter object
        for (Field field : FieldUtils.getAllFields(parameterClass)) {
            if (field.isAnnotationPresent(CommandLineOption.class)) {
                final CommandLineOption cliAnnotation = field.getAnnotation(CommandLineOption.class);

                final Option option =
                        new Option(StringUtils.defaultIfBlank(cliAnnotation.shortOption(), null), cliAnnotation.description());
                option.setLongOpt(cliAnnotation.longOption());
                if (StringUtils.isNotEmpty(cliAnnotation.argName())) {
                    option.setArgs(1);
                    option.setArgName(cliAnnotation.argName());
                }

                if (StringUtils.isNotBlank(cliAnnotation.group())) {
                    OptionGroup optionGroup = optionGroups
                            .computeIfAbsent(cliAnnotation.group().trim(), (k) -> new OptionGroup());
                    optionGroup.addOption(option);
                    if (cliAnnotation.isRequired()) {
                        optionGroup.setRequired(true);
                    }
                } else {
                    if (cliAnnotation.isRequired()) {
                        option.setRequired(true);
                    }
                    options.addOption(option);
                }
                parameterFields.add(field);
            }
        }
        optionGroups.values().stream().forEach(options::addOptionGroup);
    }

    /**
     * Parses a list of arguments (POSIX style) and writes the set values into the given parameter object.
     * The parameter object should be of the same class as this parser is initialized with.
     */
    public final T parseArguments(final String[] args, final T parameters) throws ParseException {
        if (args.length == 0 || ArrayUtils.contains(args, "--help") || ArrayUtils.contains(args, "-h")) {
            printHelp();
            return null;
        }

        final List<String> argumentsToParse = filterSystemProperties(args);

        // parse command line
        final CommandLine line = parser.parse(options, argumentsToParse.toArray(new String[0]));

        // write option values into parameter object
        for (Field field : parameterFields) {
            final CommandLineOption cliAnnotation = field.getAnnotation(CommandLineOption.class);
            field.setAccessible(true);

            if (!line.hasOption(cliAnnotation.longOption())) {
                continue;
            }

            try {
                if (boolean.class.isAssignableFrom(field.getType())) {
                    field.set(parameters, true);
                } else if (double.class.isAssignableFrom(field.getType()) || Double.class.isAssignableFrom(field.getType())) {
                    field.set(parameters, Double.parseDouble(line.getOptionValue(cliAnnotation.longOption())));
                } else if (float.class.isAssignableFrom(field.getType()) || Float.class.isAssignableFrom(field.getType())) {
                    field.set(parameters, Float.parseFloat(line.getOptionValue(cliAnnotation.longOption())));
                } else if (int.class.isAssignableFrom(field.getType()) || Integer.class.isAssignableFrom(field.getType())) {
                    field.set(parameters, Integer.parseInt(line.getOptionValue(cliAnnotation.longOption())));
                } else if (long.class.isAssignableFrom(field.getType()) || Long.class.isAssignableFrom(field.getType())) {
                    field.set(parameters, Long.parseLong(line.getOptionValue(cliAnnotation.longOption())));
                } else if (File.class.isAssignableFrom(field.getType())) {
                    field.set(parameters, new File(line.getOptionValue(cliAnnotation.longOption())));
                } else if (List.class.isAssignableFrom(field.getType())) {
                    field.set(parameters, Arrays.asList(line.getOptionValues(cliAnnotation.longOption())));
                } else {
                    field.set(parameters, line.getOptionValue(cliAnnotation.longOption()));
                }
            } catch (Throwable e) {
                throw new ParseException("Could not set field " + field.getName() + ": " + e.getLocalizedMessage());
            }
        }

        return parameters;
    }

    private List<String> filterSystemProperties(String[] args) {
        final List<String> argumentsToParse = new ArrayList<>();

        for (String arg : args) {
            if (arg.startsWith("-D") && arg.contains("=")) {
                String[] systemProperty = arg.substring(2).split("=");
                System.setProperty(systemProperty[0], systemProperty[1]);
            } else {
                argumentsToParse.add(arg);
            }
        }
        return argumentsToParse;
    }

    /**
     * Transforms the object, which holds the parameter values into a list of arguments, which can be
     * used to start MOSAIC processes with valid arguments.
     *
     * @throws UnsupportedOperationException if parameter is not in the list of supported arguments
     * @throws RuntimeException              if parameter could not be parsed
     */
    public final List<String> transformToArguments(final T parameters) {
        final List<String> arguments = new LinkedList<>();

        try {
            for (Field field : parameterFields) {
                final CommandLineOption cliAnnotation = field.getAnnotation(CommandLineOption.class);

                if (boolean.class.isAssignableFrom(field.getType())) {
                    if (field.getBoolean(parameters)) {
                        arguments.add("--" + cliAnnotation.longOption());
                    }
                } else if (String.class.isAssignableFrom(field.getType())) {
                    if (field.get(parameters) != null) {
                        arguments.add("--" + cliAnnotation.longOption());
                        arguments.add((String) field.get(parameters));
                    }
                } else if (File.class.isAssignableFrom(field.getType())) {
                    if (field.get(parameters) != null) {
                        arguments.add("--" + cliAnnotation.longOption());
                        arguments.add(((File) field.get(parameters)).getAbsolutePath());
                    }
                } else {
                    throw new UnsupportedOperationException(
                            String.format("Could not transform %s to argument. Unsupported type.", field.getName())
                    );
                }

            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Could not transform parameters to arguments", e);
        }
        return arguments;
    }

    /**
     * Prints the help.
     *
     * @param printWriter writer to output help to
     */
    public void printHelp(PrintWriter printWriter) {
        HelpFormatter helpFormatter = new HelpFormatter();

        List<String> ordering = new LinkedList<>();
        for (Field field : parameterFields) {
            final CommandLineOption cliAnnotation = field.getAnnotation(CommandLineOption.class);
            ordering.add(cliAnnotation.longOption());
        }

        helpFormatter.setOptionComparator(Comparator.<Option>comparingInt(o -> ordering.indexOf(o.getLongOpt())));
        helpFormatter.printHelp(printWriter, 120, usageHint, header, getOptions(), 1, 3, footer);
    }

    public void printHelp() {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
        printHelp(pw);
        pw.flush();
    }

    /**
     * This method provides all options declared in the parameter object this parser has been initialized with.
     */
    public final Options getOptions() {
        return this.options;
    }

    /**
     * This method is used to define a usage hint for the respective {@link CommandLineParser}.
     *
     * @param usageHint the hint to be set
     * @param header    header for the hint
     * @param footer    footer for the hint
     * @return the object to chain further methods
     */
    public CommandLineParser<T> usageHint(String usageHint, String header, String footer) {
        this.header = ObjectUtils.defaultIfNull(header, this.header);
        this.footer = ObjectUtils.defaultIfNull(footer, this.footer);
        this.usageHint = Validate.notNull(usageHint);
        return this;
    }
}
