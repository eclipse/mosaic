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

package org.eclipse.mosaic.fed.output.generator.file.format;

import org.eclipse.mosaic.rti.api.Interaction;

import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * A MethodManager saves the methods to be used for visualizing interactions
 * and formats a given interaction according to methods definition, which may
 * have following syntax:
 * <p/>
 * { "Method1.Method11", "Iterate2:Method21", ""myString"" }
 */
class MethodManager {

    public static final String LINE_SEPARATOR = "\n";
    private final char separator;
    private final DecimalFormat decimalFormat;
    /**
     * An iteration method returns a collection, which will be iterated when visualizing.
     */
    private final List<MethodElement> iterationMethods = new ArrayList<>();

    /**
     * An output method returns a string, which will be written in file.
     */
    private final List<MethodElement> outputMethods = new ArrayList<>();

    /**
     * Constructs a MethodManager.
     *
     * @param separator          separator
     * @param decimalSeparator   separator for floating-point numbers
     * @param methodsDefinitions method definitions
     * @param interactionClass   interaction class
     */
    MethodManager(char separator, char decimalSeparator, List<String> methodsDefinitions, Class<?> interactionClass)
            throws SecurityException, NoSuchMethodException, IllegalArgumentException {
        this.separator = separator;
        String formatString = "#0.0##############"; // maximum of 15 decimal digits
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        decimalFormatSymbols.setDecimalSeparator(decimalSeparator);
        this.decimalFormat = new DecimalFormat(formatString, decimalFormatSymbols);

        decimalFormat.setGroupingUsed(false); // disable grouping when using custom separator to prevent issues

        final List<String> methods = new ArrayList<>(methodsDefinitions);

        final List<Integer> objLevels = new ArrayList<>();
        final List<Class<?>> objClasses = new ArrayList<>();

        // when initialized, set all methods to the root level of iteration and all the declare-classes to interactionClass
        for (int i = 0; i < methods.size(); i++) {
            objLevels.add(i, 0);
            objClasses.add(i, interactionClass);
        }

        int iterationMethodIndex;

        // initialize iteration methods
        for (int level = 1; (iterationMethodIndex = findIterationMethod(methods)) != -1; level++) {
            // what is the name of this iteration method, without "get"
            String methodName = getIterationMethodName(methods.get(iterationMethodIndex));

            // which class declares this iteration method
            Class<?> declare = objClasses.get(iterationMethodIndex);

            // what is the level of the object, that calls the iteration method
            int objLevel = objLevels.get(iterationMethodIndex);

            MethodElement me = new MethodElement(objLevel, declare, methodName);

            this.iterationMethods.add(me);

            // the class of the component in this iteration
            Class<?> componentClass = me.getIterationComponentClass();

            if (componentClass == null) {
                throw new IllegalArgumentException("Method defined by (" + methodName + ") does not return a Collection.");
            }

            // set all the methods iterated by this iteration method in a level
            // deeper than the given level
            // and also its iterated object class
            for (int i = iterationMethodIndex; i < methods.size(); i++) {
                String method = methods.get(i);

                if (!method.startsWith(methodName)) {
                    continue;
                }

                objLevels.set(i, level);
                objClasses.set(i, componentClass);
                method = method.substring(method.indexOf(':') + 1);

                methods.set(i, method);
            }
        }

        // initialize the output methods
        for (int i = 0; i < methods.size(); i++) {

            // what is the level of the object, that calls the iteration method
            int objLevel = objLevels.get(i);

            // which class declares this iteration method
            Class<?> declaredClass = objClasses.get(i);

            this.outputMethods.add(new MethodElement(objLevel, declaredClass, methods.get(i))
            );
        }
    }

    public String format(Interaction interaction) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        List<Object> itObjects = new ArrayList<>();
        itObjects.add(interaction);
        return format(itObjects, 0);
    }

    private String format(List<Object> itObjects, int level) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StringBuilder result = new StringBuilder();

        if (level == this.iterationMethods.size()) {
            // output method
            MethodElement outputMethod;
            for (int i = 0; i < this.outputMethods.size(); i++) {
                outputMethod = this.outputMethods.get(i);
                Object methodInvocationResult = outputMethod.invoke(itObjects);
                // if result of method invocation is float or double use defined decimal format
                if (methodInvocationResult instanceof Double || methodInvocationResult instanceof Float) {
                    String invocationResultString = decimalFormat.format(methodInvocationResult);
                    result.append(invocationResultString);
                } else {
                    result.append(methodInvocationResult);
                }
                result.append(i == this.outputMethods.size() - 1 ? LINE_SEPARATOR : this.separator); // add separator or linebreak
                if (!outputMethod.isAcceptedByFilter(itObjects)) {
                    return "";
                }
            }
        } else {
            // iteration method
            Collection<?> c = (Collection<?>) this.iterationMethods.get(level).invoke(itObjects);

            itObjects.add(null);

            if (c == null) {
                itObjects.set(level + 1, null);
                result.append(format(itObjects, level + 1));
            } else {
                for (Object element : c) {
                    itObjects.set(level + 1, element);
                    result.append(format(itObjects, level + 1));
                }
            }
            itObjects.remove(level);
        }

        return result.toString();
    }

    private String getIterationMethodName(String method) {
        int i = method.indexOf(":");
        if (i == -1) {
            return null;
        } else {
            return method.substring(0, i);
        }
    }

    private int findIterationMethod(List<String> methods) {
        for (int i = 0; i < methods.size(); i++) {
            String method = methods.get(i);

            if (method.contains(":")) {
                return i;
            }
        }
        return -1;
    }
}
