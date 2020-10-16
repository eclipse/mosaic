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

package org.eclipse.mosaic.fed.cell.config.model;

import org.eclipse.mosaic.lib.geo.CartesianPolygon;
import org.eclipse.mosaic.lib.geo.GeoPolygon;
import org.eclipse.mosaic.lib.geo.GeoRectangle;

/**
 * {@link CMobileNetworkProperties} extends the {@link CNetworkProperties} with specific geographical extensions (area).
 * It applies for all regions except the "globalNetwork", which covers the remaining space.
 * The CMobileNetworkProperties only needs to be employed, when geographic information are accessed.
 */
public final class CMobileNetworkProperties extends CNetworkProperties {
    /**
     * The area.
     */
    public GeoRectangle area;

    /**
     * The area as a polygon.
     */
    public GeoPolygon polygon;

    /**
     * Cartesian polygon of the area (in parallel to georectangle).
     */
    private transient CartesianPolygon capoArea;

    public CartesianPolygon getCapoArea() {
        if (this.capoArea == null) {
            if (this.polygon != null) {
                this.capoArea = this.polygon.toCartesian();
            } else {
                this.capoArea = this.area.toCartesian().toPolygon();
            }
        }
        return this.capoArea;
    }

    @Override
    public String toString() {
        return super.toString() + ", " + ((polygon != null) ? "polygon: " + polygon.toString() : "area: " + area.toString());
    }
}
