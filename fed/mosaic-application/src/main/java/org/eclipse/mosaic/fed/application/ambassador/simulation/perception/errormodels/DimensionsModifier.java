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
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.index.objects.VehicleObject;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.math.Vector3d;

import java.util.List;

public class DimensionsModifier implements PerceptionModifier {

    private static final double SIGMA_WIDTH_OFFSET = 0.2; // given in m
    private static final double SIGMA_HEIGHT_OFFSET = 0.2; // given in m
    private static final double SIGMA_LENGTH_OFFSET = 0.5; // given in m

    private final double heightOffset;
    private final double widthOffset;
    private final double lengthOffset;

    private final RandomNumberGenerator rng;


    public DimensionsModifier(RandomNumberGenerator rng, double heightOffset, double widthOffset, double lengthOffset) {
        this.rng = rng;
        this.heightOffset = heightOffset;
        this.widthOffset = widthOffset;
        this.lengthOffset = lengthOffset;
    }

    public DimensionsModifier(RandomNumberGenerator rng) {
        this.rng = rng;
        this.heightOffset = SIGMA_HEIGHT_OFFSET;
        this.widthOffset = SIGMA_WIDTH_OFFSET;
        this.lengthOffset = SIGMA_LENGTH_OFFSET;
    }

    @Override
    public <T extends SpatialObject> List<T> apply(PerceptionModuleOwner owner, List<T> spatialObjects) {
        final Vector3d ownerPosition = owner.getVehicleData().getProjectedPosition().toVector3d();

        spatialObjects.stream()
                .filter(o -> o instanceof VehicleObject)
                .forEach(o -> adjustDimensionsOfVehicle((VehicleObject) o));

        return spatialObjects;
    }

    private void adjustDimensionsOfVehicle(VehicleObject vehicleObject) {
        vehicleObject.setDimensions(
                Math.abs(rng.nextGaussian(vehicleObject.getLength(), lengthOffset)),
                Math.abs(rng.nextGaussian(vehicleObject.getHeight(), heightOffset)),
                Math.abs(rng.nextGaussian(vehicleObject.getWidth(), widthOffset))
        );
    }

}

