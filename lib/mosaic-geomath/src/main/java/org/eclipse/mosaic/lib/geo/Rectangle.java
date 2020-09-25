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

/**
 * A {@link Rectangle} represents an immutable pair of two different points.
 * They form a rectangular area. The following definitions are possible:
 *
 * <pre>
 *
 *      a ___________                    ___________ b
 *       |           |                  |           |
 *       |     1     |                  |     2     |
 *       |___________|                  |___________|
 *                    b                a
 * </pre>
 */
public interface Rectangle<T extends Point<T>> {

    T getA();

    T getB();

    double getArea();

    T getCenter();

    Polygon<T> toPolygon();

}
