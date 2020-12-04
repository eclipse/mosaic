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
import org.eclipse.mosaic.fed.sumo.bridge.api.complex.SumoTrafficLightLogic;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.List;

/**
 * Adds a complete definition of a traffic light program. Does only work with SUMO >= 1.1.0.
 */
public interface TrafficLightAddProgram {

    void execute(Bridge con, String tlId, String programId, int phaseIndex, List<SumoTrafficLightLogic.Phase> phases) throws CommandException, InternalFederateException;
}
