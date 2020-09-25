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

package org.eclipse.mosaic.lib.transform;

public enum ReferenceEllipsoid {
    WGS_84(6378137, 0.00669438, 40075036.0);

    public final double equatorialRadius;
    public final double eccentricitySquared;
    public final double circumference;

    ReferenceEllipsoid(double radius, double ecc) {
        this(radius, ecc, 2 * Math.PI * radius);
    }

    ReferenceEllipsoid(double radius, double ecc, double c) {
        equatorialRadius = radius;
        eccentricitySquared = ecc;
        circumference = c;
    }
}