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

package org.eclipse.mosaic.fed.output.ambassador;

import static org.mockito.Mockito.spy;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * An extension of {@link OutputGeneratorLoader} for testing purposes.
 */
public class TestOutputGeneratorLoader extends OutputGeneratorLoader {

    @Override
    protected void configure(HierarchicalConfiguration<ImmutableNode> config) {
        // nothing to do
    }

    @Override
    public AbstractOutputGenerator createOutputGenerator() {
        return spy(new AbstractOutputGenerator() {});
    }

}
