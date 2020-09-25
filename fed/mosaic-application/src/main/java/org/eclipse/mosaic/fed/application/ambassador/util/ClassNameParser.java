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

package org.eclipse.mosaic.fed.application.ambassador.util;

import org.eclipse.mosaic.fed.application.ambassador.ErrorRegister;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/**
 * This utility class parses a String and awaits at least a class reference.
 * The class reference may be followed by a pair of parenthesis that includes a list of parameters like:
 * com.some.package.name.ClassName(true, 12.45, \"some text\")
 * As a result an instance of the class `ClassName` will be created with the given parameter list.
 */
public class ClassNameParser {

    // ^([a-zA-Z_$][a-zA-Z\d\._$]*)(?:|\((.+)\))$
    private final static Pattern classPattern = Pattern.compile("^([a-zA-Z_$][a-zA-Z\\d\\._$]*)(?:|\\((.+)\\))$");
    // ^(\d+|\d+\.\d+|\"[^\"\n]*\"|false|true)$
    private final static Pattern parameterPattern =
            Pattern.compile("^(?:((?:\\d+\\.\\d+|\\d+d))|(\\d+l)|(\\d+)|\\\"([^\\\"\\n]*)\\\"|'([^'\\n]*)'|(false|true))$");

    private final Logger logger;
    private final ClassLoader urlClassLoader;

    /**
     * Constructor for {@link ClassNameParser} using default {@link ClassLoader}.
     *
     * @param logger the {@link Logger} for the class
     */
    public ClassNameParser(Logger logger) {
        this(logger, ClassNameParser.class.getClassLoader());
    }

    /**
     * Constructor for {@link ClassNameParser} using a specified {@link ClassLoader}.
     *
     * @param logger         the {@link Logger} of for the class
     * @param urlClassLoader the {@link ClassLoader} for the class
     */
    public ClassNameParser(Logger logger, ClassLoader urlClassLoader) {
        this.logger = logger;
        this.urlClassLoader = urlClassLoader;
    }

    /**
     * Overload of {@link #createInstanceFromClassName(String, Class)}, will call the
     * method with {@link Object} class.
     *
     * @param className string representation of the class to be instantiated
     * @return the instantiated object or null
     */
    public Object createInstanceFromClassName(@Nonnull String className) {
        return createInstanceFromClassName(className, Object.class);
    }

    /**
     * Tries to instantiate a class from a given name. Additionally,
     * a (super) class / interface has to be specified, that the class name can
     * be assigned from.
     *
     * @param className            string representation of the class to be instantiated
     * @param assignableCheckClass a (super) class the className can be assigned from
     * @param <T>                  (super) type of the class to be instantiated
     * @return the instantiated object or null
     */
    @SuppressWarnings("unchecked")
    public <T> T createInstanceFromClassName(@Nonnull String className, @Nonnull Class<T> assignableCheckClass) {

        Matcher m = classPattern.matcher(className);
        if (!m.matches()) {
            return null;
        }

        String fullQualifiedClassName = m.group(1);
        String parameterList = m.group(2);

        final List<ParameterDeclaration> paramList = createParameterList(parameterList);
        try {
            final Class<?> clazz = Class.forName(fullQualifiedClassName, true, urlClassLoader);
            if (!assignableCheckClass.isAssignableFrom(clazz)) {
                logger.error(ErrorRegister.SIMULATION_UNIT_IsNotAssignableFrom.toString() + " : " + className);
                throw new RuntimeException(ErrorRegister.SIMULATION_UNIT_IsNotAssignableFrom.toString());
            }

            final Class<?>[] constructorParams = new Class<?>[paramList.size()];
            final Object[] constructorValues = new Object[paramList.size()];
            int i = 0;
            for (ParameterDeclaration parameterDeclaration : paramList) {
                constructorParams[i] = parameterDeclaration.paramClazz;
                constructorValues[i++] = parameterDeclaration.paramValue;
            }
            return (T) (clazz.getConstructor(constructorParams).newInstance(constructorValues));
        } catch (ClassNotFoundException e) {
            logger.error("ClassNotFoundException: ({}): {}, detailed message: {}", e.getClass(), className, e.getMessage());
            throw new RuntimeException(ErrorRegister.SIMULATION_UNIT_ClassNotFoundException.toString(), e);
        } catch (NoClassDefFoundError
                | NoSuchMethodException
                | IllegalAccessException
                | InstantiationException
                | InvocationTargetException e
        ) {
            logger.error("ConstructorNotApplicableException: ({}): {}, detailed message: {}", e.getClass(), className, e.getMessage());
            throw new RuntimeException(ErrorRegister.SIMULATION_UNIT_ConstructorNotFoundError.toString(), e);
        }
    }

