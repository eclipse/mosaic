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

package org.eclipse.mosaic.lib.util.objects;

import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * This cache is a time based cache.
 * <p>
 * Example usage: Network simulators could't provide the information when a V2XMessage is
 * expired. V2XMessages must be remove after a certain time to save memory.
 * </p>
 */
@NotThreadSafe
public class TimeCache<T extends Identifiable> implements Serializable {

    /**
     * Map containing all the message ids with the corresponding messages.
     */
    @Nonnull
    private final Map<Integer, Pair<Long, T>> cache = new ConcurrentHashMap<>();
    
    /**
     * Returns a v2x message based on their id.
     * @param id the id of the cached message.
     * @return the cached message.
     */
    @Nullable
    public final T getItem(int id) {
        Pair<Long, T> pair = cache.get(id);
        if (pair != null) {
            return pair.getRight();
        }
        return null;
    }

    /**
     * Put a {@link T} object in the cache.
     * @param time the time the object is associated with
     * @param object the object to cache.
     */
    public final void putItem(final long time, final T object) {
        cache.put(object.getId(), Pair.of(time, object));
    }
    
    /**
     * Clean the cache until the given time.
     * @param time the time until the cache is valid.
     * @return a set of the removed messages
     */
    public final Set<Integer> garbageCollection(final long time) {
        final Set<Integer> removedIds = new HashSet<>();
        if (time > 0) {
            for (Iterator<Map.Entry<Integer, Pair<Long, T>>> it = cache.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Integer, Pair<Long, T>> entry = it.next();
                if (entry.getValue().getLeft() < time) {
                    it.remove();
                    removedIds.add(entry.getKey());
                }
            }
        }
        return removedIds;
    }
}
