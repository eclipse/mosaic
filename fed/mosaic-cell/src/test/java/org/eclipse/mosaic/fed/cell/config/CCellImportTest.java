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

package org.eclipse.mosaic.fed.cell.config;

import static org.junit.Assert.assertEquals;

import org.eclipse.mosaic.fed.cell.config.util.ConfigurationReader;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import org.junit.Test;

import java.io.File;

/**
 * Mainly tests cell config.
 */
public class CCellImportTest {
    private final static String CELL_CONF_PATH =
            "src" + File.separator + "test" + File.separator + "resources"
                    + File.separator + "configs" + File.separator + "sample_cell.json";
    private final static String CELL_CONF_PATH_INVALID =
            "src" + File.separator + "test" + File.separator + "resources"
                    + File.separator + "configs" + File.separator + "sample_cell_invalid.json";

    private CCell getCellConfig() throws InternalFederateException {
        // Read the region configuration file
        return ConfigurationReader.importCellConfig(CELL_CONF_PATH);
    }

    @Test(expected = InternalFederateException.class)
    public void checkInvalidCellConfig() throws InternalFederateException {
        ConfigurationReader.importCellConfig(CELL_CONF_PATH_INVALID);
    }

    @Test
    public void checkCellConfigAsExpected() throws InternalFederateException {
        CCell cellConfig = getCellConfig();
        assertEquals("network.json", cellConfig.networkConfigurationFile);
        assertEquals("regions.json", cellConfig.regionConfigurationFile);
    }
}
