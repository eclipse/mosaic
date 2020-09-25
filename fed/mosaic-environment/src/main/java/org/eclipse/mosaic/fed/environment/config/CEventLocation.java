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

package org.eclipse.mosaic.fed.environment.config;

import org.eclipse.mosaic.lib.geo.GeoArea;
import org.eclipse.mosaic.lib.gson.GeoAreaAdapterFactory;

import com.google.gson.annotations.JsonAdapter;

import java.io.Serializable;

/**
 * Event location configuration, which is either a {@link GeoArea} (e.g. rectangle, circle, or polygon)
 * or a specific street segment.
 */
public class CEventLocation implements Serializable {

    /**
     * The area which the event is located in (e.g. {@link org.eclipse.mosaic.lib.geo.GeoRectangle},
     * or {@link org.eclipse.mosaic.lib.geo.GeoCircle}.
     */
    @JsonAdapter(GeoAreaAdapterFactory.class)
    public GeoArea area;

    /**
     * The ID of the road segment the event is located on (Connection ID or road ID).
     */
    public String roadSegmentId;
}
