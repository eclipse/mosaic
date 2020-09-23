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

package org.eclipse.mosaic.starter.config;

import org.eclipse.mosaic.rti.api.parameters.FederatePriority;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "configuration")
public class CRuntime {

    /**
     * The number of threads to be used by the time management implementation.
     * <p>
     * Be careful with this. In most of the cases increasing the number of threads would not improve simulation time,
     * as work is only distributed amongst workers for events with equal time stamps.
     * Furthermore, determinism e.g. by setting a fixed random seed cannot be guaranteed anymore if using
     * more than one thread.
     * </p>
     */
    public int threads = 1;


    @JacksonXmlElementWrapper(localName = "federates")
    @JacksonXmlProperty(localName = "federate")
    public List<CFederate> federates = new ArrayList<>();

    public static class CFederate {

        public String id;

        @JacksonXmlProperty(localName = "class", isAttribute = true)
        public String classname;

        @JacksonXmlProperty(localName = "config")
        public String configuration;

        @JacksonXmlProperty(localName = "configDeployPath")
        public String configurationDeployPath;

        @JacksonXmlProperty(localName = "priority", isAttribute = true)
        public byte priority = FederatePriority.DEFAULT;

        public String dockerImage;

        public String host = "local";

        public int port = 0;
        public boolean deploy = false;
        public boolean start = false;

        @JacksonXmlElementWrapper(localName = "subscriptions")
        @JacksonXmlProperty(localName = "subscription")
        public List<String> subscriptions = new ArrayList<>();

        @JacksonXmlProperty(localName = "customJavaArgument")
        public String javaCustomArgument;
        public Integer javaMemorySizeXmx;

        @JacksonXmlElementWrapper(localName = "javaClasspathEntries")
        @JacksonXmlProperty(localName = "classpath")
        public List<String> javaClasspathEntries = new ArrayList<>();
    }

}
