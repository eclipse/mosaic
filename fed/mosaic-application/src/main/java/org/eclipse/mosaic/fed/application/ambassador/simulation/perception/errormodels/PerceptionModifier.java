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
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.VehicleObject;
import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.util.PerceptionModifierTypeAdapterFactory;

import com.google.gson.annotations.JsonAdapter;

import java.util.List;

@JsonAdapter(PerceptionModifierTypeAdapterFactory.PerceptionModifierTypeAdapter.class)
public interface PerceptionModifier {

    /**
     * Applies the implemented filter/modifier.
     *
     * @return the filtered/modified list
     */
    List<VehicleObject> apply(PerceptionModuleOwner owner, List<VehicleObject> vehicleObjects);
}
