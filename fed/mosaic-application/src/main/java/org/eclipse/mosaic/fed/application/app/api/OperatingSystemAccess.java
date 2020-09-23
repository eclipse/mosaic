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
 */

package org.eclipse.mosaic.fed.application.app.api;

import org.eclipse.mosaic.fed.application.app.api.os.OperatingSystem;

/**
 * This interface is to be used to access the operating system of a
 * unit.
 *
 * @param <OS> the type of the {@link OperatingSystem} to be accessed
 */
@SuppressWarnings("checkstyle:InterfaceTypeParameterName")
public interface OperatingSystemAccess<OS extends OperatingSystem> extends Application {

    /**
     * Returns the operating system for this application.
     *
     * @return the operating system.
     */
    OS getOperatingSystem();

    /**
     * Returns the operating system for this application. This is
     * equivalent to {@link #getOperatingSystem()}
     *
     * @return the operating system.
     * @see #getOperatingSystem()
     */
    default OS getOs() {
        return getOperatingSystem();
    }
}
