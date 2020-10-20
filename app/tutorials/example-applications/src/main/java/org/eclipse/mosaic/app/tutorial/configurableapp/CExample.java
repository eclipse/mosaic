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

package org.eclipse.mosaic.app.tutorial.configurableapp;

import java.util.ArrayList;

/**
 * This is an example of a simple configuration class.
 */
public class CExample {
    public Integer fooInteger; //we use it as a wanted speed value, but it can be anything else
    public String fooString;
    public ArrayList<String> fooStringList = new ArrayList<>();
}
