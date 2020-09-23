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

package org.eclipse.mosaic.test.junit;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.lib.objects.addressing.IpResolver;
import org.eclipse.mosaic.lib.objects.v2x.etsi.EtsiPayloadConfiguration;
import org.eclipse.mosaic.lib.transform.GeoProjection;
import org.eclipse.mosaic.lib.util.junit.TestUtils;
import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;
import org.eclipse.mosaic.rti.MosaicComponentProvider;
import org.eclipse.mosaic.rti.config.CHosts;
import org.eclipse.mosaic.rti.config.CLocalHost;
import org.eclipse.mosaic.starter.MosaicSimulation;
import org.eclipse.mosaic.starter.config.CRuntime;
import org.eclipse.mosaic.starter.config.CScenario;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MosaicSimulationRule extends TemporaryFolder {

    private CHosts hostsConfiguration;
    private CRuntime runtimeConfiguration;
    private Path logDirectory;
    private Path logConfiguration;


    @Override
    protected void before() throws Throwable {
        super.before();

        logDirectory = newFolder("log").toPath();
        logConfiguration = prepareLogConfiguration(logDirectory);
        hostsConfiguration = prepareHostsConfiguration();
        runtimeConfiguration = prepareRuntimeConfiguration();
    }

    private Path prepareLogConfiguration(Path logDirectory) throws IOException {
        Path logConfiguration = logDirectory.resolve("logback.xml");

        try (BufferedReader resource = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/logback.xml"), "UTF-8"));
             Writer writer = new OutputStreamWriter(new FileOutputStream(logConfiguration.toFile()), StandardCharsets.UTF_8)) {
            String line;
            while ((line = resource.readLine()) != null) {
                writer.write(StringUtils.replace(line, "${logDirectory}", logDirectory.toAbsolutePath().toString()));
            }
        }
        return logConfiguration;
    }

    private CHosts prepareHostsConfiguration() throws IOException {
        Path tmpDirectory = newFolder("tmp").toPath();
        CHosts hostsConfiguration = new CHosts();
        hostsConfiguration.localHosts.add(new CLocalHost(tmpDirectory.toAbsolutePath().toString()));
        return hostsConfiguration;
    }

    private CRuntime prepareRuntimeConfiguration() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/runtime.xml")) {
            return new XmlMapper()
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(resource, CRuntime.class);
        }
    }

    public Path getLogDirectory() {
        return logDirectory;
    }

    public CRuntime getRuntimeConfiguration() {
        return runtimeConfiguration;
    }

    public MosaicSimulation.SimulationResult executeTestScenario(String name) {
        return executeSimulation("..", "scenarios", name);
    }

    public MosaicSimulation.SimulationResult executeReleaseScenario(String name) {
        return executeSimulation("..", "..", "bundle", "src", "assembly", "resources", "scenarios", name);
    }

    public MosaicSimulation.SimulationResult executeSimulation(String first, String... other) {
        return executeSimulation(Paths.get(first, other));
    }

    public MosaicSimulation.SimulationResult executeSimulation(Path scenarioDirectory) {
        try {
            return executeSimulation(scenarioDirectory,
                    new ObjectInstantiation<>(CScenario.class)
                            .readFile(scenarioDirectory.resolve("scenario_config.json").toFile())
            );
        } catch (InstantiationException e) {
            MosaicSimulation.SimulationResult result = new MosaicSimulation.SimulationResult();
            result.exception = e;
            result.success = false;
            return result;
        }
    }

    public MosaicSimulation.SimulationResult executeSimulation(Path scenarioDirectory, CScenario scenarioConfiguration) {
        try {
            return new MosaicSimulation()
                    .setRuntimeConfiguration(runtimeConfiguration)
                    .setHostsConfiguration(hostsConfiguration)
                    .setLogbackConfigurationFile(logConfiguration)
                    .setLogLevelOverride("DEBUG")
                    .setComponentProviderFactory(MosaicComponentProvider::new)
                    .runSimulation(scenarioDirectory, scenarioConfiguration);
        } catch (Throwable e) {
            MosaicSimulation.SimulationResult result = new MosaicSimulation.SimulationResult();
            result.exception = e;
            result.success = false;
            return result;
        } finally {
            resetSingletons();
        }
    }

    private void resetSingletons() {
        TestUtils.setPrivateField(GeoProjection.class, "instance", null);
        TestUtils.setPrivateField(IpResolver.class, "singleton", null);
        TestUtils.setPrivateField(EtsiPayloadConfiguration.class, "globalConfiguration", null);

        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "eventManager", null);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "interactable", null);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "navigation", null);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "classLoader", null);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "randomNumberGenerator", null);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "configuration", null);
        TestUtils.setPrivateField(SimulationKernel.SimulationKernel, "configurationPath", null);
    }


}
