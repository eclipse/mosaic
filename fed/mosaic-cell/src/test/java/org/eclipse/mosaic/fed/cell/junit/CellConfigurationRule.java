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

package org.eclipse.mosaic.fed.cell.junit;

import org.eclipse.mosaic.fed.cell.config.CCell;
import org.eclipse.mosaic.fed.cell.config.CNetwork;
import org.eclipse.mosaic.fed.cell.config.CRegion;
import org.eclipse.mosaic.fed.cell.config.util.ConfigurationReader;
import org.eclipse.mosaic.fed.cell.data.ConfigurationData;
import org.eclipse.mosaic.fed.cell.utility.RegionUtility;
import org.eclipse.mosaic.lib.util.junit.TestUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class CellConfigurationRule extends TemporaryFolder {

    private String regionConfigPath;
    private String networkConfigPath;
    private String cellConfigPath;

    boolean testRunning = false;

    public CellConfigurationRule withRegionConfig(String regionConfigPath) {
        Validate.isTrue(!testRunning, "Rule cannot be configured during test");
        this.regionConfigPath = regionConfigPath;
        return this;
    }

    public CellConfigurationRule withNetworkConfig(String networkConfigPath) {
        Validate.isTrue(!testRunning, "Rule cannot be configured during test");
        this.networkConfigPath = networkConfigPath;
        return this;
    }

    public CellConfigurationRule withCellConfig(String cellConfigPath) {
        Validate.isTrue(!testRunning, "Rule cannot be configured during test");
        this.cellConfigPath = cellConfigPath;
        return this;
    }

    private CCell cellConfig = null;
    private CRegion regionConfig = null;
    private CNetwork networkConfig = null;

    public CCell getCellConfig() {
        return cellConfig;
    }

    public CRegion getRegionConfig() {
        return regionConfig;
    }

    public CNetwork getNetworkConfig() {
        return networkConfig;
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        File targetFolder = super.newFolder();

        if (cellConfigPath != null) {
            File cellConfigFile = copyToFile(cellConfigPath, targetFolder);
            cellConfig = ConfigurationReader.importCellConfig(cellConfigFile.getAbsolutePath());
            ConfigurationData.INSTANCE.setCellConfig(cellConfig);
        }

        if (regionConfigPath != null) {
            File regionConfigFile = copyToFile(regionConfigPath, targetFolder);
            regionConfig = ConfigurationReader.importRegionConfig(regionConfigFile.getAbsolutePath());
            ConfigurationData.INSTANCE.setRegionConfig(regionConfig);
            RegionUtility.initializeRegionsIndex(regionConfig.regions);
        }

        if (networkConfigPath != null) {
            File networkConfigFile = copyToFile(networkConfigPath, targetFolder);
            networkConfig = ConfigurationReader.importNetworkConfig(networkConfigFile.getAbsolutePath());
            ConfigurationData.INSTANCE.setNetworkConfig(networkConfig);
        }
        testRunning = true;
    }

    @Override
    protected void after() {
        TestUtils.setPrivateField(ConfigurationData.INSTANCE, "cellAmbassadorConfig", null);
        cellConfig = null;

        TestUtils.setPrivateField(ConfigurationData.INSTANCE, "regionConfig", null);
        regionConfig = null;

        TestUtils.setPrivateField(ConfigurationData.INSTANCE, "networkConfig", null);
        networkConfig = null;

        testRunning = false;

        super.after();
    }

    private File copyToFile(String sourcePath, File targetFolder) throws IOException {
        File targetFile = new File(targetFolder, StringUtils.substringAfterLast(sourcePath, "/"));
        try (
                Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("/" + sourcePath), StandardCharsets.UTF_8);
                Writer writer = new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8)
        ) {
            IOUtils.copy(reader, writer);
        }
        return targetFile;
    }
}
