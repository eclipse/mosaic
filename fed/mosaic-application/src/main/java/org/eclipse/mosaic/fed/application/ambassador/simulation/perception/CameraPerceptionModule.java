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

package org.eclipse.mosaic.fed.application.ambassador.simulation.perception;

import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.geo.CartesianRectangle;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.math.VectorUtils;
import org.eclipse.mosaic.lib.spatial.BoundingBox;

import org.slf4j.Logger;

import java.util.List;

public class CameraPerceptionModule extends AbstractPerceptionModule<CameraPerceptionModuleConfiguration> {

    private Camera camera;

    public CameraPerceptionModule(PerceptionModuleOwner<CameraPerceptionModuleConfiguration> owner, Logger log) {
        super(owner, log);
    }

    @Override
    public void enable(CameraPerceptionModuleConfiguration configuration) {
        if (configuration == null) {
            log.warn("Provided configuration is null.");
            configuration = new CameraPerceptionModuleConfiguration(40, 200);
        }
        this.camera = new Camera(this.owner.getId(), configuration);
    }

    @Override
    public List<VehicleObject> getPerceivedVehicles() {
        if (camera == null) {
            return null;
        }
        if (owner.getVehicleData() == null) {
            return null;
        }
        camera.updateOrigin(owner.getVehicleData().getProjectedPosition(), owner.getVehicleData().getHeading());
        SimulationKernel.SimulationKernel.getCentralPerceptionComponentComponent().updateSpatialIndices();
        // request all vehicles within the area of the field of view
        return SimulationKernel.SimulationKernel.getCentralPerceptionComponentComponent()
                .getSpatialIndex()
                .getVehiclesInRange(camera);
    }

    /**
     * Checks whether the pre-selection of vehicles actually fall in the viewing range of the
     * ego vehicle. Note: We use ego-vehicle position as origin.
     */
    static class Camera implements PerceptionRange {

        private final String ownerId;
        private final CameraPerceptionModuleConfiguration configuration;

        private final Vector3d origin = new Vector3d();
        private final Vector3d leftBoundVector = new Vector3d();
        private final Vector3d rightBoundVector = new Vector3d();
        /**
         * the axis-aligned bounding box around the sight area
         */
        private final BoundingBox sightAreaBoundingBox = new BoundingBox();

        /**
         * vector object used for temporary calculations, to avoid unnecessary object allocations
         */
        private final Vector3d tmpVector1 = new Vector3d();
        /**
         * vector object used for temporary calculations, to avoid unnecessary object allocations
         */
        private final Vector3d tmpVector2 = new Vector3d();

        Camera(String ownerId, CameraPerceptionModuleConfiguration configuration) {
            this.ownerId = ownerId;
            this.configuration = configuration;
        }

        void updateOrigin(CartesianPoint origin, double heading) {
            origin.toVector3d(this.origin);
            calculateCircularSectorEdges(heading);
            calculateBoundingBox();
        }

        CartesianRectangle getBoundingArea() {
            return new CartesianRectangle(sightAreaBoundingBox.min.toCartesian(), sightAreaBoundingBox.max.toCartesian());
        }

        @Override
        public BoundingBox getBoundingBox() {
            return sightAreaBoundingBox;
        }

        @Override
        public boolean isInRange(SpatialObject other) {
            if (other.getId().equals(this.ownerId)) { // cannot see itself
                return false;
            }
            tmpVector2.set(0, 0, 0);
            synchronized (tmpVector1) {
                other.getProjectedPosition().toVector3d(tmpVector1).subtract(origin); // convert position of other to relative point
                return VectorUtils.isLeftOfLine(tmpVector1, tmpVector2, leftBoundVector) // vehicle is left of right edge
                        && !VectorUtils.isLeftOfLine(tmpVector1, tmpVector2, rightBoundVector) // vehicle is right of left edge
                        && tmpVector1.magnitude() <= configuration.getViewingRange(); // other vehicle is in range
            }
        }

        /**
         * Calculates the two unit vectors circumscribing the circular sector of the viewing field.
         */
        private void calculateCircularSectorEdges(double heading) {
            synchronized (tmpVector1) {
                // getting the direction vector of the heading from origin
                Vector3d directionVector = VectorUtils.getDirectionVectorFromHeading(heading, tmpVector1);
                double viewingAngleRad = toRadians(configuration.getViewingAngle());

                // calculate length of the legs of the isosceles triangle given the height (viewing range) and vertex angle (viewing angle)
                double length = sqrt(pow(configuration.getViewingRange(), 2) / (1 - pow(sin(viewingAngleRad / 2), 2)));
                directionVector.multiply(length);

                // rotate the direction vector the right using new vector
                leftBoundVector.set(directionVector).rotate(-viewingAngleRad / 2, VectorUtils.UP);
                // rotate the direction vector to the left re-using direction vector
                rightBoundVector.set(directionVector).rotate(viewingAngleRad / 2, VectorUtils.UP);
            }
        }

        private void calculateBoundingBox() {
            synchronized (tmpVector1) {
                sightAreaBoundingBox.clear();
                sightAreaBoundingBox.add(
                        origin,
                        tmpVector1.set(origin).add(leftBoundVector),
                        tmpVector2.set(origin).add(rightBoundVector)
                );
            }
        }
    }
}
