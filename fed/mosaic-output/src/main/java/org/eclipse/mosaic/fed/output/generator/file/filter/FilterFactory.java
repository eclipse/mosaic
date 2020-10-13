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

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import javax.annotation.Nonnull;

public class FilterFactory {

    private static final Filter NO_FILTER = new Filter(null) {
        @Override
        protected boolean acceptImpl(Object filterObject) {
            return true;
        }
    };

    /**
     * Creates a filter which matches best to the given filter definition. The filter definition
     * must be in format "filtername=filtervalue". Currently, the following filters exist:<br><br>
     *
     * <ul>
     *     <li>"min" - accept objects which are greater than the configured value</li>
     *     <li>"max" - accept objects which are lower than the configured value</li>
     *     <li>"eq" - accept objects which are equal to the configured value</li>
     *     <li>"regex" - accept objects which matches the configured regular expression (strings only)</li>
     * </ul>
     *
     * @param filterDef the definition of the filter
     * @return a filter which matches to the given filter definition. An empty filter is returned, if no such filter exists.
     */
    public static @Nonnull
    Filter createFilter(Method m, String filterDef) {
        final String filterName = StringUtils.substringBefore(filterDef, "=").trim();
        final String filterValue = StringUtils.substringAfter(filterDef, "=").trim();

        if (filterName.equalsIgnoreCase("min")) {
            return new MinFilter(m, filterValue);
        } else if (filterName.equalsIgnoreCase("max")) {
            return new MaxFilter(m, filterValue);
        } else if (filterName.equalsIgnoreCase("eq")) {
            return new EqualsFilter(m, filterValue);
        } else if (filterName.equalsIgnoreCase("regex")) {
            return new RegexFilter(m, filterValue);
        } else {
            return NO_FILTER;
        }
    }
}
