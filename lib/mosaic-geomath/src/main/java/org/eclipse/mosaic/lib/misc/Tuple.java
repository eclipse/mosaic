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

package org.eclipse.mosaic.lib.misc;

public class Tuple<A, B> {

    private final A itemA;
    private final B itemB;

    public Tuple(A a, B b) {
        this.itemA = a;
        this.itemB = b;
    }

    public A getA() {
        return itemA;
    }

    public B getB() {
        return itemB;
    }
}
