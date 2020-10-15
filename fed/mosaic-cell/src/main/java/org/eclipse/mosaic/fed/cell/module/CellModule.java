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

package org.eclipse.mosaic.fed.cell.module;

import org.eclipse.mosaic.fed.cell.chain.ChainManager;
import org.eclipse.mosaic.lib.util.scheduling.EventProcessor;

/**
 * The base module in the cellular simulator.
 * It has its origins in the {@link EventProcessor} so that all inheriting modules
 * are able to take part in the event scheduling.
 */
public abstract class CellModule implements EventProcessor {

    /**
     * The specific name (of the inheriting module).
     */
    protected final String moduleName;

    /**
     * Reference to the {@link ChainManager}.
     * The specific {@link CellModule}s are interlinked directly, but always communicate with the {@link ChainManager}
     */
    protected final ChainManager chainManager;

    public CellModule(String moduleName, ChainManager chainManager) {
        this.moduleName = moduleName;
        this.chainManager = chainManager;
    }

    public String getModuleName() {
        return moduleName;
    }

    public abstract long getProcessedMessages();
}
