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

package org.eclipse.mosaic.rti.api.parameters;

/**
 * Constant values for prioritizing federates.
 */
public final class FederatePriority {

    /**
     * The highest priority possible (= 0).
     */
    public static final byte HIGHEST = 0;

    /**
     * The lowest priority possible (= 100).
     */
    public static final byte LOWEST = 100;

    /**
     * The default priority assigned to all federates (= 50).
     */
    public static final byte DEFAULT = (HIGHEST + LOWEST) / 2;
}