    /**
     * Helper method for {@link #createInstanceFromClassName(String, Class)}, matches
     * the parameters in the string and generates a list of {@link ParameterDeclaration}s.
     *
     * @param parameterList the part of {@link #createInstanceFromClassName(String, Class)}'s
     *                      input string representing the classes parameters
     * @return a list of {@link ParameterDeclaration}s including all class parameters
     */
    private List<ParameterDeclaration> createParameterList(String parameterList) {
        List<ParameterDeclaration> paramList = new ArrayList<>();
        if (StringUtils.isNotBlank(parameterList)) {
            Matcher paramMatcher;
            for (String parameter : parameterList.split(",")) {
                paramMatcher = parameterPattern.matcher(parameter.trim());
                if (paramMatcher.matches()) {
                    String doubleParam = paramMatcher.group(1);
                    String longParam = paramMatcher.group(2);
                    String intParam = paramMatcher.group(3);
                    String string1Param = paramMatcher.group(4); // with "double quotes"
                    String string2Param = paramMatcher.group(5); // with 'single quotes'
                    String boolParam = paramMatcher.group(6);
                    final ParameterDeclaration param;
                    if (intParam != null) {
                        param = ParameterDeclaration.intParam(intParam);
                    } else if (longParam != null) {
                        param = ParameterDeclaration.longParam(longParam);
                    } else if (doubleParam != null) {
                        param = ParameterDeclaration.doubleParam(doubleParam);
                    } else if (string1Param != null) {
                        param = ParameterDeclaration.stringParam(string1Param);
                    } else if (string2Param != null) {
                        param = ParameterDeclaration.stringParam(string2Param);
                    } else if (boolParam != null) {
                        param = ParameterDeclaration.booleanParam(boolParam);
                    } else {
                        param = null;
                    }
                    paramList.add(param);
                }
            }
        }
        return paramList;
    }

    /**
     * Static class used to hold primitive parameter types and values.
     */
    private final static class ParameterDeclaration {
        private Class<?> paramClazz;
        private Object paramValue;

        /**
         * Generates a {@link ParameterDeclaration} for an int-parameter.
         *
         * @param intParamAsString string representation of int
         * @return the {@link ParameterDeclaration} object
         */
        private static ParameterDeclaration intParam(String intParamAsString) {
            ParameterDeclaration result = new ParameterDeclaration();
            result.paramClazz = int.class;
            result.paramValue = Integer.parseInt(intParamAsString);
            return result;
        }

        /**
         * Generates a {@link ParameterDeclaration} for a long-parameter.
         *
         * @param longParamAsString string representation of long
         * @return the {@link ParameterDeclaration} object
         */
        private static ParameterDeclaration longParam(String longParamAsString) {
            ParameterDeclaration result = new ParameterDeclaration();
            result.paramClazz = long.class;
            result.paramValue = Long.parseLong(StringUtils.substringBeforeLast(longParamAsString, "l"));
            return result;
        }

        /**
         * Generates a {@link ParameterDeclaration} for a double-parameter.
         *
         * @param doubleParamAsString string representation of double
         * @return the {@link ParameterDeclaration} object
         */
        private static ParameterDeclaration doubleParam(String doubleParamAsString) {
            ParameterDeclaration result = new ParameterDeclaration();
            result.paramClazz = double.class;
            result.paramValue = Double.parseDouble(StringUtils.substringBeforeLast(doubleParamAsString, "d"));
            return result;
        }

        /**
         * Generates a {@link ParameterDeclaration} for a string-parameter.
         *
         * @param stringParamAsString string parameter
         * @return the {@link ParameterDeclaration} object
         */
        private static ParameterDeclaration stringParam(String stringParamAsString) {
            ParameterDeclaration result = new ParameterDeclaration();
            result.paramClazz = String.class;
            result.paramValue = stringParamAsString;
            return result;
        }

        /**
         * Generates a {@link ParameterDeclaration} for a bool-parameter.
         *
         * @param boolParamAsString string representation of bool
         * @return the {@link ParameterDeclaration} object
         */
        private static ParameterDeclaration booleanParam(String boolParamAsString) {
            ParameterDeclaration result = new ParameterDeclaration();
            result.paramClazz = boolean.class;
            result.paramValue = Boolean.parseBoolean(boolParamAsString);
            return result;
        }
    }
}
