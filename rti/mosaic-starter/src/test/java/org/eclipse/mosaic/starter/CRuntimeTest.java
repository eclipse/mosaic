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

package org.eclipse.mosaic.starter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.util.InteractionUtils;
import org.eclipse.mosaic.starter.config.CRuntime;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class CRuntimeTest {

    /**
     * Checks if the runtime.xml which is bundled with Eclipse MOSAIC has only valid subscriptions.
     * This test requires the maven call {@code mvn process-test-resources} or {@code mvn install} each
     * time the runtime.xml has been changed in the {@code bundle} project.
     */
    @Test
    public void validSubscriptionsInRuntimeConfiguration() throws IOException {
        CRuntime runtimeConfiguration;
        try (InputStream resource = getClass().getResourceAsStream("/etc/runtime.xml")) {
            assertNotNull(resource);
            runtimeConfiguration = new XmlMapper()
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(resource, CRuntime.class);
        }
        assertNotNull(runtimeConfiguration);

        Map<String, Class<?>> allSupportedInteractions = InteractionUtils.getAllSupportedInteractions();
        assertFalse(allSupportedInteractions.isEmpty());

        for (CRuntime.CFederate federate : runtimeConfiguration.federates) {
            assertFalse("Federate " + federate.id + " should have at least one subscription",
                    federate.subscriptions.isEmpty()
            );
            for (String subscription : federate.subscriptions) {
                assertTrue("\"" + subscription + "\" is not a valid subscription (see federate \"" + federate.id + "\")",
                        allSupportedInteractions.containsKey(subscription)
                );
            }
        }
    }
}
