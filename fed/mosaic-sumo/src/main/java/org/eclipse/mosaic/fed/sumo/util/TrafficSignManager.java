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

package org.eclipse.mosaic.fed.sumo.util;

import org.eclipse.mosaic.fed.sumo.traci.TraciClient;
import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.objects.trafficsign.LaneAssignment;
import org.eclipse.mosaic.lib.objects.trafficsign.SpeedLimit;
import org.eclipse.mosaic.lib.objects.trafficsign.TrafficSignLaneAssignment;
import org.eclipse.mosaic.lib.objects.trafficsign.TrafficSignSpeed;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrafficSignManager {

    private final static Logger LOG = LoggerFactory.getLogger(TrafficSignManager.class);

    private final double laneWidth;

    private Set<String> variableTrafficSigns = new HashSet<>();

    private TraciClient traciClient;

    private File sumoWorkingDir;

    private TrafficSignImageCreator imageCreatorSpeedLimits;
    private TrafficSignImageCreator imageCreatorLaneAssignments;

    public TrafficSignManager(double laneWidth) {
        this.laneWidth = laneWidth;
    }

    public void configure(TraciClient traciClient, File sumoWorkingDir) throws IOException {
        this.traciClient = traciClient;
        this.sumoWorkingDir = sumoWorkingDir;
        imageCreatorLaneAssignments = TrafficSignImageCreator.forLaneAssignments(sumoWorkingDir.toPath());
        imageCreatorSpeedLimits = TrafficSignImageCreator.forSpeedLimits(sumoWorkingDir.toPath());
    }

    public void addSpeedSign(TrafficSignSpeed speedSign) throws InternalFederateException {
        List<SpeedLimit> speedLimits = speedSign.getSpeedLimits();

        for (int lane = 0; lane < speedLimits.size(); lane++) {
            int laneIndexFromLeft = speedLimits.size() - lane - 1;

            String id = String.format("%s_lane%d", speedSign.getId(), lane);
            String image = getImageForSpeedLimit(speedLimits.get(lane));
            addTrafficSignAsPoi(id, speedSign.getPosition(), image, laneIndexFromLeft, laneWidth, speedSign.getAngle());
        }
        if (speedSign.isVariable()) {
            variableTrafficSigns.add(speedSign.getId());
        }
    }

    public void addLaneAssignmentSign(TrafficSignLaneAssignment laneAssignmentSign) throws InternalFederateException {
        List<LaneAssignment> laneAssignments = laneAssignmentSign.getLaneAssignments();

        for (int lane = 0; lane < laneAssignments.size(); lane++) {
            int laneIndexFromLeft = laneAssignments.size() - lane - 1;

            String id = String.format("%s_lane%d", laneAssignmentSign.getId(), lane);
            String image = getImageForLaneAssignment(laneAssignments.get(lane));
            addTrafficSignAsPoi(id, laneAssignmentSign.getPosition(), image, laneIndexFromLeft, laneWidth, laneAssignmentSign.getAngle());
        }
        if (laneAssignmentSign.isVariable()) {
            variableTrafficSigns.add(laneAssignmentSign.getId());
        }
    }

    private void addTrafficSignAsPoi(String id, CartesianPoint basePoint, String image, int laneIndexFromLeft, double laneWidth, double angle) throws InternalFederateException {
        double distanceFromBase = ((laneIndexFromLeft + 1) * laneWidth) - (laneWidth / 2);
        double x = basePoint.getX() + Math.sin(Math.toRadians(angle + 90)) * distanceFromBase;
        double y = basePoint.getY() + Math.cos(Math.toRadians(angle + 90)) * distanceFromBase;
        if (traciClient != null && image != null) {
            traciClient.getPoiControl().addImagePoi(id, CartesianPoint.xy(x, y), image, laneWidth, laneWidth, angle);
        } else {
            LOG.error("Could not add traffic sign image.");
        }
    }

    public void changeVariableSpeedSign(String trafficSignId, int lane, double speedLimit) throws InternalFederateException {
        if (!variableTrafficSigns.contains(trafficSignId)) {
            LOG.warn("Could not find variable traffic sign with Id \"{}\". Skipping", trafficSignId);
            return;
        }

        String id = String.format("%s_lane%d", trafficSignId, lane);
        String image = getImageForSpeedLimit(new SpeedLimit(lane, speedLimit));
        if (traciClient != null && image != null) {
            traciClient.getPoiControl().changeImage(id, image);
        } else {
            LOG.error("Could not add traffic sign image.");
        }
    }

    public void changeVariableLaneAssignmentSign(String trafficSignId, int lane, List<VehicleClass> allowedVehicleClasses) throws InternalFederateException {
        if (!variableTrafficSigns.contains(trafficSignId)) {
            LOG.warn("Could not find variable traffic sign with Id \"{}\". Skipping", trafficSignId);
            return;
        }

        String id = String.format("%s_lane%d", trafficSignId, lane);
        String image = getImageForLaneAssignment(new LaneAssignment(lane, allowedVehicleClasses));
        if (traciClient != null && image != null) {
            traciClient.getPoiControl().changeImage(id, image);
        } else {
            LOG.error("Could not add traffic sign image.");
        }
    }

    private String getImageForSpeedLimit(SpeedLimit speedLimit) {
        if (imageCreatorSpeedLimits == null) {
            return null;
        }

        int speed = (int) (Math.round(speedLimit.getSpeedLimit() * 3.6));

        Path imageFile = imageCreatorSpeedLimits.getOrCreateImage(Integer.toString(speed), speedLimit.getLane());
        return sumoWorkingDir.toPath().relativize(imageFile).toString();
    }

    private String getImageForLaneAssignment(LaneAssignment laneAssignment) {
        if (imageCreatorSpeedLimits == null || imageCreatorLaneAssignments == null) {
            return null;
        }

        String assignment = "ALL";
        if (laneAssignment.getAllowedVehicleClasses().size() == 1 && laneAssignment.isVehicleClassAllowed(VehicleClass.AutomatedVehicle)) {
            assignment = "AV";
        } else if (laneAssignment.getAllowedVehicleClasses().size() == 1 && laneAssignment.isVehicleClassAllowed(VehicleClass.ElectricVehicle)) {
            assignment = "EV";
        } else if (laneAssignment.getAllowedVehicleClasses().size() == 0) {
            //special case: load empty sign with speed-limit background
            Path imageFile = imageCreatorSpeedLimits.getOrCreateImage(null, laneAssignment.getLane());
            return sumoWorkingDir.toPath().relativize(imageFile).toString();
        }

        Path imageFile = imageCreatorLaneAssignments.getOrCreateImage(assignment, laneAssignment.getLane());
        return sumoWorkingDir.toPath().relativize(imageFile).toString();
    }
}
