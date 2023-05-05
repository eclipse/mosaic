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

package org.eclipse.mosaic.fed.application.ambassador.util;

import org.eclipse.mosaic.fed.application.app.api.OperatingSystemAccess;
import org.eclipse.mosaic.fed.application.app.api.os.OperatingSystem;

import org.slf4j.Logger;

import java.nio.file.Path;
import javax.annotation.Nullable;

/**
 * Log facade for units and their applications, provides various convenience functions on top of the general {@link Logger} interface.
 */
public interface UnitLogger extends Logger {

    /**
     * Convenience method to uniformly print the log statement including the current simulation time.
     */
    void infoSimTime(OperatingSystemAccess<? extends OperatingSystem> os, String format, Object... arguments);

    /**
     * Convenience method to uniformly print the log statement including the current simulation time.
     */
    void debugSimTime(OperatingSystemAccess<? extends OperatingSystem> os, String format, Object... arguments);

    /**
     * Convenience method to uniformly print the log statement including the current simulation time.
     */
    void warnSimTime(OperatingSystemAccess<? extends OperatingSystem> os, String format, Object... arguments);

    /**
     * Convenience method to determine the current path of the log files generated for this unit.
     * Returns {@code null} if no appender for the logging could be found.
     */
    @Nullable
    Path getUnitLogDirectory();

}
