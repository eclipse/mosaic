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

package org.eclipse.mosaic.fed.cell.viz;

import org.eclipse.mosaic.fed.cell.chain.ChainManager;
import org.eclipse.mosaic.fed.cell.config.CCell;
import org.eclipse.mosaic.fed.cell.data.ConfigurationData;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BandwidthMeasurementManager {

    private final Logger log;
    private File bandwidthMeasurementsDirectory;

    /**
     * Creates a new {@link BandwidthMeasurementManager} object.
     *
     * @param log Logs if the directory creation is not successful.
     */
    public BandwidthMeasurementManager(Logger log) {
        this.log = log;
        if (!ConfigurationData.INSTANCE.getCellConfig().bandwidthMeasurements.isEmpty()) {

            bandwidthMeasurementsDirectory = new File(determineParentDir(), "bandwidthMeasurements");
            if (!bandwidthMeasurementsDirectory.mkdirs()) {
                log.warn("Could not create directory for bandwidth measurements at {}", bandwidthMeasurementsDirectory);
            }
        }
    }

    /**
     * Creates a new stream listener for measurements.
     *
     * @param chainManager Object to manage the interaction between cells and MOSAIC.
     */
    public void createStreamListener(ChainManager chainManager) {
        for (CCell.BandwidthMeasurement measurement : ConfigurationData.INSTANCE.getCellConfig().bandwidthMeasurements) {
            if (measurement.fromRegion.equals("SENDATE")
                    && measurement.toRegion.equals("SENDATE")
                    && measurement.applicationClass.equals("SENDATE")) {
                // special mode for SENDATE simulations with many measurements (over 7000).
                // this generates measurements on demand and uses a fast measurement lookup
                chainManager.addStreamListener(new OnDemandPerRegionBandwidthMeasurements(
                        bandwidthMeasurementsDirectory, ConfigurationData.INSTANCE.getRegionConfig().regions)
                );
            } else {
                chainManager.addStreamListener(new PerRegionBandwidthMeasurement(
                        bandwidthMeasurementsDirectory,
                        measurement.fromRegion,
                        measurement.toRegion,
                        measurement.transmissionMode,
                        measurement.applicationClass,
                        ConfigurationData.INSTANCE.getRegionConfig().regions));
            }
        }
    }

    /**
     * Finish the bandwidth measurements.
     */
    public void finish() {
        if (bandwidthMeasurementsDirectory != null && ConfigurationData.INSTANCE.getCellConfig().bandwidthMeasurementCompression) {
            collectBandwidthMeasurements();
        }
    }

    /**
     * Collects the bandwidth measurements while creating an output stream in zip format.
     */
    private void collectBandwidthMeasurements() {
        final Collection<File> files =
                FileUtils.listFiles(bandwidthMeasurementsDirectory, FileFilterUtils.trueFileFilter(), FileFilterUtils.falseFileFilter());

        try (ZipOutputStream zipOut =
                     new ZipOutputStream(new FileOutputStream(new File(bandwidthMeasurementsDirectory, "bandwidthMeasurements.zip")))
        ) {
            zipOut.setLevel(ZipOutputStream.STORED);
            for (File file : files) {
                zipOut.putNextEntry(new ZipEntry(file.getName()));
                try (FileInputStream in = new FileInputStream(file)) {
                    IOUtils.copy(in, zipOut);
                }
            }
        } catch (IOException e) {
            log.error("Could not collect bandwidth measurements", e);
        }
        files.forEach(FileUtils::deleteQuietly);
    }

    /**
     * Determines the parent directory for the bandwidth measurement file.
     *
     * @return {@link File}-Object pointing to parent-dir of cell log, or null.
     */
    private File determineParentDir() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
            Appender<ILoggingEvent> appender = logger.getAppender("CellLog");
            if (appender instanceof FileAppender) {
                FileAppender<?> fileAppender = ((FileAppender<?>) appender);
                return new File(fileAppender.getFile()).getParentFile();
            }
        }
        return null;
    }
}
