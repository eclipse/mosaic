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

package org.eclipse.mosaic.fed.application.ambassador.util;

import org.eclipse.mosaic.fed.application.app.api.OperatingSystemAccess;
import org.eclipse.mosaic.fed.application.app.api.os.OperatingSystem;

import org.slf4j.Logger;

/**
 * Log facade for units and their applications.
 */
public interface UnitLogger extends Logger {

    void infoSimTime(OperatingSystemAccess<? extends OperatingSystem> os, String format, Object... arguments);

    void debugSimTime(OperatingSystemAccess<? extends OperatingSystem> os, String format, Object... arguments);

    void warnSimTime(OperatingSystemAccess<? extends OperatingSystem> os, String format, Object... arguments);
}
