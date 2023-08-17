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

    private final Vector3d intersectionResult = new Vector3d();
    /**
     * This defines how many equidistant points shall be evaluated per edge of a vehicle.
     * Note generally for the front and rear edge this will result in a higher resolution compared to the sides of the vehicle.
     */
    private final int pointsPerEdge;

    /**
     * Threshold that defines how many of the points defined through {@link #pointsPerEdge} need to be visible in order for a
     * object to be treated as detected.
     */
    private final int detectionThreshold;

    public BoundingBoxOcclusionModifier(int pointsPerEdge, int detectionThreshold) {
        this.pointsPerEdge = pointsPerEdge;
        this.detectionThreshold = detectionThreshold;
    }

    @Override
    public <T extends SpatialObject> List<T> apply(PerceptionModuleOwner owner, List<T> spatialObjects) {
        List<T> newObjects = new ArrayList<>();
        Map<VehicleObject, VehicleBoundingBox> boundingBoxes = createBoundingBoxes(owner, spatialObjects);
        for (T spatialObject : spatialObjects) {
            List<Vector3d> pointsToEvaluate = createPointsToEvaluate(spatialObject);
            final int requiredVisiblePoints = pointsToEvaluate.size() == 1 ? 1 : detectionThreshold;
            int numberOfPointsVisible = 0;
            for (Vector3d point : pointsToEvaluate) {
                boolean pointOccluded = false;
                boundingBoxLoop:
                for (Map.Entry<VehicleObject, VehicleBoundingBox> occludingBoundingBoxEntry : boundingBoxes.entrySet()) {
                    if (occludingBoundingBoxEntry.getKey().getId().equals(spatialObject.getId())) {
                        continue; // cannot be occluded by itself
                    }
                    VehicleBoundingBox boundingBox = occludingBoundingBoxEntry.getValue();
                    for (Edge<Vector3d> vehicleSide : boundingBox.allEdges) {
                        boolean isOccluded = VectorUtils.computeXZEdgeIntersectionPoint(
                                owner.getVehicleData().getProjectedPosition().toVector3d(),
                                point, vehicleSide.a, vehicleSide.b, intersectionResult
                        );
                        if (isOccluded) {
                            pointOccluded = true;
                            break boundingBoxLoop;
                        }
                    }
                }
                if (!pointOccluded) {
                    numberOfPointsVisible++;
                }
                if (numberOfPointsVisible == requiredVisiblePoints) {
                    newObjects.add(spatialObject);
                    break;
                }
            }
        }
        return newObjects;
    }

    private static <T extends SpatialObject> Map<VehicleObject, VehicleBoundingBox> createBoundingBoxes(PerceptionModuleOwner owner, List<T> spatialObjects) {
        Map<VehicleObject, VehicleBoundingBox> boundingBoxes = new HashMap<>();
        for (T spatialObject : spatialObjects) {
            if (!(spatialObject instanceof VehicleObject)) { // skip non-vehicle spatial object
                continue;
            }
            if (spatialObject.getId().equals(owner.getId())) { // skip calculation of own bounding box
                continue;
            }
            VehicleObject currObject = (VehicleObject) spatialObject;
            boundingBoxes.put(currObject, VehicleBoundingBox.createFromVehicleObject(currObject));
        }
        return boundingBoxes;
    }

    /**
     * Creates a map of all points that shall be tested for occlusion. Initially, all booleans are set to {@code false} indicating
     * that they're NOT occluded.
     *
     * @param spatialObject a {@link SpatialObject} for which the occlusion should be evaluated
     */
    private <T extends SpatialObject> List<Vector3d> createPointsToEvaluate(T spatialObject) {
        List<Vector3d> pointsToEvaluate = new ArrayList<>();
        if (!(spatialObject instanceof VehicleObject)) {
            // for all objects that don't have a bounding box, just evaluate the position for occlusion
            pointsToEvaluate.add(spatialObject);
        } else {
            // for vehicles evaluate the four corners
            pointsToEvaluate.addAll(VehicleBoundingBox.createFromVehicleObject((VehicleObject) spatialObject).allCorners);
        }
        return pointsToEvaluate;
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
        private final List<Vector3d> allCorners;
        private final Vector3d frontRightCorner;
        private final Vector3d backRightCorner;
        private final Vector3d backLeftCorner;
        private final Vector3d frontLeftCorner;

        private final List<Edge<Vector3d>> allEdges;
        private final Edge<Vector3d> frontEdge;
        private final Edge<Vector3d> rightEdge;
        private final Edge<Vector3d> backEdge;
        private final Edge<Vector3d> leftEdge;

        private VehicleBoundingBox(Vector3d frontRightCorner, Vector3d backRightCorner, Vector3d backLeftCorner, Vector3d frontLeftCorner) {
            this.frontRightCorner = frontRightCorner;
            this.backRightCorner = backRightCorner;
            this.backLeftCorner = backLeftCorner;
            this.frontLeftCorner = frontLeftCorner;
            allCorners = Lists.newArrayList(frontRightCorner, backRightCorner, backLeftCorner, frontLeftCorner);
            frontEdge = new Edge<>(frontLeftCorner, frontRightCorner);
            rightEdge = new Edge<>(frontRightCorner, backRightCorner);
            backEdge = new Edge<>(backRightCorner, backLeftCorner);
            leftEdge = new Edge<>(backLeftCorner, frontLeftCorner);
            allEdges = Lists.newArrayList(frontEdge, rightEdge, backEdge, leftEdge);
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
    }
}
