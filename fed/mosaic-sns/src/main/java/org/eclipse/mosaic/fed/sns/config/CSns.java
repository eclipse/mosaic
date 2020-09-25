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

package org.eclipse.mosaic.fed.sns.config;

import org.eclipse.mosaic.fed.sns.model.AdhocTransmissionModel;
import org.eclipse.mosaic.fed.sns.model.SimpleAdhocTransmissionModel;
import org.eclipse.mosaic.fed.sns.util.AdhocTransmissionModelTypeAdapterFactory;
import org.eclipse.mosaic.lib.model.delay.ConstantDelay;
import org.eclipse.mosaic.lib.model.delay.Delay;
import org.eclipse.mosaic.lib.model.gson.DelayTypeAdapterFactory;
import org.eclipse.mosaic.lib.model.transmission.CTransmission;

import com.google.gson.annotations.JsonAdapter;

/**
 * SNS configuration.
 */
public class CSns {

    /**
     * The threshold for the amount of hops for a transmission.
     * This is an additional, hard threshold, which can't be exceeded.
     */
    public int maximumTtl = 10;

    /**
     * Default radius to be used if an AdhocConfiguration doesn't specify
     * a radius.
     */
    public double singlehopRadius = 509.4;

    /**
     * Defines the {@link AdhocTransmissionModel} to be used for transmissions.
     */
    @JsonAdapter(AdhocTransmissionModelTypeAdapterFactory.class)
    public AdhocTransmissionModel adhocTransmissionModel = new SimpleAdhocTransmissionModel();

    /**
     * Delay configuration for a single hop.
     */
    @JsonAdapter(DelayTypeAdapterFactory.class)
    public Delay singlehopDelay = new ConstantDelay();

    /**
     * Transmission configuration for a single hop.
     */
    public CTransmission singlehopTransmission = new CTransmission();
}
