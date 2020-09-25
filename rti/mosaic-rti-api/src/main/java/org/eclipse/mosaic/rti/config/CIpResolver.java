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

package org.eclipse.mosaic.rti.config;

/**
 * Configuration class for the IPResolver.
 */
public class CIpResolver {
    public String netMask    = "255.0.0.0";
    public String vehicleNet = "10.0.0.0";
    public String rsuNet     = "11.0.0.0";
    public String tlNet      = "12.0.0.0";
    public String csNet      = "13.0.0.0";
    public String serverNet  = "14.0.0.0";
    public String tmcNet     = "15.0.0.0";
}