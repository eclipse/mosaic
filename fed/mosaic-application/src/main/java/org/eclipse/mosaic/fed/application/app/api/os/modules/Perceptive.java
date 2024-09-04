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

import org.eclipse.mosaic.fed.application.ambassador.simulation.perception.SimplePerceptionConfiguration;
import org.eclipse.mosaic.fed.application.app.api.perception.PerceptionModule;

/**
 * Interface to mark an {@link org.eclipse.mosaic.fed.application.app.api.os.OperatingSystem} as
 * an owner of a {@link PerceptionModule} to perceive surrounding vehicles, thus making it perceptive.
 *
 */
public interface Perceptive {

    /**
     * Returns the perception module of this unit.
     */
    PerceptionModule<SimplePerceptionConfiguration> getPerceptionModule();

}
