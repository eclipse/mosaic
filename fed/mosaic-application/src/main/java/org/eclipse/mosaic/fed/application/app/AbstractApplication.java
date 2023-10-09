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

package org.eclipse.mosaic.fed.application.app;

import org.eclipse.mosaic.fed.application.ambassador.SimulationKernel;
import org.eclipse.mosaic.fed.application.ambassador.util.UnitLogger;
import org.eclipse.mosaic.fed.application.app.api.Application;
import org.eclipse.mosaic.fed.application.app.api.OperatingSystemAccess;
import org.eclipse.mosaic.fed.application.app.api.os.OperatingSystem;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.util.scheduling.DefaultEventScheduler;
import org.eclipse.mosaic.lib.util.scheduling.EventProcessor;

import java.util.Objects;

/**
 * The {@link AbstractApplication}. Each simulated application must extend this class.
 *
 * @param <OS> type of the operating system for the application.
 */
@SuppressWarnings("checkstyle:ClassTypeParameterName")
public abstract class AbstractApplication<OS extends OperatingSystem> implements EventProcessor, Application, OperatingSystemAccess<OS> {

    private OS operatingSystem = null;
    private UnitLogger log = null;

    private boolean isSetUp;
    private boolean isTornDown;

    /**
     * Returns the operating system for this application.
     * Additionally, the {@link #operatingSystem} is checked against {@code null}.
     *
     * @return the operating system.
     */
    @Override
    public final OS getOperatingSystem() {
        Objects.requireNonNull(operatingSystem, "The application has not been initialized by the ambassador: the OperatingSystem is null.");
        return operatingSystem;
    }

    /**
     * Returns the log facade for this application.
     * Additionally, the {@link #log} is checked against {@code null}.
     *
     * @return the log facade for this application.
     */
    public final UnitLogger getLog() {
        Objects.requireNonNull(log, "The application has not been initialized by the ambassador: the UnitLogger is null.");
        return log;
    }

    /**
     * See {@link RandomNumberGenerator} for more information.
     *
     * @return The {@link RandomNumberGenerator} for generating random numbers.
     */
    public final RandomNumberGenerator getRandom() {
        return SimulationKernel.SimulationKernel.getRandomNumberGenerator();
    }

    /**
     * Do not invoke this method in an application. Only the simulation unit should invoke this
     * method.
     *
     * @param operatingSystem the operating system reference.
     * @param log             the logger.
     */
    @SuppressWarnings("unchecked")
    public final void setUp(final OperatingSystem operatingSystem, final UnitLogger log) {
        // failsafe
        if (isSetUp) {
            throw new IllegalStateException("Application was already set up.");
        }
        this.log = log;

        try {
            this.operatingSystem = (OS) Objects.requireNonNull(operatingSystem);
        } catch (ClassCastException e) {
            throw new IllegalStateException(
                    String.format(
                            "The Operating System %s is not compatible with the Application %s",
                            operatingSystem.getClass(),
                            getClass()
                    )
            );
        }
        onStartup();

        isSetUp = true;
    }

    /**
     * Do not invoke this method in an application. Only the unit simulator should invoke this
     * method.
     */
    public final void tearDown() {
        // failsafe
        if (isTornDown) {
            throw new IllegalStateException("Application was already torn down.");
        }
        onShutdown();

        isTornDown = true;
    }

    /**
     * Indicates an application was successfully tear down.
     *
     * @return {@code true} if success, otherwise {@code false}.
     */
    protected final boolean isTornDown() {
        return isTornDown;
    }

    /**
     * This method logs and returns the state of the application.
     *
     * @return {@code true} if the application was set up and is not torn down. Otherwise, it returns {@code false}.
     */
    public final boolean isValidStateAndLog() {
        if (!isSetUp) {
            log.warn("invalid application state: application was not set up");
            return false;
        }

        if (isTornDown) {
            log.warn("invalid application state: application was already torn down");
            return false;
        }
        return true;
    }

    /**
     * Checks whether the application is set up and not torn down.
     *
     * <p>
     * This method is used by the event scheduler ({@link DefaultEventScheduler})
     * to determine the correct state of an application, before events are passed to the application
     * (you do not want to process events in an application, which is not properly set up or has
     * already been torn down).</p>
     */
    @Override
    public boolean canProcessEvent() {
        return isSetUp && !isTornDown;
    }
}
