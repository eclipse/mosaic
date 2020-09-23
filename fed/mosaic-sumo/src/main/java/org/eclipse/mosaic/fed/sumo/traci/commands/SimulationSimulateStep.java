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

package org.eclipse.mosaic.fed.sumo.traci.commands;

import org.eclipse.mosaic.fed.sumo.traci.AbstractTraciCommand;
import org.eclipse.mosaic.fed.sumo.traci.TraciCommandException;
import org.eclipse.mosaic.fed.sumo.traci.TraciConnection;
import org.eclipse.mosaic.fed.sumo.traci.TraciVersion;
import org.eclipse.mosaic.fed.sumo.traci.complex.AbstractSubscriptionResult;
import org.eclipse.mosaic.fed.sumo.traci.complex.Status;
import org.eclipse.mosaic.fed.sumo.traci.constants.CommandSimulationControl;
import org.eclipse.mosaic.fed.sumo.traci.reader.AllSubscriptionsTraciReader;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.InternalFederateException;

import java.util.List;

/**
 * This class represents the traci command which allows to set the simulation step.
 */
public class SimulationSimulateStep extends AbstractTraciCommand<AbstractSubscriptionResult> {

    /**
     * Creates a {@link SimulationSimulateStep} traci command, which
     * can late be executed with a given time value.
     * Access needs to be public, because command is called using Reflection.
     */
    @SuppressWarnings("WeakerAccess")
    public SimulationSimulateStep() {
        super(TraciVersion.LOWEST);

        write()
                .command(CommandSimulationControl.COMMAND_SIMULATION_STEP)
                .writeDoubleParam();

        read()
                .readComplex(new AllSubscriptionsTraciReader());
    }

    /**
     * This method executes the command with the given arguments in order to set the simulation step.
     *
     * @param traciCon Connection to Traci.
     * @param time     Time step.
     * @return List of the results.
     * @throws TraciCommandException     if the status code of the response is ERROR. The TraCI connection is still available.
     * @throws InternalFederateException if some serious error occurs during writing or reading. The TraCI connection is shut down.
     */
    public List<AbstractSubscriptionResult> execute(TraciConnection traciCon, long time) throws TraciCommandException, InternalFederateException {
        return super.executeAndReturnList(traciCon, (double) (time) / TIME.SECOND);
    }

    @Override
    protected AbstractSubscriptionResult constructResult(Status status, Object... objects) {
        return (AbstractSubscriptionResult) objects[0];
    }
}
