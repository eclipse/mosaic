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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception.errormodels;

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.PerceptionModuleOwner;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.SpatialObject;
import org.eclipse.mosaic.lib.math.MathUtils;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.spatial.Edge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A simple perception modifier which removes all vehicles behind walls.
 * A list of walls in the vicinity of the ego vehicle is provided by the
 * {@link org.eclipse.mosaic.fed.application.ambassador.simulation.perception.SimplePerceptionModule SimplePerceptionModule}.
 * The check for hidden vehicles is done by finding intersection of vectors between
 * ego and all other vehicles and all walls in its vicinity.
 */
public class WallOcclusion implements PerceptionModifier {

    @Override
    public <T extends SpatialObject> List<T> apply(PerceptionModuleOwner owner, List<T> spatialObjects) {
        if (spatialObjects.isEmpty()) {
            return spatialObjects;
        }

        final Collection<Edge<Vector3d>> walls = owner.getPerceptionModule().getSurroundingWalls();
        if (walls.isEmpty()) {
            return spatialObjects;
        }
        Vector3d ownerPosition = owner.getVehicleData().getProjectedPosition().toVector3d();
        final List<T> result = new ArrayList<>();
        for (T spatialObject : spatialObjects) {
            List<Vector3d> pointsToEvaluate = spatialObject.getBoundingBox().getAllCorners();
            // we say that at least half of the corners have to be visible rounding up for odd numbers
            final int requiredVisiblePoints = (int) Math.ceil((double) pointsToEvaluate.size() / 2);
            int numberOfPointsVisible = 0;
            for (Vector3d point : pointsToEvaluate) {
                boolean pointOccluded = false;
                for (Edge<Vector3d> wall : walls) {
                    // SpatialObjects with PointBoundingBoxes won't occlude anything, as they have no edges defined
                    if (doIntersect(ownerPosition.x, ownerPosition.z, point.x, point.z, wall.a.x, wall.a.z, wall.b.x, wall.b.z)) {
                        pointOccluded = true;
                        break;
                    }
                }
                if (!pointOccluded) {
                    numberOfPointsVisible++;
                }
                if (numberOfPointsVisible == requiredVisiblePoints) {
                    result.add(spatialObject);
                    break;
                }
            }
        }
        return result;
    }

    private enum Orientation {
        COLLINEAR, CLOCKWISE, COUNTERCLOCKWISE
    }

    // Function to calculate the orientation of the triplet (px, py), (qx, qy), (rx, ry).
    static Orientation calcOrientation(double px, double py, double qx, double qy, double rx, double ry) {
        double val = (qy - py) * (rx - qx) - (qx - px) * (ry - qy);
        if (Math.abs(val) <= MathUtils.EPSILON_D) { // Collinear
            return Orientation.COLLINEAR;
        }
        return (val > 0) ? Orientation.CLOCKWISE : Orientation.COUNTERCLOCKWISE; // Clockwise or Counterclockwise
    }

    // Function to check if point (qx, qy) lies on segment (px, py) to (rx, ry).
    static boolean isOnSegment(double px, double py, double qx, double qy, double rx, double ry) {
        return qx <= Math.max(px, rx)
                && qx >= Math.min(px, rx)
                && qy <= Math.max(py, ry)
                && qy >= Math.min(py, ry);
    }

    // Function to check if two lines (p1, q1) and (p2, q2) intersect.
    static boolean doIntersect(double p1x, double p1y, double q1x, double q1y,
                               double p2x, double p2y, double q2x, double q2y) {
        Orientation o1 = calcOrientation(p1x, p1y, q1x, q1y, p2x, p2y);
        Orientation o2 = calcOrientation(p1x, p1y, q1x, q1y, q2x, q2y);
        Orientation o3 = calcOrientation(p2x, p2y, q2x, q2y, p1x, p1y);
        Orientation o4 = calcOrientation(p2x, p2y, q2x, q2y, q1x, q1y);

        // General case
        if (o1 != o2 && o3 != o4) {
            return true;
        }

        // Special cases
        if (o1 == Orientation.COLLINEAR && isOnSegment(p1x, p1y, p2x, p2y, q1x, q1y)) return true;
        if (o2 == Orientation.COLLINEAR && isOnSegment(p1x, p1y, q2x, q2y, q1x, q1y)) return true;
        if (o3 == Orientation.COLLINEAR && isOnSegment(p2x, p2y, p1x, p1y, q2x, q2y)) return true;
        if (o4 == Orientation.COLLINEAR && isOnSegment(p2x, p2y, q1x, q1y, q2x, q2y)) return true;

        return false; // Doesn't fall in any of the above cases
    }

}
