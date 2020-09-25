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

package org.eclipse.mosaic.lib.geo;

import java.io.Serializable;

public interface Area<T extends Point<T>> extends Serializable {

    /**
     * Calculates and returns the bounding box of this area.
     *
     * @return the bounding box surrounding this area
     */
    Bounds<T> getBounds();

    /**
     * Calculates the size of this area in square meters.
     *
     * @return the size of this area in square meters
     */
    double getArea();

    /**
     * Checks whether this area contains the given point.
     *
     * @param point the point
     * @return {@code true} if this area contains the given point
     */
    boolean contains(T point);

}
