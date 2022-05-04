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

package org.eclipse.mosaic.fed.application.ambassador.simulation.communication;

import org.eclipse.mosaic.fed.application.app.api.communication.CommunicationModuleConfiguration;

public abstract class AbstractCommunicationModuleConfiguration implements CommunicationModuleConfiguration {

    Long camMinimalPayloadLength = null;

    /**
     * Sets the minimal payload length to assume for CAMs. Unit: Bytes
     *
     * @param minimalPayloadLength number of bytes
     */
    abstract AbstractCommunicationModuleConfiguration camMinimalPayloadLength(long minimalPayloadLength);

}
