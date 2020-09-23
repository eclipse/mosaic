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

package org.eclipse.mosaic.fed.application.app.api.communication;

import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;

public interface CommunicationModule<TConf extends CommunicationModuleConfiguration> {

    /**
     * Finalize the process of sending a generic {@link V2xMessage},
     * which has already undergone several checks to be send on the specific interface (ad hoc or cell).
     *
     * @param msg V2X message to be sent
     */
    void sendV2xMessage(final V2xMessage msg);

    /**
     * sendCam either over adhoc or cellular.
     *
     * @return message id of the sent CAM
     */
    Integer sendCam();

    /**
     * Stores the communication module configuration for enabling
     * and sends configure message to the MOSAIC RTI.
     *
     * @param configuration communication module configuration
     */
    void enable(TConf configuration);

    /**
     * Resets the communication module configuration (to null) and sends a disabling configure message to the RTI.
     */
    void disable();

    /**
     * Returns whether module is on or off.
     *
     * @return whether module is on or off
     */
    boolean isEnabled();
}
