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

package org.eclipse.mosaic.lib.junit;

import org.eclipse.mosaic.lib.objects.v2x.etsi.EtsiPayloadConfiguration;
import org.eclipse.mosaic.lib.util.junit.TestUtils;

import com.google.gson.JsonSyntaxException;
import org.junit.Assert;
import org.junit.rules.ExternalResource;

public class EtsiPayloadConfigurationRule extends ExternalResource {

    @Override
    protected void before() {
        try {
            EtsiPayloadConfiguration.setPayloadConfiguration(new EtsiPayloadConfiguration(true));
        } catch (JsonSyntaxException e) {
            Assert.fail("Test ambassador invalid");
        }
    }

    @Override
    protected void after() {
        TestUtils.setPrivateField(EtsiPayloadConfiguration.class, "globalConfiguration", null);
    }

}