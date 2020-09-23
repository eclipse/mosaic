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

package org.eclipse.mosaic.rti.api;

import org.eclipse.mosaic.rti.api.parameters.FederateDescriptor;

import java.util.Collection;


/**
 * The federation management is responsible for the administration of the
 * simulators and their ambassadors.
 */
public interface FederationManagement {

    /**
     * Creates an empty federation. After calling this method it is possible for federates to
     * join the federation.
     */
    void createFederation();

    /**
     * Adds a federate to the federation. This method can be called either
     * before or during runtime of the simulation of the federation.
     *
     * @param handle object containing all data that are necessary to manage the
     *               lifecycle of a federate
     */
    void addFederate(FederateDescriptor handle) throws Exception;

    /**
     * Checks whether a federate is joined or not.
     *
     * @param federateId unique string identifying the federate
     * @return a boolean, whether a federate is joined or not
     */
    boolean isFederateJoined(String federateId);

    /**
     * Returns a reference to the ambassador associated with the given federate
     * id.
     *
     * @param federateId unique string identifying the federate
     * @return ambassador associated with the given federate id
     */
    FederateAmbassador getAmbassador(String federateId);

    /**
     * Returns a collection of all ambassadors associated with joined federates.
     *
     * @return collection of all ambassadors
     */
    Collection<FederateAmbassador> getAmbassadors();

    /**
     * Stops the federation. All resources of federates are freed. Afterwards,
     * no new federates are allowed to join.
     */
    void stopFederation() throws Exception;

    /**
     * Returns the id of the simulated federation.
     *
     * @return the id of the simulated federation.
     */
    String getFederationId();


    /**
     * Sets the {@link WatchDog} which detects broken federates.
     */
    void setWatchdog(WatchDog watchDogThread);
}
