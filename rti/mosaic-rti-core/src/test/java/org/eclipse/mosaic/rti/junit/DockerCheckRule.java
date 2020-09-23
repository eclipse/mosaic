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

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.IOException;

public class DockerCheckRule implements TestRule {

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                int statusDockerInfo = checkForDockerRunning();
                if (statusDockerInfo != 0) {
                    throw new AssumptionViolatedException("Docker is not running (status code=" + statusDockerInfo + "). Skipping tests.");
                } else {
                    statement.evaluate();
                }
            }
        };
    }

    private int checkForDockerRunning() {
        try {
            return Runtime.getRuntime().exec(new String[]{
                    "docker", "info"
            }, null).waitFor();
        } catch (IOException | InterruptedException e) {
            return -1;
        }

    }
}
