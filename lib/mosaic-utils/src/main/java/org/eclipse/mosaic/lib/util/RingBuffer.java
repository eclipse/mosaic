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

import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import javax.annotation.Nonnull;

/**
 * Ring buffer implementation. Provides methods to add elements continuously to
 * this collection. If its capacity is exceeded, the oldest elements are replaced.
 * Adding a value and assessing a specific element via its index is accomplished in O(1).
 * Contents cannot be deleted actively.
 * Provides furthermore {@link #iterator()} and {@link #spliterator()} for
 * {@link java.util.stream.Stream} support.
 *
 * @param <T> The class of the buffers elements.
 */
public class RingBuffer<T> implements Iterable<T> {

    private final T[] elements;

    private int head = 0;
    private boolean ringMode = false;

    /**
     * Creates a new RingBuffer.
     *
     * @param capacity the capacity of this {@link RingBuffer}
     */
    @SuppressWarnings("unchecked")
    public RingBuffer(int capacity) {
        Validate.isTrue(capacity > 0, "Capacity of RingBuffer must be greater than 0.");
        elements = (T[]) new Object[capacity];
    }

    /**
     * Returns the size of ring buffer.
     *
     * @return size of the ring buffer
     */
    public int size() {
        if (ringMode) {
            return elements.length;
        }
        return head;
    }

    /**
     * Adds the {@param element} to the ring buffer.
     *
     * @param element the element to be added
     */
    public void add(T element) {
        elements[head] = element;

        head = (head + 1) % elements.length;

        if (head == 0) {
            ringMode = true;
        }
    }

    public T get(int index) {
        Validate.isTrue(index < size(), "RingBuffer out of bounds");
        return elements[ringMode ? (head + index) % elements.length : index];
    }

    public Spliterator<T> spliterator() {
        return Arrays.spliterator(elements, 0, size());
    }

    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int index = 0;

            public boolean hasNext() {
                return index < size();
            }

            public T next() {
                return get(index++);
            }
        };
    }

    public void clear() {
        Arrays.fill(elements, null);
        ringMode = false;
        head = 0;
    }
}