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

package org. eclipse.mosaic.fed.output.generator.file.format;

import org.eclipse.mosaic.rti.api.Interaction;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A MethodManager saves the methods to be used for visualizing interactions
 * and formats a given interaction according to methods definition, which may
 * have following syntax:
 * <p/>
 * { "Method1.Method11", "Iterate2:Method21", ""myString"" }
 */
class MethodManager {

    private final String separator;

    /**
     * an iteration method returns a collection, which will be iterated when visualizing. *
     */
    private final List<MethodElement> iterationMethods = new ArrayList<>();

    /**
     * an output method returns a string, which will be written in file. *
     */
    private final List<MethodElement> outputMethods = new ArrayList<>();

    /**
     * Constructs a MethodManager.
     *
     * @param separator  separator
     * @param methodsDefinitions method definitions
     * @param interactionClass   interaction class
     */
    MethodManager(String separator, List<String> methodsDefinitions, Class<?> interactionClass)
            throws SecurityException, NoSuchMethodException, IllegalArgumentException {
        this.separator = separator;

        final List<String> methods = new ArrayList<>(methodsDefinitions);

        final List<Integer> objLevels = new ArrayList<>();
        final List<Class<?>> objClasses = new ArrayList<>();

        // when initialize, set all methods to the root level of iteration
        // and all the declare class to msgClass
        for (int i = 0; i < methods.size(); i++) {
            objLevels.add(i, 0);
            objClasses.add(i, interactionClass);
        }

        int itMetIdx;

        // initialize iteration methods
        for (int level = 1; (itMetIdx = findIterationMethod(methods)) != -1; level++) {
            // what is the name of this iteration method, without "get"
            String methodName = getIterationMethodName(methods.get(itMetIdx));

            // which class declares this iteration method
            Class<?> declare = objClasses.get(itMetIdx);

            // what is the level of the object, that calls the iteration method
            int objLevel = objLevels.get(itMetIdx);

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
            for (int i = itMetIdx; i < methods.size(); i++) {
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

            this.outputMethods.add(
                    new MethodElement(objLevel, declaredClass, methods.get(i))
            );
        }
    }

    public String format(Interaction interaction) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        List<Object> itObjects = new ArrayList<>();
        itObjects.add(interaction);
        return format(itObjects, 0);
    }

    private String format(List<Object> itObjects, int level) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        StringBuilder res = new StringBuilder();

        if (level == this.iterationMethods.size()) {
            // output method
            MethodElement outputMethod;
            for (int i = 0; i < this.outputMethods.size(); i++) {
                outputMethod = this.outputMethods.get(i);
                res.append(outputMethod.invoke(itObjects)).append((i == this.outputMethods.size() - 1 ? "\n" : this.separator));
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
                res.append(format(itObjects, level + 1));
            } else {
                for (Object element : c) {
                    itObjects.set(level + 1, element);
                    res.append(format(itObjects, level + 1));
                }
            }
            itObjects.remove(level);
        }

        return res.toString();
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
