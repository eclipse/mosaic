/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.spatial;

import org.eclipse.mosaic.lib.math.Matrix3d;
import org.eclipse.mosaic.lib.math.Vector3d;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class represents a point cloud based on the {@link Vector3d} coordinate system.
 * <ul> <li>It consists of a reference point which represents the origin/center of this point cloud
 * in absolute world coordinates.</li>
 * <li>A rotation matrix used to translate relative end points of the point cloud to absolute coordinates.</li>
 * <li>A list of points (x,y,z, hit type, distance to origin) forming this point cloud.</li>
 * <li>Use {@link Relative} to create a point cloud by a given list of points in relative coordinates.
 *     When {@link #getAbsoluteEndPoints} is called on such object, a coordinate transformation is done (and cached).</li>
 * <li>Use {@link Absolute} to create a point cloud by a given list of points in absolute coordinates.
 *     When {@link #getRelativeEndPoints()} is called on such object, a coordinate transformation is done (and cached).</li>
 * </ul>
 */
public abstract class PointCloud implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Vector3d reference;
    private final RotationMatrix rotation;
    private final long creationTime;

    protected final List<Point> points;

    protected PointCloud(long creationTime, Vector3d reference, RotationMatrix rotation, List<Point> points) {
        this.creationTime = creationTime;
        this.reference = reference;
        this.rotation = rotation;
        this.points = points;
    }

    /**
     * Returns the simulation time when the {@link PointCloud} was created.
     *
     * @return time in nanoseconds
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * A {@link PointCloud} is represented by a constant origin and their end points
     * returned by {@link #getRelativeEndPoints()} and {@link #getAbsoluteEndPoints()}
     * Relative coordinates are translated and rotated absolute coordinates.
     *
     * @return origin of the rays forming this point cloud in absolute coordinates
     * @see #getRotation()
     */
    public Vector3d getReference() {
        return reference;
    }

    /**
     * Ray end points in relative coordinates of this point cloud are translated and rotated end points in
     * absolute coordinates.
     * <p/>
     * Let o be the ray origin in absolute coordinates, let R be the rotation matrix. Thus, the transformation
     * of relative coordinates r into absolute coordinates a is
     * <code>a = R * r + o</code>
     *
     * @return rotation matrix R
     * @see #getReference() returns <code>o</code>
     */
    public RotationMatrix getRotation() {
        return rotation;
    }

    /**
     * Returns all end points of all rays of this {@link PointCloud} that hit something in relative coordinates.
     * Relative coordinates mean the use of a cartesian coordinate system with its origin (0,0,0) at
     * the {@link PointCloud}'s reference, returned by {@link #getReference()}. In addition, the
     * coordinate system is rotated. See {@link #getRotation()} for details.
     *
     * @return end points of all rays of this {@link PointCloud} in relative coordinates
     * @see #getReference() origin of ray / translation of coordinates
     * @see #getRotation() rotation of coordinates
     */
    public abstract List<Point> getRelativeEndPoints();


    /**
     * Returns all end points of all rays of this {@link PointCloud} that hit something in relative coordinates.
     * Relative coordinates mean the use of a cartesian coordinate system with its origin (0,0,0) at
     * the {@link PointCloud}'s reference, returned by {@link #getReference()}. In addition, the
     * coordinate system is rotated. See {@link #getRotation()} for details.
     *
     * @return end points of all rays of this {@link PointCloud} in relative coordinates
     * @see #getReference() origin of ray / translation of coordinates
     * @see #getRotation() rotation of coordinates
     */
    public abstract List<Point> getRelativeEndPointsWithHit();

    /**
     * Returns all end points of all rays of this {@link PointCloud} in absolute coordinates.
     * Absolute coordinates represent world coordinates, and have {@link #getReference()} and {@link #getRotation()}
     * already applied.
     *
     * @return a list of points in absolute coordinates
     */
    public abstract List<Point> getAbsoluteEndPoints();

    /**
     * Returns all end points of all rays of this {@link PointCloud} that hit something in absolute coordinates.
     * Absolute coordinates represent world coordinates, and have {@link #getReference()} and {@link #getRotation()}
     * already applied.
     *
     * @return a list of points in absolute coordinates
     */
    public abstract List<Point> getAbsoluteEndPointsWithHit();

    /**
     * A {@link Point} of the point cloud consists of its coordinates, an identifier
     * of the type of object the point has hit, and the distance to the point cloud origin.
     */
    public static class Point extends Vector3d {

        private static final long serialVersionUID = 1L;

        private final byte hitType;
        private final float distance;

        /**
         * @param endPoint the coordinates of the point cloud
         * @param distance the distance to the origin of the point cloud
         * @param hitType  the type of hit object represented by this point. 0 = no hit
         */
        public Point(Vector3d endPoint, float distance, byte hitType) {
            x = endPoint.x;
            y = endPoint.y;
            z = endPoint.z;
            this.distance = distance;
            this.hitType = hitType;
        }

        /**
         * @return true if the ray generating this {@link Point} has hit an object
         */
        public boolean hasHit() {
            return hitType != 0;
        }

        /**
         * @return the type of the object the ray generating this point has hit. (0 = no hit)
         */
        public byte getHitType() {
            return hitType;
        }

        /**
         * @return the distance to the origin of point cloud this point belongs to
         */
        public float getDistance() {
            return distance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Point other = (Point) o;
            return super.equals(other)
                    && this.hitType == other.hitType
                    && Float.compare(this.distance, other.distance) == 0;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + Byte.hashCode(hitType);
            result = 31 * result + Float.hashCode(distance);
            return result;
        }
    }

    public static class Absolute extends PointCloud {

        private static final long serialVersionUID = 1L;

        private transient List<Point> absoluteEndPointsWithHit = null;
        private transient List<Point> relativeEndPoints = null;
        private transient List<Point> relativeEndPointsWithHit = null;

        /**
         * Creates a point cloud with the list of points being in absolute coordinates.
         */
        public Absolute(long creationTime, Vector3d reference, RotationMatrix rotation, List<Point> absoluteEndPoints) {
            super(creationTime, reference, rotation, absoluteEndPoints);
        }

        @Override
        public List<Point> getAbsoluteEndPoints() {
            return points;
        }

        @Override
        public List<Point> getAbsoluteEndPointsWithHit() {
            if (absoluteEndPointsWithHit == null) {
                absoluteEndPointsWithHit = getAbsoluteEndPoints().stream().filter(Point::hasHit).collect(Collectors.toList());
            }
            return absoluteEndPointsWithHit;
        }

        @Override
        public List<Point> getRelativeEndPoints() {
            if (relativeEndPoints == null) {
                relativeEndPoints = absoluteToRelative(points, p -> true);
            }
            return relativeEndPoints;
        }

        @Override
        public List<Point> getRelativeEndPointsWithHit() {
            if (relativeEndPointsWithHit == null) {
                relativeEndPointsWithHit = absoluteToRelative(points, Point::hasHit);
            }
            return relativeEndPointsWithHit;
        }

        /**
         * Transforms list of {@link Point}s in absolute coordinates to relative coordinates.         *
         *
         * @param absoluteEndPoints end points in absolute coordinates
         * @return end points in relative coordinates
         */
        private List<Point> absoluteToRelative(List<Point> absoluteEndPoints, Predicate<Point> filter) {
            Matrix3d inv = getRotation().transpose(new Matrix3d());
            return absoluteEndPoints
                    .stream()
                    .filter(filter)
                    .map(point -> (Point) inv.multiply(point.subtract(getReference(), new Point(point, point.getDistance(), point.getHitType()))))
                    .collect(Collectors.toList());
        }
    }

    public static class Relative extends PointCloud {

        private static final long serialVersionUID = 1L;

        private transient List<Point> relativeEndPointsWithHit = null;
        private transient List<Point> absoluteEndPoints = null;
        private transient List<Point> absoluteEndPointsWithHit = null;

        /**
         * Creates a point cloud with the list of points being in relative coordinates.
         */
        public Relative(long creationTime, Vector3d reference, RotationMatrix rotation, List<Point> absoluteEndPoints) {
            super(creationTime, reference, rotation, absoluteEndPoints);
        }

        @Override
        public List<Point> getAbsoluteEndPoints() {
            if (absoluteEndPoints == null) {
                absoluteEndPoints = relativeToAbsolute(points, p -> true);
            }
            return absoluteEndPoints;
        }

        @Override
        public List<Point> getAbsoluteEndPointsWithHit() {
            if (absoluteEndPointsWithHit == null) {
                absoluteEndPointsWithHit = relativeToAbsolute(points, Point::hasHit);
            }
            return absoluteEndPointsWithHit;
        }

        @Override
        public List<Point> getRelativeEndPoints() {
            return points;
        }

        @Override
        public List<Point> getRelativeEndPointsWithHit() {
            if (relativeEndPointsWithHit == null) {
                relativeEndPointsWithHit = getRelativeEndPoints().stream().filter(Point::hasHit).collect(Collectors.toList());
            }
            return relativeEndPointsWithHit;
        }

        /**
         * Transforms list of {@link Point}s in relative coordinates to absolute coordinates.
         *
         * @param relativeEndpoints end points in relative coordinates
         * @return end points in absolute coordinates
         */
        private List<Point> relativeToAbsolute(List<Point> relativeEndpoints, Predicate<Point> filter) {
            return relativeEndpoints
                    .stream()
                    .filter(filter)
                    .map(point -> (Point) getRotation().multiply(new Point(point, point.getDistance(), point.getHitType())).add(getReference()))
                    .collect(Collectors.toList());
        }
    }
}
