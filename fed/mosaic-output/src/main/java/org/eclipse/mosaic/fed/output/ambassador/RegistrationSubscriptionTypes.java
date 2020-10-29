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

package org.eclipse.mosaic.fed.output.ambassador;

import com.google.common.collect.Sets;
import org.eclipse.mosaic.interactions.mapping.RsuRegistration;
import org.eclipse.mosaic.interactions.mapping.ServerRegistration;
import org.eclipse.mosaic.interactions.mapping.TmcRegistration;
import org.eclipse.mosaic.interactions.mapping.TrafficLightRegistration;
import org.eclipse.mosaic.interactions.mapping.VehicleRegistration;
import org.eclipse.mosaic.interactions.trafficsigns.TrafficSignRegistration;

import java.util.Collections;
import java.util.Set;

/**
 * This Class is used to identify initialization interaction types.
 */
public final class RegistrationSubscriptionTypes {

    private final static Set<String> registrationSubscriptionTypes = Collections.unmodifiableSet(Sets.newHashSet(
            RsuRegistration.TYPE_ID,
            TmcRegistration.TYPE_ID,
            ServerRegistration.TYPE_ID,
            TrafficLightRegistration.TYPE_ID,
            TrafficSignRegistration.TYPE_ID,
            VehicleRegistration.TYPE_ID
    ));


    public static Set<String> get() {
        return registrationSubscriptionTypes;
    }
}
