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

package org.eclipse.mosaic.fed.application.ambassador.navigation;

import static org.mockito.Mockito.mock;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.config.CApplicationAmbassador;
import org.eclipse.mosaic.lib.routing.Routing;
import org.eclipse.mosaic.lib.util.junit.TestUtils;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.RtiAmbassador;
import org.eclipse.mosaic.rti.api.parameters.AmbassadorParameter;

import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import java.io.File;

public class CentralNavigationComponentTestRule extends ExternalResource {


    private final static String configFile = "/application_config.json";
    private TemporaryFolder folderRule;
    private Routing routingMock = mock(Routing.class);
    private CentralNavigationComponent centralNavigationComponent;
    private RtiAmbassador rtiAmbassadorMock;

    CentralNavigationComponentTestRule(TemporaryFolder folderRule) {
        this.folderRule = folderRule;
    }

    CentralNavigationComponent getCentralNavigationComponent() {
        return centralNavigationComponent;
    }

    RtiAmbassador getRtiAmbassadorMock() {
        return rtiAmbassadorMock;
    }

    public Routing getRoutingMock() {
        return routingMock;
    }

    @Override
    protected void before() throws Throwable {
        final File configCopy = folderRule.newFile(configFile);
        FileUtils.copyInputStreamToFile(getClass().getResourceAsStream(configFile), configCopy);

        CApplicationAmbassador applicationConfig = new CApplicationAmbassador();
        AmbassadorParameter ambassadorParameters = new AmbassadorParameter("test", configCopy.getParentFile());
        centralNavigationComponent = new CentralNavigationComponent(ambassadorParameters, applicationConfig.navigationConfiguration) {
            @Override
            Routing createFromType(String type) throws InternalFederateException {
                return routingMock;
            }
        };
        SimulationKernel.SimulationKernel.setConfiguration(applicationConfig);
        rtiAmbassadorMock = mock(RtiAmbassador.class);
    }

    @Override
    protected void after() {
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "configuration", null);
    }

}
