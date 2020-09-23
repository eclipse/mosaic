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

import org.eclipse.mosaic.lib.math.Vector3d;

import java.io.Serializable;

public interface Point<T extends Point<T>> extends Serializable {

    double distanceTo(T other);

    default Vector3d toVector3d() {
        return toVector3d(new Vector3d());
    }

    Vector3d toVector3d(Vector3d result);
}
