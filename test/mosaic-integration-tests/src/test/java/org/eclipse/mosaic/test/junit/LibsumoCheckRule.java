/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.test.junit;

import org.eclipse.mosaic.fed.sumo.ambassador.LibSumoAmbassador;
import org.eclipse.mosaic.rti.config.CLocalHost;

import org.apache.commons.lang3.StringUtils;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;

public class LibsumoCheckRule implements TestRule {

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                boolean libsumoAvailable = checkForLibsumoAvailable();
                if (!libsumoAvailable) {
                    throw new AssumptionViolatedException("Library 'Libsumo' is not available. Skipping tests.");
                }
                if (!LibSumoAmbassador.correctLibSumoVersion()) {
                    throw new AssumptionViolatedException("Current SUMO version at " + getSumoExecutable("sumo") + " is not supported with LibSumo.");
                }
                statement.evaluate();
            }
        };
    }

    private boolean checkForLibsumoAvailable() {
        CLocalHost.OperatingSystem operatingSystem = CLocalHost.OperatingSystem.getSystemOperatingSystem();
        String libsumoLibrary;
        if (operatingSystem == CLocalHost.OperatingSystem.WINDOWS) {
            libsumoLibrary = getSumoExecutable("libsumojni.dll");
        } else {
            libsumoLibrary = getSumoExecutable("liblibsumojni.so");
        }
        return new File(libsumoLibrary).exists();
    }

    private String getSumoExecutable(String executable) {
        String sumoHome = System.getenv("SUMO_HOME");
        if (StringUtils.isNotBlank(sumoHome)) {
            return sumoHome + File.separator + "bin" + File.separator + executable;
        }
        return executable;
    }
}
