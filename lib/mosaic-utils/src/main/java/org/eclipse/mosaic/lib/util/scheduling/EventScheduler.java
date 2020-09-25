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

package org.eclipse.mosaic.lib.util.scheduling;

import java.util.Set;
import javax.annotation.Nonnull;

/**
 * The event scheduler manages the events and processes them.
 */
public interface EventScheduler extends EventManager {

    /**
     * Returns <code>true</code> if this scheduler contains no elements.
     * Otherwise <code>false</code>.
     *
     * @return  <code>true</code> if this scheduler contains no elements. Otherwise <code>false</code>.
     */
    boolean isEmpty();

    /**
     * Returns the next event time. Make sure the scheduler is not empty.
     *
     * @return the next event time. Unit: [ns].
     */
    long getNextEventTime();

    /**
     * Returns the scheduled time.
     *
     * @return the scheduled time. Unit: [ns].
     */
    long getScheduledTime();

    @Nonnull
    int scheduleEvents(long time);

    /**
     * Returns an unmodifiable view of all remaining events.
     *
     * @return all remaining events.
     */
    @Nonnull
    Set<Event> getAllEvents();
}
