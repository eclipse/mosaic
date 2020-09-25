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

/**
 * The event processor processes a scheduled event.
 */
public interface EventProcessor {

    /**
     * Processes the given event.
     *
     * @param event the event to process
     */
    void processEvent(Event event) throws Exception;
    
    /**
     * Checks, if this {@link EventProcessor} is able to process any events. If the processor
     * is not currently to able to process a given event, those events will be skipped and
     * never reach this {@link EventProcessor}.
     * 
     * @return <code>true</code> if this {@link EventProcessor} is currently able to process events, otherwise <code>false</code>.
     */
    default boolean canProcessEvent() {
        return true;
    }
}
