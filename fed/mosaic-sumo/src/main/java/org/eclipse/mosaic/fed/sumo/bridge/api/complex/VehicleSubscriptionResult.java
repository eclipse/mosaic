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

package org.eclipse.mosaic.fed.sumo.bridge.api.complex;

import org.eclipse.mosaic.lib.util.objects.Position;

/**
 * This class summarizes the results of the vehicle subscription.
 */
public class VehicleSubscriptionResult extends AbstractSubscriptionResult {
    public Position position;
    public double heading;

    public double speed;
    public double acceleration;
    public double slope;
    public double distanceDriven;
    public int stoppedStateEncoded;
    public int signalsEncoded;
    public double minGap;

    public String routeId;
    public String edgeId;
    public int laneIndex;
    public double lanePosition;
    public double lateralLanePosition;

    public double co;
    public double co2;
    public double pmx;
    public double hc;
    public double nox;
    public double fuel;
    public double electricity;

    public LeadFollowVehicle leadingVehicle = LeadFollowVehicle.NONE;
    public LeadFollowVehicle followerVehicle = LeadFollowVehicle.NONE;
}