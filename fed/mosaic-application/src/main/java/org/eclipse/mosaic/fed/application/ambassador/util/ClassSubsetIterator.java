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

package org.eclipse.mosaic.fed.application.ambassador.util;

import java.util.Iterator;

/**
 * Iterator, which returns only classes, which are a substitute of a
 * given class.
 * <br>
 * E.g:
 * 1) B extends A, C extends B
 * 2) list with items of type: [A,B,B,C,A,C,C]
 * <br>
 * -> {@code ClassSubSetIterator<A>(list, Class<A>)} would return during iteration: [A,B,B,C,A,C,C] since all classes extend A
 * <br>
 * -> {@code ClassSubSetIterator<B>(list, Class<B>)} would return during iteration: [B,B,C,C,C] since C extends B, but A does not extend B
 * <br>
 * -> {@code ClassSubSetIterator<C>(list, Class<C>)} would return during iteration: [C,C,C] since only C is a valid substitute of C
 * @param <T> the type of the {@link Iterator}
 */
public class ClassSubsetIterator<T> implements Iterator<T> {

    private Iterator<?> baseIterator;
    private T next;
    private Class<T> conditionalClass;

    public ClassSubsetIterator(Iterator<?> base, Class<T> conditionalClass) {
        this.baseIterator = base;
        this.conditionalClass = conditionalClass;
        searchForNext();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public T next() {
        T result = next;
        searchForNext();
        return result;
    }

    @SuppressWarnings("unchecked")
    private void searchForNext() {
        next = null;
        while (baseIterator.hasNext() && next == null) {
            Object candidate = baseIterator.next();
            if (conditionalClass.isAssignableFrom(candidate.getClass())) {
                next = (T) candidate;
            }
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not supported with a conditional Iterator");
    }
}
