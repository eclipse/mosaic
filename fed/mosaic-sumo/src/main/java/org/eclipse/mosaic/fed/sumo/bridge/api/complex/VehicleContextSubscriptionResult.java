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

package org.eclipse.mosaic.fed.sumo.bridge.api.complex;

import java.util.ArrayList;
import java.util.List;

/**
 * This class summarizes the results of the vehicle subscription.
 */
public class VehicleContextSubscriptionResult extends AbstractSubscriptionResult {
    /**
     * A list of subscription result generated from the context subscription
     * (e.g. all {@link VehicleSubscriptionResult}s in the field of vision of a specific vehicle)
     */
    public List<VehicleSubscriptionResult> contextSubscriptions = new ArrayList<>();
}