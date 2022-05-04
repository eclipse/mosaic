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

package org.eclipse.mosaic.lib.objects.v2x.etsi;

public class EtsiPayloadConfiguration {

    /**
     * If set to {@code true}, the payload of CAM, DENM, SPATM, and IVIM messages are
     * encoded into a byte array. This is required if other simulators except ApplicationNT need
     * to read actual data from the message. However, enabling this would  more computation
     * time and memory consumption.
     */
    public final boolean encodePayloads;

    public EtsiPayloadConfiguration(boolean encodePayloads) {
        this.encodePayloads = encodePayloads;
    }

    private static EtsiPayloadConfiguration globalConfiguration = null;

    public static EtsiPayloadConfiguration getPayloadConfiguration() {
        if (globalConfiguration == null) {
            setPayloadConfiguration(new EtsiPayloadConfiguration(false));
        }
        return EtsiPayloadConfiguration.globalConfiguration;
    }

    public static void setPayloadConfiguration(EtsiPayloadConfiguration payloadConfiguration) {
        if (EtsiPayloadConfiguration.globalConfiguration != null) {
            throw new IllegalStateException("Could not initialize EtsiPayloadConfiguration twice");
        }
        EtsiPayloadConfiguration.globalConfiguration = payloadConfiguration;
    }
}


