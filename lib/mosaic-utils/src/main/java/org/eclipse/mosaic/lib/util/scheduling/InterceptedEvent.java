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

import java.util.List;
import javax.annotation.Nonnull;

/**
 * An intercepted event is a typesafe event for interception.
 */
public class InterceptedEvent extends Event {

    /**
     * This constructor creates a new intercepted event based on a given event
     * with other processors.
     *
     * @param event      the original event.
     * @param processors the new processors.
     */
    InterceptedEvent(@Nonnull final Event event, @Nonnull final List<EventProcessor> processors) {
        super(event.getTime(), processors, event, event.getNice());
    }

    /**
     * Returns the original event wrapped by this {@link InterceptedEvent}.
     *
     * @return the original event
     */
    public Event getOriginalEvent() {
        return (Event) getResource();
    }

}
