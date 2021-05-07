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

package org.eclipse.mosaic.fed.sumo.traci.junit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import org.eclipse.mosaic.fed.sumo.traci.SumoVersion;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to mark single unit tests to work only with specific versions of SUMO.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface SinceSumo {

    /**
     * Returns lowest supported SUMO version on default.
     *
     * @return Lowest supported SUMO version
     */
    SumoVersion value() default SumoVersion.LOWEST;
}
