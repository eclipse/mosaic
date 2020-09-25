/*
 * Copyright (c) 2020 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.application.app.api;

import org.eclipse.mosaic.lib.objects.trafficsign.TrafficSign;

public interface TrafficSignAwareApplication extends Application {

    /**
     * This method is called when the previous seen {@link TrafficSign} is not valid anymore for the vehicle.
     * (Requires the TrafficSignAmbassador to be active and configured properly)
     *
     * @param trafficSign the invalidated {@link TrafficSign}
     */
    void onTrafficSignInvalidated(TrafficSign<?> trafficSign);

    /**
     * This method is called when the vehicle has noticed a {@link TrafficSign} along its way.
     * (Requires the TrafficSignAmbassador to be active and configured properly)
     *
     * @param trafficSign the noted {@link TrafficSign}
     */
    void onTrafficSignNoticed(TrafficSign<?> trafficSign);
}
