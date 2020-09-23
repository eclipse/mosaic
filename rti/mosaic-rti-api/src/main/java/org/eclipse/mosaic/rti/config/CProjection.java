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

package org.eclipse.mosaic.rti.config;

import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.GeoPoint;

/**
 * Configuration for the Projection of geographic coordinates to cartesian coordinates.
 */
public class CProjection {

    /**
     * The geographic coordinates in the map from which the UTM zone is determined.
     */
    public GeoPoint centerCoordinates;

    /**
     * The cartesian offset which is considered when transformation from or to geographic coordinates.
     */
    public CartesianPoint cartesianOffset;
}

