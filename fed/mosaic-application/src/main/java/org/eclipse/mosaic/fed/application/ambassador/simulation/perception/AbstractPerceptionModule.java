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


package org.eclipse.mosaic.fed.application.ambassador.simulation.perception;

import org.eclipse.mosaic.fed.application.app.api.perception.PerceptionModule;
import org.eclipse.mosaic.fed.application.app.api.perception.PerceptionModuleConfiguration;

import org.slf4j.Logger;

public abstract class AbstractPerceptionModule<ConfigT extends PerceptionModuleConfiguration> implements PerceptionModule<ConfigT> {

    protected final PerceptionModuleOwner<ConfigT> owner;

    protected final Logger log;


    protected AbstractPerceptionModule(PerceptionModuleOwner<ConfigT> owner, Logger log) {
        this.owner = owner;
        this.log = log;
    }
}
