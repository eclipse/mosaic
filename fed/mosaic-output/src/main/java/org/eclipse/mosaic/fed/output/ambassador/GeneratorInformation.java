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

import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

class GeneratorInformation {

    private final String id;
    private final AbstractOutputGenerator generator;
    private final PriorityBlockingQueue<Interaction> interactionQueue;
    private final Collection<String> interactionTypes;
    private final Set<String> registrationSubscriptionTypes;
    private final int updateUnits;
    private int updateUnitCount;
    private final long handleStartTime;
    private final long handleEndTime;


    GeneratorInformation(OutputGeneratorLoader config, AbstractOutputGenerator generator, int globalUpdateIntervalInSeconds) {
        this.id = config.getId();
        this.interactionQueue = new PriorityBlockingQueue<>();
        this.generator = generator;
        this.interactionTypes = Validate.notNull(config.getInteractionTypes());
        this.registrationSubscriptionTypes = RegistrationSubscriptionTypes.get();
        this.updateUnits = config.getUpdateIntervalInSeconds() / globalUpdateIntervalInSeconds;

        this.handleStartTime = config.getHandleStartTime();
        this.handleEndTime = config.getHandleEndTime();
        reloadUpdateUnitCount();
    }

    String getId() {
        return id;
    }

    public long getHandleStartTime() {
        return handleStartTime;
    }

    public long getHandleEndTime() {
        return handleEndTime;
    }

    AbstractOutputGenerator getGenerator() {
        return generator;
    }

    int decrementUpdateUnitCount() {
        if (updateUnitCount > 0) {
            return --updateUnitCount;
        }
        return updateUnitCount;
    }

    public int getUpdateUnitCount() {
        return updateUnitCount;
    }

    void reloadUpdateUnitCount() {
        this.updateUnitCount = this.updateUnits;
    }

    /**
     * This method allows all interactions that are either relevant and in the given handle interval
     * or interactions that add a component, while not considering the time interval.
     *
     * @param type            the type of the interaction
     * @param interactionTime the time of the interaction
     * @return boolean that decides whether a interaction is relevant for handling or not
     */
    boolean isInteractionRelevant(String type, long interactionTime) {
        return (isInteractionInInterval(interactionTime) || isRelevantUnitRegistration(type)) && isInteractionTypeRelevant(type);
    }

    private boolean isRelevantUnitRegistration(String type) {
        return registrationSubscriptionTypes.contains(type);
    }

    private boolean isInteractionInInterval(long interactionTime) {
        return (interactionTime >= handleStartTime && interactionTime <= handleEndTime);
    }

    boolean isInteractionTypeRelevant(String type) {
        return interactionTypes.contains(type);
    }

    boolean needsFlush() {
        return interactionQueue.size() >= OutputAmbassador.FLUSH_THRESHOLD;
    }

    void addInteraction(Interaction interaction) {
        interactionQueue.add(interaction);
    }

    void processNextInteraction() {
        generator.handleInteraction(interactionQueue.poll());
    }

    boolean hasNextInteraction() {
        return !interactionQueue.isEmpty();
    }
}