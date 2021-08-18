/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.objects.electricity;

/**
 * This enum is used to differentiate between the three commonly used types of charging:
 * 1. AC 1-phased charging
 * 2. AC 3-phased charging
 * 3. DC charging
 */
public enum ChargingType {
    /**
     * Slow charging commonly in use when EV is charged using household sockets.
     */
    AC_1_PHASE,
    /**
     * 3-phased charging, commonly in use when EV is charged using a Wallbox.
     */
    AC_3_PHASE,
    /**
     * DC charging, commonly used when EV is charged using an external DC-/Fast-Charger.
     */
    DC
}
