/*
 * Copyright (c) 2021 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.fed.sumo.bridge.libsumo;

import org.eclipse.mosaic.fed.sumo.bridge.Bridge;

import com.google.common.collect.Lists;
import org.eclipse.sumo.libsumo.SWIGTYPE_p_std__vectorT_std__vectorT_libsumo__TraCILink_t_t;
import org.eclipse.sumo.libsumo.TrafficLight;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TrafficLightGetControlledLinks implements org.eclipse.mosaic.fed.sumo.bridge.api.TrafficLightGetControlledLinks {


    public List<TrafficLightControlledLink> execute(Bridge bridge, String tlId) {
        SWIGTYPE_p_std__vectorT_std__vectorT_libsumo__TraCILink_t_t controlledLinks
                = TrafficLight.getControlledLinks(tlId);
        //TODO currently not implemented on libsumo side
        LoggerFactory.getLogger(this.getClass()).warn("Reading the controlled links of traffic lights is not implemented yet in libsumo.");
        return Lists.newArrayList();
    }

}
