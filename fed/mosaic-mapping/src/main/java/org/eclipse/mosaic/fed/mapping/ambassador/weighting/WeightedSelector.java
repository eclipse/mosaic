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

package org.eclipse.mosaic.fed.mapping.ambassador.weighting;

/**
 * Interface for all {@link WeightedSelector}'s. Classes implementing this interface allow the random and pseudo-random
 * selection of multiple objects. The distribution can be defined by weights.
 *
 * @param <T> The type of the object to be returned
 */
public interface WeightedSelector<T extends Weighted> {

    /**
     * Request the next item.
     *
     * @return A random- or pseudo-randomly selected item from the list
     */
    T nextItem();
}
