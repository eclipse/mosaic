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
 * This interface is used to implement objects with a weight. The
 * calculation of the actual weight depends on the implementation.
 */
public interface Weighted {

    /**
     * Supplies the weight of the object. Depending on the
     * implementation this can be determined differently.
     *
     * @return weight of the object
     */
    double getWeight();
}
