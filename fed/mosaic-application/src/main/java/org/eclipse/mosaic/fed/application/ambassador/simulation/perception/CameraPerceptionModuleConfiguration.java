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

import org.eclipse.mosaic.fed.application.app.api.perception.PerceptionModuleConfiguration;

public class CameraPerceptionModuleConfiguration implements PerceptionModuleConfiguration {

    /**
     * Viewing angle of perception module. [degree]
     */
    private final double viewingAngle;

    /**
     * Viewing Range of the perception module. [m]
     */
    private final double viewingRange;

    public CameraPerceptionModuleConfiguration(double viewingAngle, double viewingRange) {
        this.viewingAngle = viewingAngle;
        this.viewingRange = viewingRange;
    }

    public double getViewingAngle() {
        return viewingAngle;
    }

    public double getViewingRange() {
        return viewingRange;
    }
}
