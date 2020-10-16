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

import org.eclipse.mosaic.fed.output.generator.file.FileOutputLoader;
import org.eclipse.mosaic.fed.output.generator.file.filter.Filter;
import org.eclipse.mosaic.fed.output.generator.file.filter.FilterFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A MethodElement defines a specific method, including the name
 * of the method to be called and from which level of the iterate
 * structure(That means, within an iterate, there can be another
 * embedded iterate.)comes the object, to which the method belongs.
 */
class MethodElement {

    private final Logger log = LoggerFactory.getLogger(FileOutputLoader.class);

    /**
     * The iteration level of the object, to which the method belongs.
     */
    private final int objIndex;

    private final String constantValue;

    private ArrayList<Method> methodList;
    private ArrayList<Filter> filterList;

    /**
     * Constructs a method element with sanity checking.
     *
     * @param objIndex iteration level of the object, to which the method belongs
     * @param declare  class
     * @param methods  list of methods
     */
    MethodElement(int objIndex, Class<?> declare, String methods) throws SecurityException, NoSuchMethodException {
        this.objIndex = objIndex;
        this.methodList = null;

        if (methods.startsWith("\"") && methods.endsWith("\"")) {
            // A constant is set
            this.constantValue = methods.substring(1, methods.length() - 1);
        } else {
            // A method is set
            this.constantValue = null;

            if (declare != null) {
                initMethodList(declare, methods);
            }
        }
    }

    /**
     * create a static method list. Thus, we don't parse the method definition any more.
     *
     * @param declare class
     * @param methods methods list
     */
    private void initMethodList(Class<?> declare, String methods) throws SecurityException, NoSuchMethodException {
        Class<?> cl = declare;

        if (cl == null) {
            return;
        }

        this.methodList = new ArrayList<>();
        this.filterList = new ArrayList<>();

        for (int i = 0, begin = 0, end = methods.indexOf("."); begin != -1; i++) {

            String methodName;
            if (end == -1) {
                methodName = methods.substring(begin);
                begin = end;
            } else {
                methodName = methods.substring(begin, end);
                begin = end + 1;
                end = methods.indexOf(".", begin);
            }

            String filterDefs = StringUtils.substringBeforeLast(StringUtils.substringAfter(methodName, "["), "]");
            if (!filterDefs.isEmpty()) {
                methodName = StringUtils.substringBefore(methodName, "[");
            }

            Method m = retrieveMethod(cl, "get" + methodName) == null
                    ? retrieveMethod(cl, "is" + methodName)
                    : retrieveMethod(cl, "get" + methodName);
            if (m == null) {
                throw new NoSuchMethodException(
                        "Method(" + methodName + ") is not supported in either basic or extended method set of " + cl.getName()
                );
            }
            this.methodList.add(m);
            cl = m.getReturnType();

            for (String filterDef : filterDefs.split(",")) {
                if (StringUtils.isNotBlank(filterDef)) {
                    this.filterList.add(FilterFactory.createFilter(m, filterDef));
                }
            }
        }
    }

    /**
     * Returns method from class.
     *
     * @param clazz      class
     * @param methodName method name
     * @return Method or null
     */
    private Method retrieveMethod(Class<?> clazz, String methodName) {
        Method m = null;
        try {
            m = clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            try {
                Class<?> scl = clazz.getSuperclass();
                if (scl != null) {
                    m = clazz.getSuperclass().getMethod(methodName);
                } else {
                    throw e;
                }
            } catch (NoSuchMethodException e1) {
                try {
                    m = ExtendedMethodSet.class.getMethod(methodName, clazz);
                } catch (NoSuchMethodException e2) {
                    try {
                        Class<?> scl = clazz.getSuperclass();
                        if (scl != null) {
                            m = ExtendedMethodSet.class.getMethod(methodName, clazz.getSuperclass());
                        } else {
                            throw e2;
                        }
                    } catch (NoSuchMethodException e3) {
                        // nop
                    }
                }
            }
        }
        return m;
    }

    /**
     * Return the component Class of iteration.
     * If the return type of the method isn't Collection, then return null;
     * <p/>
     * Currently this function always return null.
     *
     * @return class
     */
    public Class<?> getIterationComponentClass() {

        if (!this.hasInitialized()) {
            return null;
        }

        int metcnt = methodList.size();

        if (metcnt != 0) {
            Method m = methodList.get(metcnt - 1);

            Type retType = m.getGenericReturnType();
            if (retType instanceof ParameterizedType) {
                Type[] type = ((ParameterizedType) retType).getActualTypeArguments();

                if (type.length == 1) {
                    return (Class<?>) type[0];
                }
            }
        }
        return null;
    }

    /**
     * invoke the Method defined by this MethodElement.
     * If the methods have such a syntax: "myString", then return the String inside ""
     * Otherwise, have the methods this syntax: "Method1.Method2",
     * Then the "getMethod1()" of the Object, whose index in objList equals to objIndex,
     * will be called, and then invoke "getMethod2()" of the object returned by the first
     * call.
     *
     * @param objList list of objects
     * @return object
     */
    public Object invoke(final List<Object> objList)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        // if a constant string is defined
        if (constantValue != null) {
            return constantValue;
        }

        Object ret = null;
        Object declareObj = objList.get(objIndex);

        if (declareObj == null) {
            return null;
        }

        Class<?> declareClass = declareObj.getClass();

        // cascaded method
        for (Method m : methodList) {
            String simpleName = m.getName();

            // extended method
            if (m.getDeclaringClass() == ExtendedMethodSet.class) {
                ret = m.invoke(null, declareObj);
            } else {
                // basic method
                ret = m.invoke(declareObj);

                // if basic method returns null, then try the extended method
                if (ret == null) {
                    for (Class<?> c = declareClass; c != null; c = c.getSuperclass()) {
                        try {
                            Method extMet = ExtendedMethodSet.class.getMethod(simpleName, c);
                            ret = extMet.invoke(null, declareObj);
                        } catch (NoSuchMethodException e) {
                            //be quiet as we try until we find something suitable
                        } catch (Exception e) {
                            log.debug("Exception occurred", e);
                        }
                    }
                }
            }

            declareObj = ret;
            if (declareObj != null) {
                declareClass = declareObj.getClass();
            } else {
                // if a null object is returned, then stop invoking remaining methods
                return null;
            }
        }

        return ret;
    }

    public boolean isAcceptedByFilter(final List<Object> objList) {
        if (constantValue != null) {
            return true;
        }

        Object declareObj = objList.get(objIndex);
        if (declareObj == null) {
            return true;
        }

        for (Filter filter : this.filterList) {
            if (!filter.accept(declareObj)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasInitialized() {
        return this.constantValue != null || this.methodList != null;
    }
}
