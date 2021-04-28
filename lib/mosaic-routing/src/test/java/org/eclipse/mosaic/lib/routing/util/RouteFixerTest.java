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

package org.eclipse.mosaic.lib.routing.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.database.Database;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RouteFixerTest {

    private final List<String> brokenRoute = Lists.newArrayList(
            "52955705_30994888_670715050", "52924187_30994903_670046017", "52835767_670046017_334389638", "48403983_334389638_334389639",
            "48403988_334389639_334389642", "52835765_334389642_30903525");

    private final List<String> fixedRoute = Lists.newArrayList(
            "52955705_30994888_670715050", "52955705_670715050_2225831361", "52955705_2225831361_670714856", "52924192_670714856_30994903", "52924187_30994903_670046017",
            "52835767_670046017_334389638", "48403983_334389638_334389639", "48403988_334389639_334389642", "52835765_334389642_30903525");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Database database;

    @Before
    public void setUp() throws IOException {
        final File dbFileCopy = folder.newFile("Girona.db");

        FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/Girona.db"), dbFileCopy);

        database = Database.loadFromFile(dbFileCopy);
    }


    @Test
    public void GironaFix() {
        RouteFixer routeFixer = new RouteFixer(database, 8);

        List<String> fixedRoute = routeFixer.fixRoute(brokenRoute);

        assertTrue(fixedRoute.size() > brokenRoute.size());
        assertEquals(this.fixedRoute, fixedRoute);
    }

}