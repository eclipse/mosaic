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

package org.eclipse.mosaic.fed.environment.config;

import org.eclipse.mosaic.lib.util.gson.TimeFieldAdapter;

import com.google.gson.annotations.JsonAdapter;

import java.io.Serializable;

/**
 * Event time configuration.
 */
public final class CEventTime implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Start time of the event.
     */
    @JsonAdapter(TimeFieldAdapter.NanoSeconds.class)
    public long start;

    /**
     * End time of the event.
     */
    @JsonAdapter(TimeFieldAdapter.NanoSeconds.class)
    public long end;

}

