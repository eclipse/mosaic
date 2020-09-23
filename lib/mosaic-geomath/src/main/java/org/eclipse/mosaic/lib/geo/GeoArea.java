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

package org.eclipse.mosaic.lib.geo;

public interface GeoArea extends Area<GeoPoint> {

    /**
     * Converts this area for {@link GeoPoint} to an equivalent area for {@link CartesianPoint}.
     *
     * @return the equivalent area based on {@link CartesianPoint}
     */
    CartesianArea toCartesian();

}
