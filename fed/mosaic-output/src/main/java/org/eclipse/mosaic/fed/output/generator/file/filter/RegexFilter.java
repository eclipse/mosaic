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

package org. eclipse.mosaic.fed.output.generator.file.filter;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * Filter which accepts object which match the configured regular expression.
 */
public class RegexFilter extends Filter {

    private final Pattern pattern;

    RegexFilter(Method method, String filterDefinition) {
        super(method);
        pattern = Pattern.compile(filterDefinition);
    }

    @Override
    protected boolean acceptImpl(Object filterObject) {
        return pattern.matcher(String.valueOf(filterObject)).matches();
    }
}
