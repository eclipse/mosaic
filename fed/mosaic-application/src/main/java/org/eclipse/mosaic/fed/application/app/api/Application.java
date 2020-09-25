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

import org.eclipse.mosaic.lib.util.scheduling.EventProcessor;

/**
 * All basic applications need to implement this interface, for the
 * application simulator to properly handle them.
 */
public interface Application extends EventProcessor {

    /**
     * The operating system calls this method to notify that the application is started.
     */
    void onStartup();

    /**
     * The operating system calls this method to notify that the application is going to be torn down.
     */
    void onShutdown();
}
