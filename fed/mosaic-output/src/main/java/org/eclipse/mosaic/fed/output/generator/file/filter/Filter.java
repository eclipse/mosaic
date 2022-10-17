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

package org.eclipse.mosaic.fed.output.generator.file.filter;

import org.eclipse.mosaic.fed.output.generator.file.format.ExtendedMethodSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public abstract class Filter {

    private static final Logger log = LoggerFactory.getLogger(Filter.class);

    private final Method method;

    protected Filter(Method method) {
        this.method = method;
    }

    /**
     * Returns {@code true} if the given object is accepted by this filter, else {@code false}. Note, if
     * an exception occurs, the filter also returns {@code true}.
     *
     * @return {@code true} if the given object is accepted by this filter
     */
    public boolean accept(Object declareObj) {
        if (this.method != null && this.method.getDeclaringClass() != ExtendedMethodSet.class) {
            try {
                Object filterObject = this.method.invoke(declareObj);
                if (filterObject != null) {
                    return this.acceptImpl(filterObject);
                }
            } catch (Exception e) {
                log.debug("Exception occurred during filtering message", e);
                return true;
            }
        }
        return true;
    }

    /**
     * @param filterObject object to be filtered
     * @return true
     */
    protected abstract boolean acceptImpl(Object filterObject);
}
