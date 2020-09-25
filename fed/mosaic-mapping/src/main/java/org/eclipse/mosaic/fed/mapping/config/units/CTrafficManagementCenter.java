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

package org.eclipse.mosaic.fed.mapping.config.units;

import java.util.List;

/**
 * Class defining a Traffic Management Center (TMC). Will be parsed against
 * JSON-configuration.
 */
public class CTrafficManagementCenter {

    /**
     * The name of the tmc to be matched against this object. All
     * properties which are not specified will then be replaced.
     */
    public String name;

    /**
     * The group name.
     */
    public String group;

    /**
     * Specify the applications to be used for this object. If none are
     * specified, none are used.
     */
    public List<String> applications;

    /**
     * Specify the induction loops the tmc shall be matched with. If none are
     * specified, none are used.
     */
    public List<String> inductionLoops;

    /**
     * Specify the lane area detectors the tmc shall be matched with. If none are
     * specified, none are used.
     */
    public List<String> laneAreaDetectors;
}
