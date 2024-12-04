/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.routing.pt;

import java.util.ArrayList;
import java.util.List;

public class MultiModalRoute {

    private final List<MultiModalLeg> legs = new ArrayList<>();

    public MultiModalRoute(List<MultiModalLeg> legs) {
        this.legs.addAll(legs);
    }

    public List<MultiModalLeg> getLegs() {
        return this.legs;
    }

}
