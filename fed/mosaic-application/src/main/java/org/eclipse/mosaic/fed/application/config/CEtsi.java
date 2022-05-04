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

package org.eclipse.mosaic.fed.application.config;

import org.eclipse.mosaic.lib.util.gson.DataFieldAdapter;
import org.eclipse.mosaic.lib.util.gson.TimeFieldAdapter;
import org.eclipse.mosaic.lib.util.gson.UnitFieldAdapter;
import org.eclipse.mosaic.rti.DATA;
import org.eclipse.mosaic.rti.TIME;

import com.google.gson.annotations.JsonAdapter;

/**
 * ETSI specific parameter (delta for each value). ETSI EN 302 637-2
 * V1.3.1 (2014-09)
 */
public class CEtsi {

    /**
     * The minimum payload length assumed for CAM messages. Unit: bits
     */
    @JsonAdapter(DataFieldAdapter.Size.class)
    public long minimalPayloadLength = 200 * DATA.BYTE;

    /**
     * (random) start offset to simulate non-synchronized timings of different application.
     */
    @JsonAdapter(TimeFieldAdapter.NanoSeconds.class)
    public long maxStartOffset = 1 * TIME.SECOND;

    /**
     * Minimal interval. I.e. 0_100_000_000ns 100ms (0.1 sec). Unit: [ns].
     */
    @JsonAdapter(TimeFieldAdapter.NanoSeconds.class)
    public Long minInterval = 100 * TIME.MILLI_SECOND;

    /**
     * Maximum time ago between two messages. I.e. 1_000_000_000ns 1000ms (1.0 sec). Unit: [ns].
     */
    @JsonAdapter(TimeFieldAdapter.NanoSeconds.class)
    public Long maxInterval = 1 * TIME.SECOND;

    /**
     * Position change. I.e. 4m. Unit: [m].
     */
    @JsonAdapter(UnitFieldAdapter.DistanceMetersQuiet.class)
    public Double positionChange = 4D;

    /**
     * Heading change. I.e. 4°. Unit: [°].
     */
    public Double headingChange = 4D;

    /**
     * Velocity change. I.e. 0.5m/s. Unit: [m/s].
     */
    @JsonAdapter(UnitFieldAdapter.SpeedMSQuiet.class)
    public Double velocityChange = 0.5D;
}
