/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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

package org.eclipse.mosaic.fed.cell.config.gson;

import com.google.gson.FieldNamingStrategy;

import java.lang.reflect.Field;

public class RegionsNamingStrategy implements FieldNamingStrategy {

    @Override
    public String translateName(Field field) {
        switch (field.getName()) {
            case "a":
                return "nw";
            case "b":
                return "se";
            case "latitude":
                return "lat";
            case "longitude":
                return "lon";
            case "vertices":
                return "coordinates";
            default:
                return field.getName();
        }

    }
}
