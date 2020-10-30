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

package org.eclipse.mosaic.lib.coupling;

import org.eclipse.mosaic.lib.util.objects.ObjectInstantiation;

import org.junit.Test;

public class CAbstractNetworkAmbassadorTest {

    @Test
    public void tiergartenOmnetpp() throws InstantiationException {
        new ObjectInstantiation<>(CAbstractNetworkAmbassador.class)
                .read(getClass().getResourceAsStream("/Tiergarten/omnetpp_config.json"));
    }

    @Test
    public void tiergartenNs3() throws InstantiationException {
        new ObjectInstantiation<>(CAbstractNetworkAmbassador.class)
                .read(getClass().getResourceAsStream("/Tiergarten/ns3_config.json"));
    }
}
