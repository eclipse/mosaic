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

package org.eclipse.mosaic.rti.junit;

import org.eclipse.mosaic.rti.api.Interaction;

/**
 * This message class is used by the ambassadors for interaction between the
 * dummy implementations.
 */
public class TestDummyInteraction extends Interaction {

    private static final long serialVersionUID = 1L;

    private final String typeId;

    /**
     * Creates a new DummyMessage.
     *
     * @param time Simulation time at which the interaction happens in ns
     * @param typeId the type of the message
     */
    public TestDummyInteraction(long time, String typeId) {
        super(time);
        this.typeId = typeId;
    }

    @Override
    public String getTypeId() {
        return typeId;
    }
}
