/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.application.app.api.os.modules;

import org.eclipse.mosaic.lib.geo.GeoPoint;

/**
 * Interface that marks a {@link org.eclipse.mosaic.fed.application.app.api.os.OperatingSystem} as locatable,
 * meaning that is located somewhere in world and is able to provide its location.
 */
public interface Locatable {

    /**
     * This data element provides an absolute geographical longitude and latitude in a WGS84
     * coordinate system.
     *
     * @return the position.
     */
    GeoPoint getInitialPosition();

    /**
     * This data element provides an absolute geographical longitude and latitude in a WGS84
     * coordinate system.
     *
     * @return the position.
     */
    GeoPoint getPosition();

}
