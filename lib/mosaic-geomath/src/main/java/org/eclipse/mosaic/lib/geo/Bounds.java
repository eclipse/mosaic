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

/**
 * Extends the {@link Rectangle} with side representation for defining a bounding box.
 *
 * <pre>
 *             sideC
 *        ___________
 *       |           |
 * sideD |           | sideB
 *       |___________|
 *           sideA
 * </pre>
 */
public interface Bounds<T extends Point<T>> extends Rectangle<T> {

    double getSideA();

    double getSideB();

    double getSideC();

    double getSideD();

}
