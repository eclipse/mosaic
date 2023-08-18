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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects;

import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.math.VectorUtils;
import org.eclipse.mosaic.lib.spatial.Edge;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * An object representing a vehicles' 2D bounding box.
 * <pre>
 *   frontRightCorner----------rightEdge----------backRightCorner
 *                  |                             |
 *   Heading <== frontEdge                     backEdge
 *                  |                             |
 *    frontLeftCorner----------leftEdge-----------backLeftCorner
 * </pre>
 */
public class VehicleBoundingBox implements SpatialObjectBoundingBox {
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

    @Override
    public List<Vector3d> getAllCorners() {
        return allCorners;
    }

    @Override
    public List<Edge<Vector3d>> getAllEdges() {
        return allEdges;
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
