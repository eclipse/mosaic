/*
 * Copyright (c) 2022 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.sumo.bridge.traci;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;
import org.eclipse.mosaic.fed.sumo.bridge.CommandException;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import com.google.common.collect.Lists;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TrafficLightGetControlledJunctions implements org.eclipse.mosaic.fed.sumo.bridge.api.TrafficLightGetControlledJunctions {

    boolean warned = false;

    public List<String> execute(Bridge bridge, String tlId) throws CommandException, InternalFederateException {
        if (!warned) {
            LoggerFactory.getLogger(this.getClass()).warn("Could not return list of controlled junctions for traffic light. Not implemented in SUMO yet.");
            warned = true;
        }
        return Lists.newArrayList(tlId);
    }
}
