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

package org. eclipse.mosaic.fed.output.generator.zeromq;

import org.eclipse.mosaic.fed.output.ambassador.AbstractOutputGenerator;
import org.eclipse.mosaic.fed.output.ambassador.OutputGeneratorLoader;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import java.io.File;

public class SocketZeromqLoader extends OutputGeneratorLoader {

    private int port;

    @Override
    public void initialize(HierarchicalConfiguration<ImmutableNode> config, File configurationDirectory) throws Exception {
        super.initialize(config, configurationDirectory);
        port = config.getInt("port");
    }

    @Override
    public AbstractOutputGenerator createOutputGenerator() {
        return new SocketZeromq(port);
    }

}
