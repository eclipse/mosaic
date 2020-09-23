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

package org.eclipse.mosaic.lib.objects.mapping;

import java.util.ArrayList;
import java.util.List;

/**
 * A Traffic Management Center (TMC) simulation unit that is equipped with applications.
 */
public final class TmcMapping extends UnitMapping {

    private static final long serialVersionUID = 1L;

    private final List<String> inductionLoops = new ArrayList<>();
    private final List<String> laneAreaDetectors = new ArrayList<>();

    /**
     * Creates a new TMC simulation unit equipped with applications.
     *
     * @param name              The name of the TMC.
     * @param group             The group name of the TMC.
     * @param applications      The list of applications the TMC is equipped with.
     * @param inductionLoops    The list of induction loops the TMC is responsible for.
     * @param laneAreaDetectors The list of lane area detectors the TMC is responsible for.
     */
    public TmcMapping(
            final String name,
            final String group,
            final List<String> applications,
            final List<String> inductionLoops,
            final List<String> laneAreaDetectors
    ) {
        super(name, group, applications);
        this.inductionLoops.addAll(inductionLoops);
        this.laneAreaDetectors.addAll(laneAreaDetectors);
    }

    public List<String> getInductionLoops() {
        return inductionLoops;
    }

    public List<String> getLaneAreaDetectors() {
        return laneAreaDetectors;
    }
}