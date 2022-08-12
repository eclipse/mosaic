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

package org.eclipse.mosaic.lib.database.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.mosaic.lib.util.junit.TestFileRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * This tests the access class for SQLite files. At least reading and writing needs to be checked.
 */
public class SQLiteAccessTest {

    private SQLiteAccess testDb;

    @Rule
    public TestFileRule testFileRule = new TestFileRule().with("/butzbach_outdated.db");

    @Before
    public void setUp() throws Exception {
        String path = testFileRule.get("butzbach_outdated.db").getAbsolutePath();

        testDb = new SQLiteAccess(path);
        testDb.connect();
    }

    /**
     * Test automatic access to an existing file (independent from it having content)
     */
    @Test
    public void testSimpleDBConnection() throws SQLException {
        SQLiteAccess connectionDB = new SQLiteAccess("");

        assertNotNull("database object wasn't created", connectionDB);
        assertNotNull("database connection wasn't created", connectionDB.getConnection());
        assertFalse("database connection is closed, but shouldn't be", connectionDB.getConnection().isClosed());

        connectionDB.disconnect(null);
        assertTrue("connection wasn't closed", connectionDB.getConnection().isClosed());
    }

    /**
     * Test manual access to an existing file (independent of it having content).
     */
    @Test
    public void testManualDBConnection() throws SQLException {
        SQLiteAccess connectionDB = new SQLiteAccess();

        // RUN
        connectionDB.setDatabaseFile("");
        //ASSERT
        assertNotNull("connection wasn't created", connectionDB.getConnection());
        assertTrue("connection is open, but should still be closed", connectionDB.getConnection().isClosed());

        //RUN
        Statement statement = connectionDB.connect();
        //ASSERT
        assertFalse("connection shouldn't be closed", connectionDB.getConnection().isClosed());

        // RUN
        connectionDB.disconnect(statement);
        //ASSERT
        assertTrue("connection wasn't closed after disconnection", connectionDB.getConnection().isClosed());
    }

    /**
     * Test if statements can be resolved.
     */
    @Test
    public void testExecuteStatement() throws SQLException {
        SQLiteAccess.Result testResult = testDb.executeStatement("SELECT name FROM sqlite_master WHERE type='table' and name='nodes' ORDER BY name;");
        assertNotNull("statement didn't create any result", testResult);
        assertNotNull("created result isn't valid", testResult.getRows());
        assertEquals("Wrong result content amount", 1, testResult.getRows().size());
        assertEquals("result content isn't valid", "nodes", testResult.getFirstRow().getString("name"));
    }

}
