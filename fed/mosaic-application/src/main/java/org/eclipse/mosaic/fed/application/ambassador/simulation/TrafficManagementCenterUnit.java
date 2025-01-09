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

package org.eclipse.mosaic.fed.application.ambassador.simulation;

import org.eclipse.mosaic.fed.application.ambassador.ErrorRegister;
import org.eclipse.mosaic.fed.application.ambassador.simulation.tmc.InductionLoop;
import org.eclipse.mosaic.fed.application.ambassador.simulation.tmc.LaneAreaDetector;
import org.eclipse.mosaic.fed.application.app.api.TrafficManagementCenterApplication;
import org.eclipse.mosaic.fed.application.app.api.os.TrafficManagementCenterOperatingSystem;
import org.eclipse.mosaic.interactions.traffic.InductionLoopDetectorSubscription;
import org.eclipse.mosaic.interactions.traffic.LaneAreaDetectorSubscription;
import org.eclipse.mosaic.interactions.traffic.LanePropertyChange;
import org.eclipse.mosaic.interactions.traffic.TrafficDetectorUpdates;
import org.eclipse.mosaic.interactions.trafficsigns.TrafficSignLaneAssignmentChange;
import org.eclipse.mosaic.interactions.trafficsigns.TrafficSignSpeedLimitChange;
import org.eclipse.mosaic.lib.enums.VehicleClass;
import org.eclipse.mosaic.lib.objects.mapping.TmcMapping;
import org.eclipse.mosaic.lib.objects.traffic.InductionLoopInfo;
import org.eclipse.mosaic.lib.objects.traffic.LaneAreaDetectorInfo;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.api.Interaction;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a Traffic Management Center in the application simulator.
 */
public class TrafficManagementCenterUnit extends ServerUnit implements TrafficManagementCenterOperatingSystem {

    private final Map<String, InductionLoop> inductionLoopMap = new HashMap<>();

    private final Map<String, LaneAreaDetector> laneAreaDetectorMap = new HashMap<>();

    /**
     * Constructor for {@link TrafficManagementCenterUnit}, sets operating system and
     * traffic detectors.
     *
     * @param applicationTmc the configuration of the tmc including traffic detectors
     */
    public TrafficManagementCenterUnit(final TmcMapping applicationTmc) {
        super(applicationTmc.getName());
        setRequiredOperatingSystem(TrafficManagementCenterOperatingSystem.class);

        applicationTmc.getInductionLoops().forEach(inductionLoopId -> {
            inductionLoopMap.put(inductionLoopId, new InductionLoop(inductionLoopId));
            sendInteractionToRti(new InductionLoopDetectorSubscription(getSimulationTime(), inductionLoopId));
        });
        applicationTmc.getLaneAreaDetectors().forEach(laneAreaDetectorId -> {
            laneAreaDetectorMap.put(laneAreaDetectorId, new LaneAreaDetector(laneAreaDetectorId));
            sendInteractionToRti(new LaneAreaDetectorSubscription(getSimulationTime(), laneAreaDetectorId));
        });
    }

    /**
     * Returns the list of mapped induction loop ids.
     *
     * @return The list of mapped induction loop ids.
     */
    public Collection<String> getInductionLoopIds() {
        return inductionLoopMap.keySet();
    }

    /**
     * Returns the list of mapped lane area detector ids.
     *
     * @return The list of mapped lane area detector ids.
     */
    public Collection<String> getLaneAreaIds() {
        return laneAreaDetectorMap.keySet();
    }

    @Override
    public InductionLoop getInductionLoop(String id) {
        return inductionLoopMap.get(id);
    }

    @Override
    public LaneAreaDetector getLaneAreaDetector(String id) {
        return laneAreaDetectorMap.get(id);
    }

    @Override
    public Collection<InductionLoop> getInductionLoops() {
        return Collections.unmodifiableCollection(inductionLoopMap.values());
    }

    @Override
    public Collection<LaneAreaDetector> getLaneAreaDetectors() {
        return Collections.unmodifiableCollection(laneAreaDetectorMap.values());
    }

    @Override
    public ChangeLaneState changeLaneState(String edge, int laneIndex) {
        return new ChangeLaneStateImpl(edge, laneIndex, this);
    }

    @Override
    public ChangeLaneState changeVariableMessageSignState(String signId, int laneIndex) {
        return new ChangeVariableMessageSignStateImpl(signId, laneIndex, this);
    }

    @Override
    public void processEvent(Event event) throws Exception {
        // never remove the preProcessEvent call!
        final boolean preProcessed = super.preProcessEvent(event);

        // don't handle processed events
        if (preProcessed) {
            return;
        }

        final Object resource = event.getResource();

        // failsafe
        if (resource == null) {
            getOsLog().error("Event has no resource: {}", event);
            throw new RuntimeException(ErrorRegister.TRAFFIC_MANAGEMENT_CENTER_NoEventResource.toString());
        }

        if (!handleEventResource(resource)) {
            getOsLog().error("Unknown event resource: {}", event);
            throw new RuntimeException(ErrorRegister.TRAFFIC_MANAGEMENT_CENTER_UnknownEvent.toString());
        }
    }

