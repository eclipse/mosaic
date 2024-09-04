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

package org.eclipse.mosaic.fed.application.app.api.os.modules;

import org.eclipse.mosaic.fed.application.ambassador.simulation.navigation.IRoutingModule;

/**
 * Interface to mark an {@link org.eclipse.mosaic.fed.application.app.api.os.OperatingSystem} as
 * an owner of a {@link IRoutingModule} to calculates routes from A to B, thus making it routable.
 */
public interface Routable {

    /**
     * Returns a routing module to calculate arbitrary routes from any point to any other.
     *
     * @return the {@link IRoutingModule} of the unit.
     */
    IRoutingModule getRoutingModule();
}
