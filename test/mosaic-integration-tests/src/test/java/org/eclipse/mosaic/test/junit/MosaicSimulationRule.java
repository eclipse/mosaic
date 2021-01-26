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

package org.eclipse.mosaic.test.junit;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.sumo.ambassador.SumoGuiAmbassador;
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

import com.google.common.base.Charsets;
import org.apache.commons.io.FileUtils;
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
import java.util.HashMap;
import java.util.Map;

public class MosaicSimulationRule extends TemporaryFolder {

    protected CHosts hostsConfiguration;
    protected CRuntime runtimeConfiguration;
    protected MosaicSimulation.ComponentProviderFactory componentProviderFactory = MosaicComponentProvider::new;
    protected Path logDirectory;

    protected String logLevelOverride = null;
    protected Map<String, String> federateOverride = new HashMap<>();

    @Override
    protected void before() throws Throwable {
        super.before();

        hostsConfiguration = prepareHostsConfiguration();
        runtimeConfiguration = prepareRuntimeConfiguration();
    }

    public MosaicSimulationRule logLevelOverride(String logLevelOverride) {
        this.logLevelOverride = logLevelOverride;
        return this;
    }

    public MosaicSimulationRule federateOverride(String federateName, Class federateAmbassador) {
        this.federateOverride.put(federateName, federateAmbassador.getCanonicalName());
        return this;
    }

    private void setFederateOverride(Map<String, String> federateOverride) {
        for (Map.Entry<String, String> federateEntry : federateOverride.entrySet()) {
            for (CRuntime.CFederate federate : runtimeConfiguration.federates) {
                if (federate.id.equals(federateEntry.getKey())) {
                    federate.classname = federateEntry.getValue();
                }
            }
        }
    }

    public MosaicSimulationRule componentProviderFactory(MosaicSimulation.ComponentProviderFactory factory) {
        this.componentProviderFactory = factory;
        return this;
    }

    protected CHosts prepareHostsConfiguration() throws IOException {
        Path tmpDirectory = newFolder("tmp").toPath();
        CHosts hostsConfiguration = new CHosts();
        hostsConfiguration.localHosts.add(new CLocalHost(tmpDirectory.toAbsolutePath().toString()));
        return hostsConfiguration;
    }

    protected CRuntime prepareRuntimeConfiguration() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/runtime.json")) {
            return new ObjectInstantiation<>(CRuntime.class).read(resource);
        } catch (InstantiationException e) {
            throw new IOException(e);
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
            logDirectory = Paths.get("./log").resolve(scenarioConfiguration.simulation.id);
            final Path logConfiguration = prepareLogConfiguration(logDirectory);

            setFederateOverride(federateOverride);
            return new MosaicSimulation()
                    .setRuntimeConfiguration(runtimeConfiguration)
                    .setHostsConfiguration(hostsConfiguration)
                    .setLogbackConfigurationFile(logConfiguration)
                    .setLogLevelOverride(logLevelOverride)
                    .setComponentProviderFactory(componentProviderFactory)
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

    protected Path prepareLogConfiguration(Path logDirectory) throws IOException {
        FileUtils.deleteQuietly(logDirectory.toFile());

        Path logConfiguration = newFile("logback.xml").toPath();

        try (BufferedReader resource = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/logback.xml"), Charsets.UTF_8));
             Writer writer = new OutputStreamWriter(new FileOutputStream(logConfiguration.toFile()), StandardCharsets.UTF_8)) {
            String line;
            while ((line = resource.readLine()) != null) {
                writer.write(StringUtils.replace(line, "${logDirectory}", logDirectory.toAbsolutePath().toString()));
            }
        }
        return logConfiguration;
    }


    protected void resetSingletons() {
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

    /**
     * Debug feature: activate SUMO GUI to visualize vehicle movements.
     * DO NOT commit test with having this activated.
     */
    public void activateSumoGui() {
        getRuntimeConfiguration().federates.stream().filter(s -> s.id.equals("sumo")).forEach(
                s -> s.classname = SumoGuiAmbassador.class.getCanonicalName()
        );
    }
}
