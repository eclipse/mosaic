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

package org.eclipse.mosaic.rti.api;

import org.eclipse.mosaic.rti.api.time.FederateEvent;

public interface Monitor {

    default void onInteraction(Interaction interaction) {
        // nop
    }

    default void onReceiveInteraction(String id, Interaction interaction) {
        // nop
    }

    default void onProcessInteraction(String id, Interaction nextInteraction) {
        // nop
    }

    default void onBeginSimulation(FederationManagement federationManagement, TimeManagement timeManagement, int numberOfThreads) {
        // nop
    }

    default void onEndSimulation(FederationManagement federationManagement, TimeManagement timeManagement, long durationInMs, int statusCode) {
        // nop
    }

    default void onEndActivity(FederateEvent event, long l) {
        // nop
    }

    default void onBeginActivity(FederateEvent event) {
        // nop
    }

    default void onScheduling(int id, FederateEvent event) {
        // nop
    }
}
