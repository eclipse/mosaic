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

package org.eclipse.mosaic.fed.sumo.bridge.api;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.List;

/**
 * This class represents the SUMO command which allows to get the links controlled by a traffic light.
 */
public interface TrafficLightGetControlledLinks {

    List<TrafficLightControlledLink> execute(Bridge bridge, String tlId) throws CommandException, InternalFederateException;

    class TrafficLightControlledLink {
        private final int signalIndex;
        private final String incoming;
        private final String outgoing;

        public TrafficLightControlledLink(int signalIndex, String incoming, String outgoing) {
            this.signalIndex = signalIndex;
            this.incoming = incoming;
            this.outgoing = outgoing;
        }

        @SuppressWarnings("unused")
        public int getSignalIndex() {
            return signalIndex;
        }

        public String getIncoming() {
            return incoming;
        }

        public String getOutgoing() {
            return outgoing;
        }
    }
}
