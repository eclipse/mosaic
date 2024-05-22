/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.objects.v2x;

/**
 * A message that can duplicate itself.
 * @param <T> a class that extends {@link org.eclipse.mosaic.lib.objects.v2x.V2xMessage}
 */
public interface DuplicatableMessage<T extends V2xMessage> {

    /**
     * Creates a copy of the V2xMessage.
     * The message gets a new ID and routing (to use current vehicle position).
     * Only its contents (e.g. payload, type, ...) are copied.
     *
     * @param routing contains current vehicle position
     * @return cloned message
     */
    T duplicate(MessageRouting routing);
}
