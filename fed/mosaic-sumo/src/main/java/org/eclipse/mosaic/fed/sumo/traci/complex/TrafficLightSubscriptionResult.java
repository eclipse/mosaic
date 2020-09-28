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

package org.eclipse.mosaic.fed.sumo.traci.complex;

/**
 * Information received from SUMO about a traffic light that we have subscribed to.
 **/
public class TrafficLightSubscriptionResult extends AbstractSubscriptionResult {

    public String currentProgramId;
    public int currentPhaseIndex;

    /**
     * Assumed time of the next phase switch. Unit: [ns]
     */
    public long assumedNextPhaseSwitchTime;

    public String currentStateEncoded;

}
