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


import java.io.File;
import java.io.IOException;

/**
 * This jar is bundled in "src/test/resources/TestFederate.jar" and will
 * be executed by {@link org.eclipse.mosaic.rti.federation.LocalFederationManagementTest}.<br>
 * <br>
 * This process writes an empty file in its working directory.
 * The {@link org.eclipse.mosaic.rti.federation.LocalFederationManagementTest} can then check if this process has been executed.
 */
public class TestFederateProcess {

    public static void main(String[] args) throws Exception {
        File touchFile = new File("federate.test.file");
        if (!touchFile.createNewFile() && !touchFile.setLastModified(System.currentTimeMillis())) {
            throw new IOException("Could not create " + touchFile);
        }
    }
}
