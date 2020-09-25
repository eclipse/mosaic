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

package org.eclipse.mosaic.lib.model.delay;

import org.eclipse.mosaic.lib.math.RandomNumberGenerator;
import org.eclipse.mosaic.lib.model.gson.DelayTypeAdapterFactory;

import com.google.gson.annotations.JsonAdapter;

import java.io.Serializable;

/**
 * Basic Delay to be extended by specialized delays.
 */
@JsonAdapter(DelayTypeAdapterFactory.class)
public abstract class Delay implements Serializable {

    /**
     * Abstract method to be implemented by Delay-classes.
     *
     * @param randomNumberGenerator {@link RandomNumberGenerator} to be used for calculation
     * @param speedOfNode current speed of the sender
     * @return the calculated delay value
     */
    public abstract long generateDelay(RandomNumberGenerator randomNumberGenerator, double speedOfNode);
}
