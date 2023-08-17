/*
 * Copyright (c) 2023 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception.errormodels;

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.PerceptionModuleOwner;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.SpatialObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.VehicleObject;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.math.VectorUtils;
import org.eclipse.mosaic.lib.spatial.Edge;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoundingBoxOcclusionModifier implements PerceptionModifier {

    /**
     * This defines how many equidistant points shall be evaluated per edge of a vehicle.
     * Note generally for the front and rear edge this will result in a higher resolution compared to the sides of the vehicle.
     */
    private final int edgeResolution;

    /**
     * Threshold that defines how many of the points defined through {@link #edgeResolution} need to be visible in order for a
     * vehicle to be treated as detected.
     */
    private final int detectionThreshold;

    public BoundingBoxOcclusionModifier(int edgeResolution, int detectionThreshold) {
        this.edgeResolution = edgeResolution;
        this.detectionThreshold = detectionThreshold;
    }

    @Override
    public <T extends SpatialObject<T>> List<T> apply(PerceptionModuleOwner owner, List<T> spatialObjects) {
        List<T> newObjects = new ArrayList<>();
        Map<VehicleObject, VehicleBoundingBox> boundingBoxes = new HashMap<>();
        for (T spatialObject : spatialObjects) {
            if (!(spatialObject instanceof VehicleObject)) { // skip non-vehicle spatial object
                newObjects.add(spatialObject);
                continue;
            }
            if (spatialObject.getId().equals(owner.getId())) { // skip calculation of own bounding box
                continue;
            }
            VehicleObject currObject = (VehicleObject) spatialObject;
            boundingBoxes.put(currObject, VehicleBoundingBox.createFromVehicleObject(currObject));
        }
        for (Map.Entry<VehicleObject, VehicleBoundingBox> boundingBoxEntry : boundingBoxes.entrySet()) {
            VehicleObject currentVehicleObject = boundingBoxEntry.getKey();
            VehicleBoundingBox currentBoundingBox = boundingBoxEntry.getValue();
            List<Vector3d> pointsToEvaluate = Lists.newArrayList(
                    currentBoundingBox.getFrontLeftCorner(),
                    currentBoundingBox.getFrontRightCorner(),
                    currentBoundingBox.getBackLeftCorner(),
                    currentBoundingBox.getFrontRightCorner()
            );
            double spreadDistanceWidth = currentVehicleObject.getWidth() / edgeResolution;
            double spreadDistanceLength = currentVehicleObject.getLength() / edgeResolution;
            for (int i = 0; i < edgeResolution - 2; i++) {
//                pointsToEvaluate.add()
            }
            int nonOccludedPoints = 0;

        }
        return newObjects;
    }



    /**
     * An object representing a vehicles' bounding box.
     * <pre>
     *   frontRightCorner----------rightEdge----------backRightCorner
     *                  |                             |
     *   Heading <== frontEdge                     backEdge
     *                  |                             |
     *    frontLeftCorner----------leftEdge-----------backLeftCorner
     * </pre>
     */
    static class VehicleBoundingBox {
        private final Vector3d frontRightCorner;
        private final Vector3d backRightCorner;
        private final Vector3d backLeftCorner;
        private final Vector3d frontLeftCorner;
        private final Edge<Vector3d> frontEdge;
        private final Edge<Vector3d> rightEdge;
        private final Edge<Vector3d> backEdge;
        private final Edge<Vector3d> leftEdge;

        private VehicleBoundingBox(Vector3d frontRightCorner, Vector3d backRightCorner, Vector3d backLeftCorner, Vector3d frontLeftCorner) {
            this.frontRightCorner = frontRightCorner;
            this.backRightCorner = backRightCorner;
            this.backLeftCorner = backLeftCorner;
            this.frontLeftCorner = frontLeftCorner;
            frontEdge = new Edge<>(frontLeftCorner, frontRightCorner);
            rightEdge = new Edge<>(frontRightCorner, backRightCorner);
            backEdge = new Edge<>(backRightCorner, backLeftCorner);
            leftEdge = new Edge<>(backLeftCorner, frontLeftCorner);
        }

        public static VehicleBoundingBox createFromVehicleObject(VehicleObject vehicleObject) {
            Vector3d headingVector = new Vector3d();
            VectorUtils.getDirectionVectorFromHeading(vehicleObject.getHeading(), headingVector).norm();
            double length = vehicleObject.getLength();
            double halfWidth = vehicleObject.getWidth() / 2;
            Vector3d pointA = new Vector3d(headingVector).rotateDeg(90d, VectorUtils.UP);
            pointA = pointA.multiply(halfWidth).add(vehicleObject);
            Vector3d pointD = new Vector3d(headingVector).rotateDeg(-90d, VectorUtils.UP);
            pointD = pointD.multiply(halfWidth).add(vehicleObject);
            Vector3d pointB = new Vector3d(headingVector).multiply(-1d);
            pointB = pointB.multiply(length).add(pointA);
            Vector3d pointC = new Vector3d(headingVector).multiply(-1d);
            pointC = pointC.multiply(length).add(pointD);
            return new VehicleBoundingBox(pointA, pointB, pointC, pointD);
        }

        public Vector3d getFrontRightCorner() {
            return frontRightCorner;
        }

        public Vector3d getBackRightCorner() {
            return backRightCorner;
        }

        public Vector3d getBackLeftCorner() {
            return backLeftCorner;
        }

        public Vector3d getFrontLeftCorner() {
            return frontLeftCorner;
        }

        public Edge<Vector3d> getFrontEdge() {
            return frontEdge;
        }

        public Edge<Vector3d> getRightEdge() {
            return rightEdge;
        }

        public Edge<Vector3d> getBackEdge() {
            return backEdge;
        }

        public Edge<Vector3d> getLeftEdge() {
            return leftEdge;
        }
    }
}
