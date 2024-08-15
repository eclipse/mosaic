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

package org.eclipse.mosaic.fed.application.app.api.os;

import org.eclipse.mosaic.fed.application.app.api.os.modules.AdHocCommunicative;
import org.eclipse.mosaic.fed.application.app.api.os.modules.CellCommunicative;
import org.eclipse.mosaic.fed.application.app.api.os.modules.Locatable;
import org.eclipse.mosaic.fed.application.app.api.os.modules.Routable;

/**
 * This interface extends the basic {@link OperatingSystem} and
 * is implemented by the {@link org.eclipse.mosaic.fed.application.ambassador.simulation.RoadSideUnit}.
 */
public interface RoadSideUnitOperatingSystem
        extends OperatingSystem, Locatable, Routable, CellCommunicative, AdHocCommunicative {

}
