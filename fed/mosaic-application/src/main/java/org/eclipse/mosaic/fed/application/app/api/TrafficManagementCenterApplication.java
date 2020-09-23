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

package org.eclipse.mosaic.fed.application.app.api;

import org.eclipse.mosaic.fed.application.ambassador.simulation.tmc.InductionLoop;
import org.eclipse.mosaic.fed.application.ambassador.simulation.tmc.LaneAreaDetector;
import org.eclipse.mosaic.fed.application.app.api.os.TrafficManagementCenterOperatingSystem;

import java.util.Collection;

/**
 * All applications accessing traffic management center functionality
 * are to implement this interface.
 */
public interface TrafficManagementCenterApplication extends Application, OperatingSystemAccess<TrafficManagementCenterOperatingSystem> {

    /**
     * This method is called when any subscribed {@link InductionLoop} of the unit
     * has been updated. This requires this tmc unit to be mapped correctly to
     * induction loop detectors in the mapping3 Configuration.
     *
     * @param updatedInductionLoops collection of updated {@link InductionLoop} states
     */
    void onInductionLoopUpdated(Collection<InductionLoop> updatedInductionLoops);

    /**
     * This method is called when any subscribed {@link LaneAreaDetector} of the unit
     * has been updated. This requires this tmc unit to be mapped correctly to
     * lane area detectors in the mapping3 Configuration.
     *
     * @param updatedLaneAreaDetectors collection of updated {@link LaneAreaDetector} states
     */
    void onLaneAreaDetectorUpdated(Collection<LaneAreaDetector> updatedLaneAreaDetectors);

}
