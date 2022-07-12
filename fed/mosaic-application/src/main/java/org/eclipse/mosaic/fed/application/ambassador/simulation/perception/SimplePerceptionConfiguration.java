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

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.errormodels.PerceptionModifier;
import org.eclipse.mosaic.fed.application.app.api.perception.PerceptionModuleConfiguration;

import com.google.common.collect.Lists;

import java.util.List;

public class SimplePerceptionConfiguration implements PerceptionModuleConfiguration {

    /**
     * Viewing angle of perception module. [degree]
     */
    private final double viewingAngle;

    /**
     * Viewing Range of the perception module. [m]
     */
    private final double viewingRange;

    private final List<PerceptionModifier> perceptionModifiers;

    public SimplePerceptionConfiguration(double viewingAngle, double viewingRange, PerceptionModifier... perceptionModifiers) {
        this.viewingAngle = viewingAngle;
        this.viewingRange = viewingRange;
        this.perceptionModifiers = Lists.newArrayList(perceptionModifiers);
    }

    public double getViewingAngle() {
        return viewingAngle;
    }

    @Override
    public double getViewingRange() {
        return viewingRange;
    }

    public List<PerceptionModifier> getPerceptionModifiers() {
        return perceptionModifiers;
    }
}
