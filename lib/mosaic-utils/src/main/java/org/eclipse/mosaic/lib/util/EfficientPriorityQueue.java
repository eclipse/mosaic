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

package org.eclipse.mosaic.lib.util;

import java.util.HashSet;
import java.util.PriorityQueue;

/**
 * Extends {@link PriorityQueue} with an efficient contains method
 * that avoids to loop through the queue to find an element.
 * Instead it checks if the elements hash code is stored in a separate {@link HashSet}.
 */
public class EfficientPriorityQueue<T> extends PriorityQueue<T> {

    private static final long serialVersionUID = 1L;

    private final HashSet<T> hashes = new HashSet<>();

    @Override
    public boolean contains(Object o) {
        return hashes.contains(o);
    }

    @Override
    public boolean offer(T object) {
        if (super.offer(object)) {
            hashes.add(object);
            return true;
        }
        return false;
    }

    @Override
    public T poll() {
        T o = super.poll();
        if (o != null) {
            hashes.remove(o);
        }
        return o;
    }

    @Override
    public boolean remove(Object obj) {
        if (super.remove(obj)) {
            hashes.remove(obj);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        super.clear();
        hashes.clear();
    }
}
