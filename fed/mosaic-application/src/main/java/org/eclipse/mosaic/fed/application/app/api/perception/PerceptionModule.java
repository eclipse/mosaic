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

package org.eclipse.mosaic.fed.application.app.api.perception;

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.CameraPerceptionModuleConfiguration;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;

import java.util.List;

public interface PerceptionModule  {

    void enable(CameraPerceptionModuleConfiguration configuration);

    List<VehicleData> getEnvironmentVehicles();
}
