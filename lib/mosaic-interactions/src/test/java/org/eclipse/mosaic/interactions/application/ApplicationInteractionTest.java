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

package org.eclipse.mosaic.interactions.application;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ApplicationInteractionTest {

    @Test
    public void typeId() {
        ApplicationInteraction applicationInteraction = new TestApplicationInteraction();

        assertEquals(ApplicationInteraction.TYPE_ID, applicationInteraction.getTypeId());
    }

    private static class TestApplicationInteraction extends ApplicationInteraction {

        private TestApplicationInteraction() {
            super(0, null);
        }
    }
}