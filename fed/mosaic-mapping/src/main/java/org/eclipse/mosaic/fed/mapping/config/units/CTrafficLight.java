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
 */

package org.eclipse.mosaic.fed.mapping.config.units;

import java.util.List;

/**
 * Defining a prototype for a traffic light. Since it is a traffic light only
 * applications can be defined.
 */
public class CTrafficLight {

    /**
     * The name of the prototype to be matched against this object. All
     * properties which are not specified will then be replaced.
     */
    public String name;

    /**
     * The group name.
     */
    public String group;

    /**
     * The name of the traffic light.
     */
    public String tlGroupId;

    /**
     * The weight is used to distribute traffic lights between multiple default
     * types. All weights do NOT have to add up to 1 or 100.
     */
    public Double weight;

    /**
     * Specify the applications to be used for this object. If none are
     * specified, none are used
     */
    public List<String> applications;
}
