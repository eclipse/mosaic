/*
 * Copyright (c) 2025 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.mapping.config.units;

import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.util.gson.TimeFieldAdapter;
import org.eclipse.mosaic.lib.util.gson.UnitFieldAdapter;

import com.google.gson.annotations.JsonAdapter;

import java.util.List;

public class CAgent {

    /**
     * Time at which the agent will be created.
     */
    @JsonAdapter(TimeFieldAdapter.DoubleSeconds.class)
    public double startingTime = 0.0;

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
     * Specify the applications to be used for this object. If none are
     * specified, none are used
     */
    public List<String> applications;

    /**
     * Point from which the vehicles will be spawned.
     */
    public GeoPoint origin;

    /**
     * Point to which the vehicles will travel.
     */
    public GeoPoint destination;

    /**
     * Walking speed of this agent, in m/s.
     */
    @JsonAdapter(UnitFieldAdapter.SpeedMS.class)
    public Double walkingSpeed;

}
