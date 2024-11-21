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

import org.eclipse.mosaic.fed.application.app.api.navigation.NavigationModule;

/**
 * Interface to mark an {@link org.eclipse.mosaic.fed.application.app.api.os.OperatingSystem} as
 * an owner of a {@link NavigationModule} to calculate routes from its current position to any target,
 * thus making it navigable.
 */
public interface Navigable extends Locatable {

    /**
     * Returns a navigation module to calculate and switch routes to any target.
     *
     * @return the {@link NavigationModule} of this unit.
     */
    NavigationModule getNavigationModule();
}
