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

package org.eclipse.mosaic.rti.api;

public interface Interactable {

    /**
     * This method allows a caller to interact with other federates by sending an
     * interaction. Responsible to forward the interaction subscribers is the
     * interaction management.
     * After this method is called, the callers last granted time advance
     * request is canceled. Consequently, the caller is not allowed to advance
     * its own time to any time later than the interaction time before a new time
     * advance request is granted.
     *
     * @param interaction the interaction object to send to the other ambassadors in the federation
     * @throws IllegalValueException     if the interaction contains illegal values
     * @throws InternalFederateException if the interaction could not be published by the {@link InteractionManagement}
     */
    void triggerInteraction(Interaction interaction) throws IllegalValueException, InternalFederateException;
}