    private boolean handleEventResource(Object resource) {
        if (resource instanceof TrafficDetectorUpdates detectorUpdates) {
            final List<InductionLoop> updatedInductionLoops = new ArrayList<>(detectorUpdates.getUpdatedInductionLoops().size());
            for (InductionLoopInfo inductionLoopInfo : detectorUpdates.getUpdatedInductionLoops()) {
                InductionLoop inductionLoop = getInductionLoop(inductionLoopInfo.getName());
                if (inductionLoop != null) {
                    inductionLoop.update(inductionLoopInfo);
                    updatedInductionLoops.add(inductionLoop);
                }
            }
            if (!updatedInductionLoops.isEmpty()) {
                for (TrafficManagementCenterApplication application : getApplicationsIterator(TrafficManagementCenterApplication.class)) {
                    application.onInductionLoopUpdated(updatedInductionLoops);
                }
            }

            final List<LaneAreaDetector> updatedLaneAreaDetectors = new ArrayList<>(detectorUpdates.getUpdatedLaneAreaDetectors().size());
            for (LaneAreaDetectorInfo laneAreaDetectorInfo : detectorUpdates.getUpdatedLaneAreaDetectors()) {
                LaneAreaDetector laneAreaDetector = getLaneAreaDetector(laneAreaDetectorInfo.getName());
                if (laneAreaDetector != null) {
                    laneAreaDetector.update(laneAreaDetectorInfo);
                    updatedLaneAreaDetectors.add(laneAreaDetector);
                }
            }
            if (!updatedLaneAreaDetectors.isEmpty()) {
                for (TrafficManagementCenterApplication application : getApplicationsIterator(TrafficManagementCenterApplication.class)) {
                    application.onLaneAreaDetectorUpdated(updatedLaneAreaDetectors);
                }
            }

            return true;
        }
        return false;
    }

    private static abstract class ChangeLaneStateBaseImpl {

        final AbstractSimulationUnit unit;

        private ChangeLaneStateBaseImpl(AbstractSimulationUnit unit) {
            this.unit = unit;
        }

        void sendInteractionQuietly(Interaction interaction) {
            try {
                unit.sendInteractionToRti(interaction);
            } catch (RuntimeException e) {
                unit.getOsLog().error("Could not send interaction", e);
            }
        }
    }

    private static class ChangeLaneStateImpl extends ChangeLaneStateBaseImpl implements ChangeLaneState {

        private final String edgeId;
        private final int laneIndex;

        private ChangeLaneStateImpl(String edgeId, int laneIndex, AbstractSimulationUnit unit) {
            super(unit);
            this.edgeId = edgeId;
            this.laneIndex = laneIndex;
        }

        @Override
        public ChangeLaneState openOnlyForVehicleClasses(VehicleClass... allowVehicleClasses) {
            sendInteractionQuietly(new LanePropertyChange(
                    unit.getSimulationTime(),
                    edgeId,
                    laneIndex,
                    Lists.newArrayList(allowVehicleClasses),
                    null,
                    null)
            );
            return this;
        }

        @Override
        public ChangeLaneState closeOnlyForVehicleClasses(VehicleClass... disallowVehicleClasses) {
            sendInteractionQuietly(new LanePropertyChange(
                    unit.getSimulationTime(),
                    edgeId,
                    laneIndex,
                    null,
                    Lists.newArrayList(disallowVehicleClasses),
                    null)
            );
            return this;
        }

        @Override
        public ChangeLaneState setMaxSpeed(double maxSpeedMs) {
            sendInteractionQuietly(new LanePropertyChange(unit.getSimulationTime(), edgeId, laneIndex, null, null, maxSpeedMs));
            return this;
        }
    }

    private static class ChangeVariableMessageSignStateImpl extends ChangeLaneStateBaseImpl implements ChangeLaneState {

        private final String signId;
        private final int laneIndex;

        private ChangeVariableMessageSignStateImpl(String signId, int laneIndex, AbstractSimulationUnit unit) {
            super(unit);
            this.signId = signId;
            this.laneIndex = laneIndex;
        }

        @Override
        public ChangeLaneState openOnlyForVehicleClasses(VehicleClass... allowVehicleClasses) {
            if (laneIndex < 0) {
                throw new IllegalArgumentException("With negative lane index you can only use speed assignments, then addressing all lanes.");
            }
            sendInteractionQuietly(new TrafficSignLaneAssignmentChange(
                            unit.getSimulationTime(),
                            signId,
                            laneIndex,
                            Lists.newArrayList(allowVehicleClasses)
                    )
            );
            return this;
        }

        @Override
        public ChangeLaneState closeOnlyForVehicleClasses(VehicleClass... disallowVehicleClasses) {
            ArrayList<VehicleClass> remaining = Lists.newArrayList(VehicleClass.values());
            for (VehicleClass removeme : disallowVehicleClasses) {
                remaining.remove(removeme);
            }
            return openOnlyForVehicleClasses(remaining.toArray(new VehicleClass[remaining.size()]));
        }

        @Override
        public ChangeLaneState setMaxSpeed(double maxSpeedMs) {
            if (laneIndex < 0) {
                sendInteractionQuietly(
                        new TrafficSignSpeedLimitChange(
                                unit.getSimulationTime(),
                                signId,
                                maxSpeedMs
                        )
                );
            } else {
                sendInteractionQuietly(
                        new TrafficSignSpeedLimitChange(
                                unit.getSimulationTime(),
                                signId,
                                laneIndex,
                                maxSpeedMs
                        )
                );
            }
            return this;
        }

    }
}